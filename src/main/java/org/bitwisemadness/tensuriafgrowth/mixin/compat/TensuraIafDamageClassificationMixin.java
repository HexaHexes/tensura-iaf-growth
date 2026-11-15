package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import com.iafenvoy.iceandfire.registry.IafDamageTypes;
import io.github.manasmods.tensura.damage.TensuraDamageHelper;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ported from TenSura Compat: Ice and Fire (MinhEragon), retargeted to
 * IceAndFireCE 2.2 (issue #80): Tensura's damage classification must see Ice
 * and Fire's own damage types, so elemental resistances and the abnormal/-
 * physical gates work against dragon breath and the gorgon's gaze:
 *
 * <ul>
 *   <li>gorgon gaze -&gt; abnormal condition damage;</li>
 *   <li>dragon ice breath -&gt; cold; dragon fire breath -&gt; fire;
 *       dragon lightning breath -&gt; lightning;</li>
 *   <li>any of the four -&gt; NOT a physical attack (so physical-only
 *       defenses and formulas skip it).</li>
 * </ul>
 *
 * <p>All five inject at HEAD, cancellable - a classified answer short-circuits
 * Tensura's own classification. The four IaF damage-type keys were verified
 * present in IceAndFireCE 2.2.</p>
 */
@Mixin(value = TensuraDamageHelper.class, remap = true)
public class TensuraIafDamageClassificationMixin {

    @Inject(method = "isAbnormal", at = @At("HEAD"), cancellable = true)
    private static void tensuraIaf$gorgonGazeIsAbnormal(DamageSource source,
            CallbackInfoReturnable<Boolean> cir) {
        if (source.is(IafDamageTypes.GORGON_DMG_TYPE)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isCold", at = @At("HEAD"), cancellable = true)
    private static void tensuraIaf$dragonIceIsCold(DamageSource source,
            CallbackInfoReturnable<Boolean> cir) {
        if (source.is(IafDamageTypes.DRAGON_ICE_TYPE)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isFireDamage", at = @At("HEAD"), cancellable = true)
    private static void tensuraIaf$dragonFireIsFire(DamageSource source,
            CallbackInfoReturnable<Boolean> cir) {
        if (source.is(IafDamageTypes.DRAGON_FIRE_TYPE)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isLightningDamage", at = @At("HEAD"), cancellable = true)
    private static void tensuraIaf$dragonLightningIsLightning(DamageSource source,
            CallbackInfoReturnable<Boolean> cir) {
        if (source.is(IafDamageTypes.DRAGON_LIGHTNING_TYPE)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isPhysicalAttack", at = @At("HEAD"), cancellable = true)
    private static void tensuraIaf$iafDamageIsNotPhysical(DamageSource source,
            CallbackInfoReturnable<Boolean> cir) {
        if (source.is(IafDamageTypes.GORGON_DMG_TYPE)
                || source.is(IafDamageTypes.DRAGON_ICE_TYPE)
                || source.is(IafDamageTypes.DRAGON_FIRE_TYPE)
                || source.is(IafDamageTypes.DRAGON_LIGHTNING_TYPE)) {
            cir.setReturnValue(false);
        }
    }
}
