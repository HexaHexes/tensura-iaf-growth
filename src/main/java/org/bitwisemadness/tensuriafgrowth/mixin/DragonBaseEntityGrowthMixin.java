package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.bitwisemadness.tensuriafgrowth.handler.DragonGrowthStatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stretches Ice and Fire's dragon growth curve across the configured age cap,
 * clamps the armor line the stretch exposes, and hands every attribute update
 * to the growth-pinned stat handler.
 *
 * <p>Every constant here is a point on the age axis: the span the attributes
 * grow over ({@code 125}), the stage boundaries ({@code 25/50/75/100}), the
 * size and step-height curves, and the fly-speed ramp. Each is multiplied by
 * {@code f = maxTamedDragonAge / 128}, so the curve ends at the same place -
 * full size, full attributes, stage 5 - but takes the whole raised cap to get
 * there. At the untouched default of 128 every hook returns its original
 * value, so the addon is byte-for-byte vanilla until the cap is raised.
 *
 * <p>The two {@code 128} literals are not stretched: they are the age cap
 * itself, and are replaced by the configured value so untamed dragons are
 * uncapped along with tamed ones.
 *
 * <p>Two hooks live on {@code updateAttributes} beyond the stretch: the armor
 * line's raw age is clamped to the span (vanilla's 128 cap hid the overshoot),
 * and the tail hands off to {@link DragonGrowthStatHandler} so the dragon's EP
 * follows growth - see that class for the pin.
 */
@Mixin(value = DragonBaseEntity.class, remap = false)
public final class DragonBaseEntityGrowthMixin {
    // --- Age cap -----------------------------------------------------------
    // Both sites read `isTame() ? maxTamedDragonAge : 128`; the literal is the
    // untamed cap, which must follow the config or wild dragons never stretch.

    @ModifyConstant(method = "getAgeInDays", constant = @Constant(intValue = 128))
    private int tensura_iaf_growth$untamedAgeCap(int vanillaCap) {
        return Growth.maxAge();
    }

    @ModifyConstant(method = "mobInteract", constant = @Constant(intValue = 128))
    private int tensura_iaf_growth$untamedMealCap(int vanillaCap) {
        return Growth.maxAge();
    }

    // --- Attribute growth --------------------------------------------------
    // age is clamped to the span, and each step divides by the same span, so
    // full stats land on day 125 * f instead of day 125.

    @ModifyConstant(method = "updateAttributes", constant = @Constant(intValue = 125))
    private int tensura_iaf_growth$attributeAgeSpan(int vanillaSpan) {
        return Growth.scaleDays(vanillaSpan);
    }

    @ModifyConstant(method = "updateAttributes", constant = @Constant(doubleValue = 125.0D))
    private double tensura_iaf_growth$attributeStepSpan(double vanillaSpan) {
        return Growth.scaleSpan(vanillaSpan);
    }

    // --- Spawn age ---------------------------------------------------------
    // Wild dragons roll 1..80 days out of a 128 day life; scale the roll so the
    // spawned stage distribution survives the stretch.

    @ModifyConstant(method = "finalizeSpawn", constant = @Constant(intValue = 80))
    private int tensura_iaf_growth$spawnAgeRange(int vanillaRange) {
        return Growth.scaleDays(vanillaRange);
    }

    @ModifyConstant(method = "finalizeSpawn", constant = @Constant(doubleValue = 125.0D))
    private double tensura_iaf_growth$spawnHealthSpan(double vanillaSpan) {
        return Growth.scaleSpan(vanillaSpan);
    }

    // --- Growth stages -----------------------------------------------------
    // Stage 2/3/4/5 at 25/50/75/100 days becomes 25f/50f/75f/100f, keeping
    // isBaby/isTeen/isMature (and therefore loot, size and EP stage) in step
    // with the stretched clock.

    @ModifyConstant(method = "getDragonStage", constant = @Constant(intValue = 100))
    private int tensura_iaf_growth$stageFiveFrom(int vanillaDays) {
        return Growth.scaleDays(vanillaDays);
    }

    @ModifyConstant(method = "getDragonStage", constant = @Constant(intValue = 75))
    private int tensura_iaf_growth$stageFourFrom(int vanillaDays) {
        return Growth.scaleDays(vanillaDays);
    }

    @ModifyConstant(method = "getDragonStage", constant = @Constant(intValue = 50))
    private int tensura_iaf_growth$stageThreeFrom(int vanillaDays) {
        return Growth.scaleDays(vanillaDays);
    }

