package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Moves {@code isSkeletal()}'s threshold onto the canonical corpse axis
 * (issue #88), in lockstep with the dead loot table and skeleton texture it
 * mirrors: {@code deathStage >= (ageInDays / 5) / 2} becomes
 * {@code >= corpseStage}, with {@code 10 *} folding the damping into the
 * caller's own integer division exactly as in {@code DragonDeadLootTableMixin}.
 * Without it the male overlay renderer - the only caller - would disagree
 * with the texture and loot the same stage selects.
 *
 * <p>The age read is rewritten in place (value only, no receiver), matching
 * the pattern the loot-table mixin uses for its three self-owned call sites.
 */
@Mixin(value = DragonBaseEntity.class, remap = false)
public final class DragonBaseEntitySkeletalMixin {

    @ModifyExpressionValue(
        method = "isSkeletal",
        at = @At(
            value = "INVOKE",
            target = "getAgeInDays()I"
        )
    )
    private int tensura_iaf_growth$observeCorpseStageAge(int ageInDays) {
        return 10 * Growth.corpseStage(ageInDays);
    }
}
