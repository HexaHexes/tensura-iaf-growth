package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.world.feature.DragonSkeletonSpawnFeature;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Scales the dragon skeleton feature's age by the growth stretch (issue #82).
 *
 * <p>The remains the feature places are aged at {@code 10 + random.nextInt(100)}
 * days so they render as the adult skeletons of a long-dead dragon, but the age
 * is written straight into the entity while the render size reads the stretched
 * stage table - under a raised cap every skeleton would come out hatchling
 * sized. Scaling the whole argument keeps the roll's spread over the stretched
 * axis; at the untouched default of 128 days {@link Growth#scaleDays} is the
 * identity and the feature places byte-for-byte vanilla skeletons.
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
}
