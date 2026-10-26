package org.bitwisemadness.tensuriafgrowth.mixin;

import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.tensura.data.existence.EntityExistenceData;
import io.github.manasmods.tensura.handler.AttributeHandler;
import io.github.manasmods.tensura.storage.ep.IExistence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Forces full mastery on skills granted through a mob's entity_existence data.
 *
 * <p>Tensura learns the skill at mastery 0 and only auto-toggles it when
 * {@code canBeToggled} passes - which for most skills means
 * {@code instance.isMastered(entity)}. Granted skills therefore sat inert on
 * dragons unless something else raised their mastery. Granting max mastery
 * here makes every skill the datapack hands out actually turn on, and keeps
 * the press-only skills (breaths, charm, hydraulic propulsion) inert, since
 * those answer {@code canBeToggled} regardless of mastery.
 */
@Mixin(value = AttributeHandler.class, remap = false)
public final class AttributeHandlerMixin {
    @Redirect(
        method = "initEntityExistence(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/LevelAccessor;Lio/github/manasmods/tensura/storage/ep/IExistence;Lio/github/manasmods/tensura/data/existence/EntityExistenceData;)V",
        at = @At(
            value = "INVOKE",
            target = "Lio/github/manasmods/manascore/skill/api/ManasSkillInstance;canBeToggled(Lnet/minecraft/world/entity/LivingEntity;)Z"
        )
    )
    private static boolean tensura_iaf_growth$forceMasteryOnGrant(ManasSkillInstance instance, LivingEntity entity) {
        instance.setMastery(instance.getMaxMastery());
        return instance.canBeToggled(entity);
    }
}
