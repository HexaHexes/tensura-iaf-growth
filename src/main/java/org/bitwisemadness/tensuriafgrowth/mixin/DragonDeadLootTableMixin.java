package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.entity.FireDragonEntity;
import com.iafenvoy.iceandfire.entity.IceDragonEntity;
import com.iafenvoy.iceandfire.entity.LightningDragonEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Moves the dead-loot table's skeleton gate onto the canonical corpse axis
 * (issue #88).
 *
 * <p>Each dragon subtype's {@code getDeadLootTable} picks the skeleton table
 * while {@code deathStage >= (ageInDays / 5) / 2}. Under a raised cap the raw
 * age pushes that threshold past the harvest ladder's ceiling
 * ({@code min(age / 5, 25)}) and the stage - placed at the damped value by
 * {@code DragonSkeletonSpawnFeatureMixin} - would never reach it, serving
 * flesh loot from skeleton-textured remains. Rewriting the age read to
 * {@code 10 * corpseStage(age)} folds the damping into the method's own
 * arithmetic: {@code (10 * s / 5) / 2 == s} exactly for the integer
 * divisions, so the gate fires precisely when the stage reaches
 * {@link Growth#corpseStage}. At the untouched 128 the helper returns
 * {@code ageInDays}, leaving the stock comparison untouched.
 *
 * <p>Each subtype references {@code getAgeInDays} with <em>itself</em> as the
 * invocation owner (javac emits the receiver's static type), so the target
 * names only the member - {@code MemberInfo} matches any owner. The handler
 * therefore takes just the modified value and no receiver, which is what lets
 * one method serve all three subclasses: a {@code @Redirect} would have to
 * declare each call site's exact owner as its first parameter.
 */
@Mixin(
    value = { FireDragonEntity.class, IceDragonEntity.class, LightningDragonEntity.class },
    remap = false
)
public final class DragonDeadLootTableMixin {

    @ModifyExpressionValue(
        method = "getDeadLootTable",
        at = @At(
            value = "INVOKE",
            target = "getAgeInDays()I"
        )
    )
    private int tensura_iaf_growth$observeCorpseStageAge(int ageInDays) {
        return 10 * Growth.corpseStage(ageInDays);
    }
}
