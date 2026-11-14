package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import com.iafenvoy.iceandfire.registry.IafMobEffects;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.tensura.ability.skill.resist.ColdNullification;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ported from TenSura Compat: Ice and Fire (MinhEragon), retargeted to
 * IceAndFireCE 2.2 (issue #80): the Cold Nullification resistance skill also
 * lists Ice and Fire's frozen effect among the effects its owner is immune
 * to - at RETURN, the frozen holder joins the skill's immune list.
 *
 * <p>The original read the holder through architectury's registrar; IaF-CE 2.2
 * migrated the effects registry to NeoForge's DeferredRegister, whose holder
 * <em>is</em> a {@link Holder}.</p>
 */
@Mixin(value = ColdNullification.class, remap = true)
public abstract class TensuraIafColdNullificationMixin {

    @Inject(method = "getImmuneEffects", at = @At("RETURN"), cancellable = true)
    private void tensuraIaf$frozenJoinsImmuneList(ManasSkillInstance skill,
            LivingEntity target, CallbackInfoReturnable<List<Holder<MobEffect>>> cir) {
        List<Holder<MobEffect>> effects = new ArrayList<>(cir.getReturnValue());
        effects.add(IafMobEffects.FROZEN);
        cir.setReturnValue(effects);
    }
}
