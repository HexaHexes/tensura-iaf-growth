package org.bitwisemadness.tensuriafgrowth;

import com.iafenvoy.iceandfire.config.IafCommonConfig;

/**
 * The maths shared by the growth mixins.
 *
 * <p>Everything the addon stretches is derived from Ice and Fire's own
 * {@code maxTamedDragonAge} config value, so the stretch factor is
 * {@code f = maxTamedDragonAge / 128}. With the untouched default of 128 that
 * is {@code f = 1} and every hook below is an identity transform - the addon
 * only changes behaviour once the cap is raised (up to {@link #MAX_AGE_CAP},
 * the widened range bound).
 */
public final class Growth {
    /** Upper bound the {@code maxTamedDragonAge} range is widened to. */
    public static final int MAX_AGE_CAP = 4096;

    /** The age Ice and Fire's growth curve was authored against. */
    public static final int VANILLA_MAX_AGE = 128;

    /** EP gained from a dragon kill, indexed by growth stage (1-5). */
    private static final double[] EP_STAGE_FACTOR = new double[] { 0.0, 0.15, 0.30, 0.50, 0.75, 1.00 };

    private Growth() {
    }

    /** The configured age cap; the same value applies to tamed and untamed dragons. */
    public static int maxAge() {
        return IafCommonConfig.INSTANCE.dragon.maxTamedDragonAge.getValue();
    }

    /** The growth stretch factor: 1.0 while the cap sits at its default. */
    public static double stretch() {
        return (double) maxAge() / VANILLA_MAX_AGE;
    }

    /** Scales an age-axis day count by the stretch factor, never below one day. */
    public static int scaleDays(int vanillaDays) {
        return Math.max(1, (int) Math.round(vanillaDays * stretch()));
    }

    /** Scales a double growth divisor by the stretch factor. */
    public static double scaleSpan(double vanillaSpan) {
        return vanillaSpan * stretch();
    }

    /** Scales a float growth divisor by the stretch factor. */
    public static float scaleSpan(float vanillaSpan) {
        return (float) (vanillaSpan * stretch());
    }

    /** The share of full EP a dragon of the given stage is worth when slain. */
    public static double epStageFactor(int stage) {
        if (stage <= 1) {
            return EP_STAGE_FACTOR[1];
        }
        return stage >= EP_STAGE_FACTOR.length ? 1.0 : EP_STAGE_FACTOR[stage];
    }
}
