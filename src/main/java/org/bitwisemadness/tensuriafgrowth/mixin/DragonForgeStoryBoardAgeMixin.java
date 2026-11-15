package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.compat.ponder.DragonForgeStoryBoard;
import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Scales the ponder storyboard's display dragon age by the growth stretch
 * (issue #82).
 *
 * <p>The dragonforge story board spawns its display dragon at a fixed 50 days
 * - vanilla's stage 3 adult mid-feed - written straight through
 * {@code setAgeInDays} while the stage table around it is stretched, so a
 * raised cap would show a hatchling where the scene wants an adult. Scaling
 * the literal keeps the scene's stage at any cap and stays byte-for-byte
 * vanilla at the untouched default of 128 days.
 *
 * <p>Client-only: the target class lives in Ice and Fire's ponder package and
 * touches {@code Minecraft.getInstance}, so this mixin is registered in the
 * config's {@code client} array and never applies to a dedicated server.
 */
@Mixin(value = DragonForgeStoryBoard.class, remap = false)
public final class DragonForgeStoryBoardAgeMixin {

    @Redirect(
        method = "lambda$program$0",
        at = @At(
            value = "INVOKE",
            target = "Lcom/iafenvoy/iceandfire/entity/DragonBaseEntity;setAgeInDays(I)V"
        )
    )
    private static void tensura_iaf_growth$stretchPonderDisplayAge(DragonBaseEntity dragon, int days) {
        dragon.setAgeInDays(Growth.scaleDays(days));
    }
}