    @ModifyConstant(method = "getDragonStage", constant = @Constant(intValue = 25))
    private int tensura_iaf_growth$stageTwoFrom(int vanillaDays) {
        return Growth.scaleDays(vanillaDays);
    }

    // --- Step height -------------------------------------------------------
    // Grows from 1.2 to 3.0 between day 25 and day 125. Stretching both bounds
    // and the denominator keeps the endpoints identical over the longer ramp.

    @ModifyConstant(method = "maxUpStep", constant = @Constant(intValue = 125))
    private int tensura_iaf_growth$stepHeightTop(int vanillaDays) {
        return Growth.scaleDays(vanillaDays);
    }

    @ModifyConstant(method = "maxUpStep", constant = @Constant(intValue = 25))
    private int tensura_iaf_growth$stepHeightBase(int vanillaDays) {
        return Growth.scaleDays(vanillaDays);
    }

    @ModifyConstant(method = "maxUpStep", constant = @Constant(floatValue = 100.0F))
    private float tensura_iaf_growth$stepHeightSpan(float vanillaSpan) {
        return Growth.scaleSpan(vanillaSpan);
    }

    // --- Body size ---------------------------------------------------------
    // The size table is indexed by stage (already stretched above); the two
    // 25.0F constants are the per-day step and the full step, so scaling both
    // keeps the final size byte-identical while the growth takes 25f days.

    @ModifyConstant(method = "getRenderSize", constant = @Constant(intValue = 125))
    private int tensura_iaf_growth$sizeAgeCap(int vanillaDays) {
        return Growth.scaleDays(vanillaDays);
    }

    @ModifyConstant(method = "getRenderSize", constant = @Constant(floatValue = 25.0F))
    private float tensura_iaf_growth$sizeStepSpan(float vanillaSpan) {
        return Growth.scaleSpan(vanillaSpan);
    }

    @ModifyConstant(method = "getAgeFactor", constant = @Constant(intValue = 25))
    private int tensura_iaf_growth$ageFactorSpan(int vanillaDays) {
        return Growth.scaleDays(vanillaDays);
    }

    // --- Fly speed ---------------------------------------------------------
    // Ramps with age up to 125 days; the denominator keeps the top speed where
    // it was while the ramp takes the whole stretched life to climb.

    @ModifyConstant(method = "getFlySpeed", constant = @Constant(doubleValue = 125.0D))
    private double tensura_iaf_growth$flySpeedSpan(double vanillaSpan) {
        return Growth.scaleSpan(vanillaSpan);
    }

    // --- Armor clamp -------------------------------------------------------
    // The armor line multiplies the raw getAgeInDays() - unclamped, unlike the
    // health line above it. Vanilla's 128-day cap sat just above the 125-day
    // span, so the overshoot was bounded at ~2%; with the raised cap armor
    // would grow linearly forever past full growth. Redirect the second
    // invocation (the armor line's) to the clamped age so armor tops at Ice and
    // Fire's own maximumArmor exactly when HP does.

    @Redirect(
        method = "updateAttributes()V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/iafenvoy/iceandfire/entity/DragonBaseEntity;getAgeInDays()I",
            ordinal = 1
        )
    )
    private int tensura_iaf_growth$clampArmorAge(DragonBaseEntity dragon) {
        return Math.min(dragon.getAgeInDays(), Growth.scaleDays(125));
    }

    // --- Growth-pinned existence -------------------------------------------
    // Every updateAttributes run ends with the growth handler: the dragon's EP
    // is pinned to the stage-banded curve from this mod's config by age - see
    // DragonGrowthStatHandler. updateAttributes runs at construction, on load,
    // on each growth day (growDragon) and on equipment changes, so the recompute
    // covers every way a dragon's age or belongings can move.

    @Inject(method = "updateAttributes()V", at = @At("TAIL"))
    private void tensura_iaf_growth$refreshGrowthPinnedStats(CallbackInfo ci) {
        // The downcast travels through an Object parameter: static analysis only
        // sees the unmerged mixin type and would call it impossible, while the
        // merge into DragonBaseEntity makes it succeed at runtime.
        tensura_iaf_growth$refreshGrowthPinnedStatsOn(this);
    }

    private static void tensura_iaf_growth$refreshGrowthPinnedStatsOn(Object dragon) {
        DragonGrowthStatHandler.refresh((DragonBaseEntity) dragon);
    }
}
