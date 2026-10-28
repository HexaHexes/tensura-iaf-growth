package org.bitwisemadness.tensuriafgrowth.handler;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import io.github.manasmods.manascore.config.ConfigRegistry;
import io.github.manasmods.manascore.storage.api.StorageHolder;
import io.github.manasmods.tensura.config.entity.AttributeConfig;
import io.github.manasmods.tensura.registry.data.TensuraCustomData;
import io.github.manasmods.tensura.data.existence.EntityExistenceData;
import io.github.manasmods.tensura.registry.attribute.TensuraAttributes;
import io.github.manasmods.tensura.storage.ep.ExistenceStorage;
import io.github.manasmods.tensura.storage.ep.IExistence;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bitwisemadness.tensuriafgrowth.Growth;

/**
 * Makes a dragon's Tensura existence follow its growth, and caps what its banked
 * EP can do to its stats.
 *
 * <p>Three layers, each with one job:
 *
 * <ul>
 *   <li><strong>The growth curve</strong> - Ice and Fire's own {@code updateAttributes},
 *   stretched by {@link org.bitwisemadness.tensuriafgrowth.mixin.DragonBaseEntityGrowthMixin},
 *   sets HP/attack/armor from age. Deterministic, identical for all three dragon types
 *   (min {@code 0.04 x maxHealth} to max {@code maxHealth} over the span), and it is
 *   re-run before every refresh below, so the curve is always the floor.</li>
 *   <li><strong>The EP pin</strong> - the dragon's aura and magicule no longer come from
 *   the spawn roll. They lerp inside the entity_existence band with the growth fraction,
 *   and the matching max-attribute caps follow, so Tensura's join-time clamping and EP
 *   regen stay aligned with the pin instead of fighting it. This is what makes growth
 *   stage the EP lever for the dragon's own existence.</li>
 *   <li><strong>The capped bank</strong> - EP the dragon earns (kills, regen - anything
 *   that reaches {@code addStatOnEPGain}, which {@link org.bitwisemadness.tensuriafgrowth.mixin.AttributeHandlerMixin}
 *   turns into a bank-only operation for dragons) still makes it stronger, but the stat
 *   bonus is recomputed from {@code floor(min(bank, budget) / 10,000) x gains} where the
 *   budget is the band maximum scaled by the growth fraction. A whelp has no budget, so
 *   no amount of banked EP inflates a stage 1; a max-growth dragon's budget is the full
 *   band, so a well-fought one lands at the cap and can never exceed it. The recomputation
 *   is total, never incremental: every refresh writes {@code curve + bonus}, so old
 *   dragons with inflated bases correct themselves on the first refresh after load.</li>
 * </ul>
 *
 * <p>Refresh runs at the tail of every {@code updateAttributes} call - construction,
 * load, each growth day via {@code growDragon}, and equipment changes - and after every
 * banked EP gain, which is where "gets stronger as it fights" is felt. It never heals and
 * never touches movement speed or the equipment armor modifier; those layers belong to
 * Ice and Fire.
 */
public final class DragonGrowthStatHandler {
    /** Tensura's stat-step size: one step of each gain per this much banked EP. */
    private static final double EP_PER_STEP = 10000.0D;
    /** The full-growth span Ice and Fire's attribute curve was authored against. */
    private static final int CURVE_SPAN = 125;

    private DragonGrowthStatHandler() {
    }

    /** Called from the mixin at the tail of {@code updateAttributes} and after banked gains. */
    public static void refresh(DragonBaseEntity dragon) {
        if (dragon.level() == null || dragon.level().isClientSide) {
            return;
        }
        IExistence existence = ((StorageHolder) dragon).manasCore$getStorage(ExistenceStorage.getKey());
        if (existence == null) {
            return;
        }
        EntityExistenceData band = dragon.level().registryAccess()
                .registryOrThrow(TensuraCustomData.ENTITY_EXISTENCE)
                .get(BuiltInRegistries.ENTITY_TYPE.getKey(dragon.getType()));
        double growth = Mth.clamp(dragon.getAgeInDays() / (double) Growth.scaleDays(CURVE_SPAN), 0.0D, 1.0D);
        if (band != null) {
            pinExistence(dragon, existence, band, growth);
            applyBankedBonus(dragon, existence, band, growth);
        }
    }

    /**
     * Pins aura and magicule, and their max-attribute caps, to the growth fraction
     * inside the entity_existence band. Guarded writes: refresh runs often, and the
     * storage is marked dirty only when a value actually moved.
     */
    private static void pinExistence(DragonBaseEntity dragon, IExistence existence,
            EntityExistenceData band, double growth) {
        double magicule = Mth.lerp(growth, band.minMagicule(), band.maxMagicule());
        double aura = Mth.lerp(growth, band.minAura(), band.maxAura());
        boolean moved = false;
        if (existence.getMagicule() != magicule) {
            existence.setMagicule(magicule);
            setMaxBase(dragon, TensuraAttributes.MAX_MAGICULE, magicule);
            moved = true;
        }
        if (existence.getAura() != aura) {
            existence.setAura(aura);
            setMaxBase(dragon, TensuraAttributes.MAX_AURA, aura);
            moved = true;
        }
        if (moved) {
            existence.markDirty();
        }
    }

    /**
     * Applies the banked-EP stat bonus on top of the curve base the caller has just
     * written. The budget grows with the dragon: {@code (maxMagicule + maxAura) x growth},
     * so a hatchling's budget is zero and a full-growth dragon's is the whole band.
     */
    private static void applyBankedBonus(DragonBaseEntity dragon, IExistence existence,
            EntityExistenceData band, double growth) {
        double budget = (band.maxMagicule() + band.maxAura()) * growth;
        double banked = Math.min(existence.getGainedEP(), budget);
        double steps = Math.floor(banked / EP_PER_STEP);
        if (steps <= 0.0D) {
            return;
        }
        AttributeConfig config = ConfigRegistry.getConfig(AttributeConfig.class);
        addBaseBonus(dragon, Attributes.MAX_HEALTH, steps * config.HPGain);
        addBaseBonus(dragon, Attributes.ATTACK_DAMAGE, steps * config.attackGain);
        addBaseBonus(dragon, Attributes.ARMOR, steps * config.armorGain);
    }

    /** Adds the bonus to the attribute's base value - the curve - without touching modifiers. */
    private static void addBaseBonus(LivingEntity entity, Holder<Attribute> attribute, double bonus) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        double target = instance.getBaseValue() + bonus;
        if (instance.getBaseValue() != target) {
            instance.setBaseValue(target);
        }
    }

    /** Sets a max-energy attribute's base value, in the shape {@code initEntityExistence} uses. */
    private static void setMaxBase(DragonBaseEntity dragon, Holder<Attribute> attribute, double value) {
        AttributeInstance instance = dragon.getAttribute(attribute);
        if (instance != null && instance.getBaseValue() != value) {
            instance.setBaseValue(value);
        }
    }
}
