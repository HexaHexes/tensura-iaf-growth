package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import com.iafenvoy.iceandfire.entity.SirenEntity;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.tensura.ability.SkillUtils;
import io.github.manasmods.tensura.registry.skill.ResistanceSkills;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ported from TenSura Compat: Ice and Fire (MinhEragon), retargeted to
 * IceAndFireCE 2.2 (issue #80): the siren's earplug check at RETURN treats an
 * entity with the Spiritual Attack Nullification resistance skill toggled as
 * wearing earplugs - spiritual nullification blocks the charm song the same
 * way vanilla earplugs do. Existing earplug answers are left alone.
 */
@Mixin(value = SirenEntity.class, remap = true)
public class TensuraIafSirenCharmMixin {

    @Inject(method = "isWearingEarplugs(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("RETURN"), cancellable = true)
    private static void tensuraIaf$spiritualNullificationIsEarplugs(LivingEntity target,
            CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }
        ManasSkill nullifier = ResistanceSkills.SPIRITUAL_ATTACK_NULLIFICATION.get();
        if (SkillUtils.isSkillToggled(target, nullifier)) {
            cir.setReturnValue(true);
        }
    }
}
