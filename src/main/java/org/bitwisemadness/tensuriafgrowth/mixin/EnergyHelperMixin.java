package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import io.github.manasmods.tensura.util.EnergyHelper;
import net.minecraft.world.entity.LivingEntity;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes a dragon's growth stage the biggest EP lever: the EP a kill pays out
 * is scaled by the slain dragon's stage, so a whelpling is worth 15% of a
 * fully grown dragon of the same species instead of the same amount.
 *
 * <p>The multipliers live in {@link Growth#epStageFactor(int)} as plain
 * constants: 0.15 / 0.30 / 0.50 / 0.75 / 1.00 for stages 1 to 5.
 */
@Mixin(value = EnergyHelper.class, remap = false)
public final class EnergyHelperMixin {
    @Inject(
        method = "getEPGain(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;Z)D",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void tensura_iaf_growth$scaleDragonEPGain(
        LivingEntity target,
        LivingEntity attacker,
        boolean applyReduction,
        CallbackInfoReturnable<Double> cir
    ) {
        if (target instanceof DragonBaseEntity dragon) {
            cir.setReturnValue(cir.getReturnValueD() * Growth.epStageFactor(dragon.getDragonStage()));
        }
    }
}
