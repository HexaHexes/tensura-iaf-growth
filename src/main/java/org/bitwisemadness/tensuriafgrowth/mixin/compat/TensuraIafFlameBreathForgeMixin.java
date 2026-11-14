package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import com.iafenvoy.iceandfire.item.block.entity.DragonForgeInputBlockEntity;
import com.iafenvoy.iceandfire.registry.IafBlocks;
import io.github.manasmods.tensura.entity.magic.breath.FlameBreathProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ported from TenSura Compat: Ice and Fire (MinhEragon), retargeted to
 * IceAndFireCE 2.2 (issue #80): Tensura's flame breath feeds Ice and Fire's
 * dragonforge - hitting the fire-forge input advances it (a roll out of 32
 * against the breath's damage) via {@code onHitWithFlame} and cancels the
 * vanilla interaction. A forge input without its block entity falls through
 * to the vanilla behavior, as the original did.
 *
 * <p>Verified in IceAndFireCE 2.2: {@code IafBlocks.DRAGONFORGE_FIRE_INPUT}
 * (a DeferredBlock, which is a Holder) and
 * {@code DragonForgeInputBlockEntity.onHitWithFlame}; {@code getDamage()}
 * rides the pinned Tensura's TensuraProjectile base.</p>
 */
@Mixin(value = FlameBreathProjectile.class, remap = true)
public class TensuraIafFlameBreathForgeMixin {

    @Inject(method = "applyBlockInteraction", at = @At("HEAD"), cancellable = true)
    private void tensuraIaf$feedDragonforge(Vec3 hit, BlockPos pos, BlockState state,
            CallbackInfo ci) {
        if (!state.is(IafBlocks.DRAGONFORGE_FIRE_INPUT)) {
            return;
        }
        FlameBreathProjectile self = (FlameBreathProjectile) (Object) this;
        if (!(self.level().getBlockEntity(pos) instanceof DragonForgeInputBlockEntity forge)) {
            return;
        }
        if (self.level().getRandom().nextInt(0, 32) < self.getDamage()) {
            forge.onHitWithFlame((double) self.getDamage());
        }
        ci.cancel();
    }
}
