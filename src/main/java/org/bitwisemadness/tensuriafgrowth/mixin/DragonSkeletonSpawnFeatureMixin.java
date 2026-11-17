package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.world.feature.DragonSkeletonSpawnFeature;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Scales the dragon skeleton feature's age by the growth stretch (issue #82)
 * and keeps the death stage in step with the stretched age (issue #84).
 *
 * <p>The remains the feature places are aged at {@code 10 + random.nextInt(100)}
 * days so they render as the adult skeletons of a long-dead dragon, but the age
 * is written straight into the entity while the render size reads the stretched
 * stage table - under a raised cap every skeleton would come out hatchling
 * sized. Scaling the whole argument keeps the roll's spread over the stretched
 * axis; at the untouched default of 128 days {@link Growth#scaleDays} is the
 * identity and the feature places byte-for-byte vanilla skeletons.
 *
 * <p>Ice and Fire serves the remains as a skeleton - bone texture and the
 * {@code *_dragon_skeleton} loot table, with the flesh harvest stages (scales,
 * meat, blood, heart) already out of reach - only while
 * {@code deathStage >= ageInDays / 10}. The feature derives the stage from the
 * same unstretched roll ({@code days / 10}), so once the age above is scaled
 * the comparison fails permanently and <em>every</em> placement comes out a
 * fresh, flesh-lootable corpse (issue #84). Reading the stretched age back and
 * taking the maximum keeps the invariant at any cap and collapses to the stock
 * {@code days / 10} at the untouched 128, where {@code age == days} - the
 * identity-parity claim of issue #82 holds.
 */
@Mixin(value = DragonSkeletonSpawnFeature.class, remap = false)
public final class DragonSkeletonSpawnFeatureMixin {

    @Redirect(
        method = "place",
        at = @At(
            value = "INVOKE",
            target = "Lcom/iafenvoy/iceandfire/entity/DragonBaseEntity;growDragon(I)V"
        )
    )
    private void tensura_iaf_growth$stretchSkeletonAge(DragonBaseEntity dragon, int days) {
        dragon.growDragon(Growth.scaleDays(days));
    }

    @Redirect(
        method = "place",
        at = @At(
            value = "INVOKE",
            target = "Lcom/iafenvoy/iceandfire/entity/DragonBaseEntity;setDeathStage(I)V"
        )
    )
    private void tensura_iaf_growth$alignDeathStageToStretchedAge(DragonBaseEntity dragon, int stage) {
        dragon.setDeathStage(Math.max(stage, dragon.getAgeInDays() / 10));
    }
}
