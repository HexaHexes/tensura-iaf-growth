package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import com.iafenvoy.iceandfire.item.block.entity.DragonForgeInputBlockEntity;
import com.iafenvoy.iceandfire.registry.IafBlocks;
import io.github.manasmods.tensura.entity.magic.breath.ThunderBreathProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ported from TenSura Compat: Ice and Fire (MinhEragon), retargeted to
 * IceAndFireCE 2.2 (issue #80): Tensura's thunder breath feeds Ice and Fire's
 * lightning-forge input - the same roll-and-cancel shape as the flame and ice
 * legs, on {@code IafBlocks.DRAGONFORGE_LIGHTNING_INPUT} (verified present
 * in 2.2 as a DeferredBlock).
 */
@Mixin(value = ThunderBreathProjectile.class, remap = true)
public class TensuraIafThunderBreathForgeMixin {

    @Inject(method = "applyBlockInteraction", at = @At("HEAD"), cancellable = true)
    private void tensuraIaf$feedDragonforge(Vec3 hit, BlockPos pos, BlockState state,
            CallbackInfo ci) {
        if (!state.is(IafBlocks.DRAGONFORGE_LIGHTNING_INPUT)) {
            return;
        }
        ThunderBreathProjectile self = (ThunderBreathProjectile) (Object) this;
        if (!(self.level().getBlockEntity(pos) instanceof DragonForgeInputBlockEntity forge)) {
            return;
        }
        if (self.level().getRandom().nextInt(0, 32) < self.getDamage()) {
            forge.onHitWithFlame((double) self.getDamage());
        }
        ci.cancel();
    }
}
