package org.bitwisemadness.tensuriafgrowth.handler;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import io.github.manasmods.manascore.storage.api.StorageHolder;
import io.github.manasmods.tensura.registry.attribute.TensuraAttributes;
import io.github.manasmods.tensura.storage.ep.ExistenceStorage;
import io.github.manasmods.tensura.storage.ep.IExistence;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.bitwisemadness.tensuriafgrowth.config.DragonEpConfig;

/**
 * Pins a dragon's Tensura existence (EP, and the aura/magicule pools and their
 * max caps behind it) to the stage-banded growth curve shipped as this mod's
 * config - see {@link DragonEpConfig}.
 *
 * <p>Two paths, chosen by a persistent-data flag:
 *
 * <ul>
 *   <li><strong>Migration snap (first refresh on 1.5.0, flag absent)</strong> -
 *   pools and max bases are rewritten to the line exactly: aura = line / 10,
 *   magicule = line x 9 / 10, so {@code aura + magicule = line} at every age.
 *   History at this point - spawn rolls, values pinned against the old
 *   240k-340k band, modifier artifacts - is indistinguishable from a top-up,
 *   so it is normalized once, here.</li>
 *   <li><strong>Floor pin (flag present)</strong> - "grow up, never clip down":
 *   {@code sum := clamp(sum, line, ceiling)}. Below the line the pools are
 *   lifted to the line shares; above the ceiling they are clipped down and
 *   re-split 1:9; in between they are left untouched, which is what lets
 *   earned top-ups survive. The ceiling is
 *   {@code max(stage cap, max-attribute bases)}: naming gains - including
 *   endow's 9x EP, which Tensura grants by raising MAX_AURA/MAX_MAGICULE bases
 *   before lifting the pools - ride the bases through every refresh and relog,
 *   even past {@code stage5EpCap}. Kill top-ups that only touch the pools ride
 *   the stage cap. The max bases themselves get the line as a floor (never
 *   clipped down, raised when the line grows) and never fall behind the pools
 *   they cap, preserving Tensura's {@code current <= max} invariant.</li>
 * </ul>
 *
 * <p>Refresh runs at the tail of every {@code updateAttributes} call -
 * construction, load, each growth day via {@code growDragon}, equipment changes
 * - and on {@link EntityJoinLevelEvent}, which is what catches spawn rolls and
 * load-time values before anything else observes them. It never heals and never
 * touches movement speed or the equipment armor modifier; HP, attack and armor
 * are the pure Ice and Fire growth curve (the banked-EP stat budget is
 * deprecated - see issue #30).
 */
public final class DragonGrowthStatHandler {
    /**
     * Persistent-data flag: the migration snap has run, so this dragon is under
     * floor-pin semantics from here on.
     */
    private static final String MIGRATED_KEY = "tensura_iaf_growth_ep_pinned";

    private DragonGrowthStatHandler() {
    }

