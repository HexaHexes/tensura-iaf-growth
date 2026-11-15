package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import com.iafenvoy.iceandfire.item.ChainItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ported from TenSura Compat: Ice and Fire (MinhEragon), retargeted to
 * IceAndFireCE 2.2 (issue #80): the chain item refuses entities in the
 * {@code tensura_iaf:cannot_be_chained} tag - the interaction passes before
 * the chain attachment runs. The tag file itself is absorbed data
 * (CompatBaselineContent ships it at the same path).
 *
 * <p>The 2.2 target attaches through {@code ChainData.attachChain}; the
 * injection still fires before that invoke (verified present in 2.2).</p>
 */
@Mixin(value = ChainItem.class, remap = true)
public class TensuraIafChainItemMixin {

    @Inject(method = "interactLivingEntity", at = @At(value = "INVOKE",
            target = "Lcom/iafenvoy/iceandfire/data/component/ChainData;attachChain(Ljava/util/UUID;)V",
            shift = At.Shift.BEFORE), cancellable = true)
    private void tensuraIaf$refuseChainedTargets(ItemStack stack, Player player,
            LivingEntity target, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (target.getType().is(CompatEntityTags.CANNOT_BE_CHAINED)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
