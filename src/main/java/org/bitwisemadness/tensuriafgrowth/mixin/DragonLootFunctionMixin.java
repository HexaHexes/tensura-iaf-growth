package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.loot.DragonLootFunction;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Rolls Ice and Fire's dragon loot counts on the damped age axis (issue #87).
 *
 * <p>{@code DragonLootFunction} keys every count to the raw age - bones as
 * {@code 1 + nextInt(1 + age / 25)}, scales as {@code age / 25 +
 * nextInt(1 + age / 5)}, eggs as {@code 1 + nextInt(1 + age / 5)} - so at a
 * raised cap the stock formula pays out several times the stock yield (a cap
 * 512 skeleton rolls up to 18 bones where stock rolls 5; a fresh corpse up to
 * 104 scales where stock rolls 26). Redirecting the age reads to
 * {@link Growth#dampLootAge} damps all three branches with one injection -
 * the square root of the stretch, which is the identity at the untouched 128
 * ({@code sqrt(1.0) == 1.0} exactly), half the axis at a 4x cap, and
 * symmetric below 128, matching the corpse axis #88 put the stage and its
 * gates on.
 */
@Mixin(value = DragonLootFunction.class, remap = false)
public final class DragonLootFunctionMixin {

    @Redirect(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lcom/iafenvoy/iceandfire/entity/DragonBaseEntity;getAgeInDays()I"
        )
    )
    private int tensura_iaf_growth$rollOnDampedAge(DragonBaseEntity dragon) {
        return Growth.dampLootAge(dragon.getAgeInDays());
    }
}
