package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import com.iafenvoy.iceandfire.entity.GorgonEntity;
import com.iafenvoy.iceandfire.registry.tag.IafEntityTags;
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
 * IceAndFireCE 2.2 (issue #80): the gorgon's stone-gaze check at HEAD treats
 * an entity as already "wearing the blindfold" when it sits in the
 * {@code immune_to_gorgon_stone} tag, or when the owner has the Abnormal
 * Condition Nullification resistance skill toggled.
 *
 * <p>The tag moved packages in 2.2 ({@code registry.tag.IafEntityTags}); the
 * skill check is Tensura-side and unchanged.</p>
 */
@Mixin(value = GorgonEntity.class, remap = true)
public class TensuraIafGorgonStareMixin {

    @Inject(method = "isBlindfolded(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void tensuraIaf$stareImmunity(LivingEntity target,
            CallbackInfoReturnable<Boolean> cir) {
        if (target == null) {
            return;
        }
        if (target.getType().is(IafEntityTags.IMMUNE_TO_GORGON_STONE)) {
            cir.setReturnValue(true);
            return;
        }
        ManasSkill nullifier = ResistanceSkills.ABNORMAL_CONDITION_NULLIFICATION.get();
        if (SkillUtils.isSkillToggled(target, nullifier)) {
            cir.setReturnValue(true);
        }
    }
}
