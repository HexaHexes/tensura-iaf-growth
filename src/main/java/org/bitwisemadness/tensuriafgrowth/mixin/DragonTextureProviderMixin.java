package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.render.texture.DragonTextureProvider;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps the skeleton texture on the canonical corpse axis (issue #88).
 *
 * <p>{@code getTextureByEntity} serves the skeleton texture while
 * {@code deathStage >= ageInDays / 10} and the flesh ("sleep") texture below
 * it - under a raised cap the raw threshold outruns the damped stage placed
 * by the feature, rendering skeleton-looted remains as fresh flesh. The
 * {@code 10 *} fold makes the caller's {@code age / 10} evaluate to
 * {@link Growth#corpseStage} exactly; at the untouched 128 the helper returns
 * the raw age and the stock comparison stands. Client-only (registered in
 * the config's {@code client} array like the ponder storyboard mixin) and
 * therefore proven by a client launch, not the server GameTest run.
 *
 * <p>The age read is rewritten in place (value only, no receiver), matching
 * the pattern {@code DragonDeadLootTableMixin} uses for its three self-owned
 * call sites.
 */
@Mixin(value = DragonTextureProvider.class, remap = false)
public final class DragonTextureProviderMixin {

    @ModifyExpressionValue(
        method = "getTextureByEntity",
        at = @At(
            value = "INVOKE",
            target = "getAgeInDays()I"
        )
    )
    private int tensura_iaf_growth$observeCorpseStageAge(int ageInDays) {
        return 10 * Growth.corpseStage(ageInDays);
    }
}
