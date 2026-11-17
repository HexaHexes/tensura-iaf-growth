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

    /**
     * The age Ice and Fire's corpse and loot systems should <em>observe</em>:
     * the stretch damped by its square root (issues #87, #88).
     *
     * <p>Ice and Fire keys every corpse decision to the raw age - the skeleton
     * texture at {@code age / 10}, the skeleton loot table and
     * {@code isSkeletal()} at {@code (age / 5) / 2}, drop counts at
     * {@code age / 25} - so at a raised cap the raw axis pays out several
     * times the stock yield and the harvest ladder's ceiling
     * ({@code min(age / 5, 25)}, capped while {@code age / 10} is not) starts
     * refusing interaction outright. The square root of the stretch damps it
     * all: identity at {@link #VANILLA_MAX_AGE} ({@code sqrt(1.0) == 1.0}
     * exactly, so {@code round(age / 1.0)} is bit-for-bit the age), half the
     * axis at a 4x cap, and symmetric below 128.
     */
    public static int dampLootAge(int age) {
        return (int) Math.round(age / Math.sqrt(stretch()));
    }

    /**
     * The canonical death stage for remains of the given age: the single
     * value the skeleton feature places, the skeleton texture reads,
     * {@code isSkeletal()} reports and the dead loot table keys off
     * (issue #88).
     *
     * <p>Three constraints meet here. Ice and Fire's harvest ladder
     * ({@code DragonBaseEntity.mobInteract}) refuses all interaction once
     * {@code deathStage >= min(age / 5, 25)} - the ceiling - while corpses
     * are damage-immune, so that refusal means unharvestable; it opens blood
     * below {@code ceiling / 2} and the heart at {@code ceiling / 2 - 1},
     * which must stay closed for skeletons exactly as stock identity closes
     * them; and it walks the stage up by one per harvest, so the placed stage
     * must start below the ceiling. The texture and loot gates fire at
     * {@code >= age / 10}, which at a stretched cap (age >= 250 at cap 512)
     * outgrows the capped ceiling - no raw value satisfies both sides.
     *
     * <p>So: the damped decay stage clamped into
     * {@code [ceiling / 2, ceiling - 1]} - at or above the flesh windows,
     * below the refuse gate, at every cap up to {@link #MAX_AGE_CAP}. At an
     * untouched 128 the damped stage equals {@code age / 10} and every clamp
     * is a no-op, so the whole corpse axis is byte-for-byte stock.
     */
    public static int corpseStage(int age) {
        int ceiling = Math.min(age / 5, 25);
        int decayed = dampLootAge(age) / 10;
        return Math.max(0, Math.min(Math.max(decayed, ceiling / 2), ceiling - 1));
    }

    /** The share of full EP a dragon of the given stage is worth when slain. */
    public static double epStageFactor(int stage) {
        if (stage <= 1) {
            return EP_STAGE_FACTOR[1];
        }
        return stage >= EP_STAGE_FACTOR.length ? 1.0 : EP_STAGE_FACTOR[stage];
    }
}