    /**
     * Join-time refresh. The spawn roll (and any load-time value pinned against
     * the old band) exceeds the early-stage caps and {@code updateAttributes}
     * does not run at construction, so the pin must also run the moment the
     * dragon enters the level.
     */
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof DragonBaseEntity dragon) {
            refresh(dragon);
        }
    }

    /**
     * Called from the mixin at the tail of {@code updateAttributes} and after banked gains.
     *
     * <p>Reentrancy guard lives here: Tensura fires
     * {@code ATTRIBUTE_BASE_CHANGE_EVENT} from an {@code @Inject} at the
     * <em>HEAD</em> of {@code AttributeInstance.setBaseValue} (verified in
     * {@code MixinAttributeInstance}), so while listeners run
     * {@code getBaseValue()} still reads the old value and the new base has
     * not committed. Every max-base write this pin makes therefore re-enters
     * through the bake mixin ({@code addStatOnEPGain} -&gt;
     * {@code updateAttributes} -&gt; tail) with the write still mid-flight, and
     * without this guard the nested pass sees the same stale base, "raises"
     * it again, and recurses until the stack blows - the 1.5.0 spawn-egg
     * {@code StackOverflowError}. Nested calls return immediately; the
     * outermost pass is still running and completes every remaining write
     * itself, so nothing is skipped. Entity attribute writes run on the server
     * thread, so a plain static flag is correct.
     */
    private static boolean refreshing;

    public static void refresh(DragonBaseEntity dragon) {
        if (refreshing) {
            return;
        }
        if (dragon.level() == null || dragon.level().isClientSide) {
            return;
        }
        IExistence existence = ((StorageHolder) dragon).manasCore$getStorage(ExistenceStorage.getKey());
        if (existence == null) {
            return;
        }
        refreshing = true;
        try {
            pin(dragon, existence);
        } finally {
            refreshing = false;
        }
    }

    /** One of the two pin paths; guarded writes, storage marked dirty only when something moved. */
    private static void pin(DragonBaseEntity dragon, IExistence existence) {
        CompoundTag data = dragon.getPersistentData();
        int age = dragon.getAgeInDays();
        double line = DragonEpConfig.stageLine(age);
        double lineAura = line / 10.0D;
        double lineMagicule = line * 9.0D / 10.0D;
        boolean moved = false;
        if (!data.getBoolean(MIGRATED_KEY)) {
            moved |= setPools(existence, lineAura, lineMagicule);
            moved |= setMaxBase(dragon, TensuraAttributes.MAX_AURA, lineAura);
            moved |= setMaxBase(dragon, TensuraAttributes.MAX_MAGICULE, lineMagicule);
            data.putBoolean(MIGRATED_KEY, true);
        } else {
            // Ceiling first: the bases are read before this refresh writes them,
            // so raises naming made since the last refresh (namingReward puts the
            // gain into both bases) are still counted here.
            double ceiling = Math.max(DragonEpConfig.stageCeiling(age), maxBaseSum(dragon));
            double sum = existence.getAura() + existence.getMagicule();
            double target = Mth.clamp(sum, line, ceiling);
            if (target != sum) {
                moved |= setPools(existence, target / 10.0D, target * 9.0D / 10.0D);
            }
            moved |= setMaxBase(dragon, TensuraAttributes.MAX_AURA,
                    Math.max(lineAura, existence.getAura()));
            moved |= setMaxBase(dragon, TensuraAttributes.MAX_MAGICULE,
                    Math.max(lineMagicule, existence.getMagicule()));
        }
        if (moved) {
            existence.markDirty();
        }
    }

    /** Writes both pools; returns whether either one actually changed. */
    private static boolean setPools(IExistence existence, double aura, double magicule) {
        boolean moved = false;
        if (existence.getAura() != aura) {
            existence.setAura(aura);
            moved = true;
        }
        if (existence.getMagicule() != magicule) {
            existence.setMagicule(magicule);
            moved = true;
        }
        return moved;
    }

    /**
     * Sets a max-energy attribute's base value, in the shape
     * {@code initEntityExistence} uses. Raising it fires Tensura's
     * attribute-base-change event <em>before</em> the write commits, whose bake
     * our mixin cancels for dragons - the banked delta is harmless, and the
     * nested {@code updateAttributes} tail returns at the guard in
     * {@link #refresh} while this write is still in flight.
     */
    private static boolean setMaxBase(DragonBaseEntity dragon, Holder<Attribute> attribute, double value) {
        AttributeInstance instance = dragon.getAttribute(attribute);
        if (instance == null || instance.getBaseValue() == value) {
            return false;
        }
        instance.setBaseValue(value);
        return true;
    }

    /** The sum of the two max-energy bases, before this refresh writes anything. */
    private static double maxBaseSum(DragonBaseEntity dragon) {
        return baseOf(dragon, TensuraAttributes.MAX_AURA) + baseOf(dragon, TensuraAttributes.MAX_MAGICULE);
    }

    private static double baseOf(DragonBaseEntity dragon, Holder<Attribute> attribute) {
        AttributeInstance instance = dragon.getAttribute(attribute);
        return instance == null ? 0.0D : instance.getBaseValue();
    }
}
