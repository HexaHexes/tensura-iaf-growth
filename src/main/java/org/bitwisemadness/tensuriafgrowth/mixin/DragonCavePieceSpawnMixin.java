package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Scales the cave dragon's spawn age by the growth stretch (issue #82).
 *
 * <p>A dragon cave guardian is rolled at {@code 75 + random.nextInt(50)} days -
 * vanilla's stage 4 to stage 5 adult - but the structure piece builds the
 * dragon straight from {@code EntityType.create} and never passes through
 * {@code finalizeSpawn}, where the spawn roll is already stretched. Without
 * this redirect the age lands on the unscaled axis while {@code getDragonStage}
 * reads the stretched one, and a raised cap turns every cave guardian into a
 * hatchling. The argument scales as a whole, so the roll rides along and the
 * intended adult distribution survives the stretch; at the untouched default
 * of 128 days {@link Growth#scaleDays} is the identity and the structure
 * spawns byte-for-byte vanilla.
 */
@Mixin(
    targets = "com.iafenvoy.iceandfire.world.structure.DragonCaveStructure$DragonCavePiece",
    remap = false
)
public final class DragonCavePieceSpawnMixin {

    @Redirect(
        method = "createDragon",
        at = @At(
            value = "INVOKE",
            target = "Lcom/iafenvoy/iceandfire/entity/DragonBaseEntity;growDragon(I)V"
        )
    )
    private void tensura_iaf_growth$stretchCaveSpawnAge(DragonBaseEntity dragon, int days) {
        dragon.growDragon(Growth.scaleDays(days));
    }
}
