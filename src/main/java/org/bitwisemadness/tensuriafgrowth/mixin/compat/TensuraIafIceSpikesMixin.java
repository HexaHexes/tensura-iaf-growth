package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import com.iafenvoy.iceandfire.item.block.IceSpikesBlock;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.tensura.ability.SkillUtils;
import io.github.manasmods.tensura.registry.skill.ResistanceSkills;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ported from TenSura Compat: Ice and Fire (MinhEragon), retargeted to
 * IceAndFireCE 2.2 (issue #80): stepping on Ice and Fire's dragon ice spikes
 * is cancelled for living entities with any of the four cold/thermal
 * resistance or nullification skills toggled - the vanilla stepOn freeze
 * never applies to them. Non-living entities and unresisted living entities
 * fall through to the original behavior.
 */
@Mixin(value = IceSpikesBlock.class, remap = true)
public class TensuraIafIceSpikesMixin {

    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void tensuraIaf$resistedEntitiesSkipFreeze(Level level, BlockPos pos,
            BlockState state, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        ManasSkill[] resistances = {
                ResistanceSkills.COLD_RESISTANCE.get(),
                ResistanceSkills.COLD_NULLIFICATION.get(),
                ResistanceSkills.THERMAL_FLUCTUATION_RESISTANCE.get(),
                ResistanceSkills.THERMAL_FLUCTUATION_NULLIFICATION.get()
        };
        for (ManasSkill skill : resistances) {
            if (SkillUtils.isSkillToggled(living, skill)) {
                ci.cancel();
                return;
            }
        }
    }
}
