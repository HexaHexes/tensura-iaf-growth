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
 * {@code *_dragon_skeleton} loot table - only once the death stage reaches
 * the skeleton threshold derived from the same age, while its harvest ladder
 * ({@code mobInteract}) refuses <em>all</em> interaction the moment the stage
 * reaches {@code min(age / 5, 25)} - and corpses are damage-immune, so that
 * refusal leaves the skeleton unharvestable (issue #84, issue #88). At the
 * untouched 128-day default the two thresholds sit exactly {@code age / 10}
 * apart in the stock favour, but a raised cap pushed the raw {@code age / 10}
 * past the ladder's capped ceiling (age >= 250 at cap 512), satisfying
 * neither side. {@link Growth#corpseStage} is the single canonical value that
 * satisfies both - skeleton-textured and harvestable at any cap - and it
 * equals {@code days / 10} at the untouched 128, where {@code age == days},
 * so the identity-parity claim of issue #82 still holds.
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

    /**
     * Places the remains at the canonical corpse stage instead of the stock
     * {@code days / 10}: at the untouched default the two are equal, under a
     * raised cap the stage stays on the damped axis the skeleton gates (now
     * redirected the same way - see {@code DragonDeadLootTableMixin} and
     * friends) and the harvest ladder read. The stock argument is therefore
     * superseded rather than maxed - {@link Growth#corpseStage} already folds
     * in everything the old {@code max} protected.
     */
    @Redirect(
        method = "place",
        at = @At(
            value = "INVOKE",
            target = "Lcom/iafenvoy/iceandfire/entity/DragonBaseEntity;setDeathStage(I)V"
        )
    )
    private void tensura_iaf_growth$placeCanonicalCorpseStage(DragonBaseEntity dragon, int stage) {
        dragon.setDeathStage(Growth.corpseStage(dragon.getAgeInDays()));
    }
}
