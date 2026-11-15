package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Scales the roost dragon's spawn age by the growth stretch (issue #82).
 *
 * <p>A roost dragon is rolled at {@code 40 + (12 + random.nextInt(8))} days -
 * vanilla's stage 3 juvenile - assigned directly by the structure piece via
 * {@code EntityType.create} and {@code growDragon}, bypassing the stretched
 * {@code finalizeSpawn} roll. The redirect scales the whole sum, so both the
 * fixed 40-day base and the random roll ride the stretch together: a raised
 * cap keeps spawning juveniles instead of collapsing them to stage 1. At the
 * untouched default of 128 days {@link Growth#scaleDays} is the identity and
 * the structure spawns byte-for-byte vanilla.
 */
@Mixin(
    targets = "com.iafenvoy.iceandfire.world.structure.DragonRoostStructure$DragonRoostPiece",
    remap = false
)
public final class DragonRoostPieceSpawnMixin {

    @Redirect(
        method = "spawnDragon",
        at = @At(
            value = "INVOKE",
            target = "Lcom/iafenvoy/iceandfire/entity/DragonBaseEntity;growDragon(I)V"
        )
    )
    private void tensura_iaf_growth$stretchRoostSpawnAge(DragonBaseEntity dragon, int days) {
        dragon.growDragon(Growth.scaleDays(days));
    }
}
