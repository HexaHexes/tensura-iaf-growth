package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.tensura.data.existence.EntityExistenceData;
import io.github.manasmods.tensura.handler.AttributeHandler;
import io.github.manasmods.tensura.storage.ep.IExistence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.bitwisemadness.tensuriafgrowth.handler.DragonGrowthStatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tensura attribute-layer corrections for dragons.
 *
 * <p>Two jobs: granted skills start at full mastery (the redirect below), and the
 * stat-on-EP bake is cancelled for dragons - see
 * {@link DragonGrowthStatHandler} and {@code tensura_iaf_growth$bankDragonEPWithoutBaking}.
 */
@Mixin(value = AttributeHandler.class, remap = false)
public final class AttributeHandlerMixin {
    @Redirect(
        method = "initEntityExistence(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/LevelAccessor;Lio/github/manasmods/tensura/storage/ep/IExistence;Lio/github/manasmods/tensura/data/existence/EntityExistenceData;)V",
        at = @At(
            value = "INVOKE",
            target = "Lio/github/manasmods/manascore/skill/api/ManasSkillInstance;canBeToggled(Lnet/minecraft/world/entity/LivingEntity;)Z"
        )
    )
    private static boolean tensura_iaf_growth$forceMasteryOnGrant(ManasSkillInstance instance, LivingEntity entity) {
        instance.setMastery(instance.getMaxMastery());
        return instance.canBeToggled(entity);
    }

    /**
     * Cancels Tensura's stat-on-EP bake for dragons; EP gained is only banked.
     *
     * <p>{@code addStatOnEPGain} fires whenever MAX_AURA/MAX_MAGICULE's base
     * value increases (the join-time band init, naming rewards including endow's
     * 9x gain, and this mod's own line-following max writes) and permanently
     * adds {@code HPGain / armorGain / attackGain} to an entity's base attributes
     * per 10,000 of the increase, with no ceiling - the mechanism behind stage 1
     * dragons at 1,900 HP and wild lightning dragons at 20k. Since 1.5.0 the
     * banked-EP stat budget that used to convert this into a capped bonus is
     * deprecated (issue #30): HP, attack and armor are the pure Ice and Fire
     * growth curve, so the bake is cancelled outright. {@code gainedEP} still
     * accumulates (harmlessly, for downstream readers) and
     * {@code updateAttributes} is re-run, which re-derives the growth-pinned EP.
     * Tensura's own remainder bookkeeping and the step-payout heal are skipped
     * by design - dragons heal through their own regeneration.
     *
     * <p>The {@code ep_initiate_excluded} tag was considered for this and rejected: the
     * join handler reads the same tag and excludes marked entities from
     * {@code updateEntityExistence} entirely, which would strip dragons of their granted
     * abilities - breaths, resistances and the secondary elements.
     */
    @Inject(
        method = "addStatOnEPGain(Lnet/minecraft/world/entity/LivingEntity;Lio/github/manasmods/tensura/storage/ep/IExistence;D)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void tensura_iaf_growth$bankDragonEPWithoutBaking(LivingEntity entity, IExistence existence,
            double gained, CallbackInfo ci) {
        if (!(entity instanceof DragonBaseEntity dragon)) {
            return;
        }
        existence.setGainedEP(existence.getGainedEP() + gained);
        existence.markDirty();
        if (dragon.level() != null) {
            dragon.updateAttributes();
        }
        ci.cancel();
    }
}
