package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.jupiter.config.entry.IntegerEntry;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Widens Ice and Fire's {@code maxTamedDragonAge} range from {@code [0, 128]}
 * to {@code [0, 4096]} so the age cap can actually be raised.
 *
 * <p>This is the addon's only config change: it has no config of its own, it
 * just stops I&amp;F from clamping the value that drives every growth hook.
 * The default value stays 128, so nothing changes until the cap is raised.
 */
@Mixin(value = IafCommonConfig.DragonConfig.class, remap = false)
public final class IafCommonConfigDragonMixin {
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/iafenvoy/jupiter/config/entry/IntegerEntry$Builder;range(II)Lcom/iafenvoy/jupiter/config/entry/IntegerEntry$Builder;"
        )
    )
    private IntegerEntry.Builder tensura_iaf_growth$widenMaxTamedDragonAge(IntegerEntry.Builder builder, int min, int max) {
        // Only the maxTamedDragonAge entry uses the (0, 128) pair; every other
        // range in this constructor keeps its own bounds untouched.
        if (min == 0 && max == Growth.VANILLA_MAX_AGE) {
            return builder.range(min, Growth.MAX_AGE_CAP);
        }
        return builder.range(min, max);
    }
}
