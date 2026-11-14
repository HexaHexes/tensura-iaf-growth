package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import com.iafenvoy.iceandfire.item.block.entity.DragonForgeInputBlockEntity;
import com.iafenvoy.iceandfire.registry.IafBlocks;
import dev.architectury.event.EventResult;
import io.github.manasmods.tensura.entity.magic.breath.IceBreathProjectile;
import io.github.manasmods.tensura.event.TensuraSkillEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ported from TenSura Compat: Ice and Fire (MinhEragon), retargeted to
 * IceAndFireCE 2.2 (issue #80): two interactions for Tensura's ice breath -
 *
 * <ul>
 *   <li>HEAD: hitting the ice-forge input advances it (roll out of 32 against
 *       the breath's damage) via {@code onHitWithFlame} and cancels;</li>
 *   <li>TAIL: an air block above the hit may grow the dragon ice spikes -
 *       gated by Tensura's skill-grief events exactly like the original
 *       (PRE may veto, POS observes).</li>
 * </ul>
 *
 * <p>Verified in IceAndFireCE 2.2: {@code IafBlocks.DRAGONFORGE_ICE_INPUT},
 * {@code IafBlocks.DRAGON_ICE_SPIKES} and
 * {@code DragonForgeInputBlockEntity.onHitWithFlame} all present; the grief
 * events and {@code getSkill()} ride the pinned Tensura.</p>
 */
@Mixin(value = IceBreathProjectile.class, remap = true)
public class TensuraIafIceBreathForgeMixin {

    @Inject(method = "applyBlockInteraction", at = @At("HEAD"), cancellable = true)
    private void tensuraIaf$feedDragonforge(Vec3 hit, BlockPos pos, BlockState state,
            CallbackInfo ci) {
        if (!state.is(IafBlocks.DRAGONFORGE_ICE_INPUT)) {
            return;
        }
        IceBreathProjectile self = (IceBreathProjectile) (Object) this;
        if (!(self.level().getBlockEntity(pos) instanceof DragonForgeInputBlockEntity forge)) {
            return;
        }
        if (self.level().getRandom().nextInt(0, 32) < self.getDamage()) {
            forge.onHitWithFlame((double) self.getDamage());
        }
        ci.cancel();
    }

    @Inject(method = "applyBlockInteraction", at = @At("TAIL"))
    private void tensuraIaf$placeIceSpike(Vec3 hit, BlockPos pos, BlockState state,
            CallbackInfo ci) {
        IceBreathProjectile self = (IceBreathProjectile) (Object) this;
        BlockPos above = pos.above();
        if (!self.level().getBlockState(above).isAir()) {
            return;
        }
        BlockState spikes = IafBlocks.DRAGON_ICE_SPIKES.get().defaultBlockState();
        if (!spikes.canSurvive(self.level(), above)) {
            return;
        }
        EventResult veto = TensuraSkillEvents.SKILL_GRIEF_PRE.invoker().grief(
                self.getSkill(), self.level(), self.getOwner(),
                hit.x(), hit.y() + 1, hit.z());
        if (veto.isFalse()) {
            return;
        }
        self.level().setBlockAndUpdate(above, spikes);
        TensuraSkillEvents.SKILL_GRIEF_POS.invoker().grief(
                self.getSkill(), self.level(), self.getOwner(),
                hit.x(), hit.y() + 1, hit.z());
    }
}
