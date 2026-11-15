package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import io.github.manasmods.tensura.race.RaceUtils;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ported from TenSura Compat: Ice and Fire (MinhEragon), retargeted to
 * IceAndFireCE 2.2 (issue #80): a dead or dying dragon corpse counts as
 * non-living for Tensura's race checks - the model-dead state or a positive
 * death stage makes the entity non-living, so possession/kill rules treat the
 * carcass like any other non-living entity. Existing non-living answers are
 * left alone (the answer only ever gains true).
 */
@Mixin(RaceUtils.class)
public class TensuraIafRaceUtilsMixin {

    @Inject(method = "isNonLiving", at = @At("RETURN"), cancellable = true)
    private static void tensuraIaf$dragonsAreNonLivingWhenDead(Entity entity,
            CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }
        if (!(entity instanceof DragonBaseEntity dragon)) {
            return;
        }
        if (dragon.isModelDead() || dragon.getDeathStage() > 0) {
            cir.setReturnValue(true);
        }
    }
}
