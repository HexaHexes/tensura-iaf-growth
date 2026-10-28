package org.bitwisemadness.tensuriafgrowth.config;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.bitwisemadness.tensuriafgrowth.TensuraIafGrowth;

/**
 * The stage-banded EP curve a dragon's existence is pinned to - this mod's own
 * config, {@code config/tensura_iaf_growth-common.toml}, section {@code [dragon_ep]}.
 *
 * <p>The six values are the curve's vertices on the age axis: the hatch floor and
 * one cap per growth stage. Read across the stretched stage boundaries
 * ({@code 0 -> 25 -> 50 -> 75 -> 100 -> 125} days, each scaled by
 * {@link Growth#scaleDays}), they form a piecewise-linear line from
 * {@code hatchEpFloor} at day 0 to {@code stage5EpCap} at full growth, so the
 * curve is correct at any configured age cap. The line is the total EP
 * ({@code aura + magicule}); the split is always 1 aura : 9 magicule.
 *
 * <p>Values are read live on every refresh, so a world reload picks up file
 * edits. On read they are validated to be non-decreasing - a stage cap below the
 * stage before it would make the line run backwards - and clamped into order
 * with one warning per distinct bad config.
 */
public final class DragonEpConfig {
    /** Curve vertices in age order; indexes match {@link #curve()}. */
    private static final int HATCH = 0;
    private static final int S1 = 1;
    private static final int S2 = 2;
    private static final int S3 = 3;
    private static final int S4 = 4;
    private static final int S5 = 5;

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue HATCH_EP_FLOOR;
    public static final ModConfigSpec.IntValue STAGE1_EP_CAP;
    public static final ModConfigSpec.IntValue STAGE2_EP_CAP;
    public static final ModConfigSpec.IntValue STAGE3_EP_CAP;
    public static final ModConfigSpec.IntValue STAGE4_EP_CAP;
    public static final ModConfigSpec.IntValue STAGE5_EP_CAP;

    /** The last invalid config that was warned about, so reloads do not spam. */
    private static String lastWarning = "";

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("dragon_ep");
        HATCH_EP_FLOOR = builder.comment(
                "The EP line a dragon starts on (day 0): aura + magicule combined.",
                "Aura and magicule always split it 1 : 9 (hatch = 1,000 aura + 9,000 magicule).")
                .defineInRange("hatchEpFloor", 10_000, 1, Integer.MAX_VALUE);
        STAGE1_EP_CAP = builder.comment(
                "EP line at the end of growth stage 1 - the ceiling while stage 1 lasts.")
                .defineInRange("stage1EpCap", 100_000, 1, Integer.MAX_VALUE);
        STAGE2_EP_CAP = builder.comment(
                "EP line at the end of growth stage 2 - the ceiling while stage 2 lasts.")
                .defineInRange("stage2EpCap", 200_000, 1, Integer.MAX_VALUE);
        STAGE3_EP_CAP = builder.comment(
                "EP line at the end of growth stage 3 - the ceiling while stage 3 lasts.")
                .defineInRange("stage3EpCap", 350_000, 1, Integer.MAX_VALUE);
        STAGE4_EP_CAP = builder.comment(
                "EP line at the end of growth stage 4 - the ceiling while stage 4 lasts.")
                .defineInRange("stage4EpCap", 550_000, 1, Integer.MAX_VALUE);
        STAGE5_EP_CAP = builder.comment(
                "EP line at full growth (stage 5, day 125 x stretch), and the ceiling for",
                "stage 5: as high as this much EP before naming or other top-ups.")
                .defineInRange("stage5EpCap", 800_000, 1, Integer.MAX_VALUE);
        builder.pop();
        SPEC = builder.build();
    }

    private DragonEpConfig() {
    }

    /**
     * The EP line (aura + magicule) at the given age in days: piecewise-linear
     * across the stretched stage windows, clamped at the hatch floor below day 0
     * and at {@code stage5EpCap} after full growth.
     */
    public static double stageLine(int ageDays) {
        double[] pts = curve();
        int[] days = stageBoundaries();
        if (ageDays >= days[5]) {
            return pts[S5];
        }
        for (int i = 0; i < 5; i++) {
            if (ageDays <= days[i + 1]) {
                double span = days[i + 1] - days[i];
                double t = span <= 0.0D ? 1.0D : (ageDays - days[i]) / span;
                return Mth.lerp(t, pts[i], pts[i + 1]);
            }
        }
        return pts[S5];
    }

    /**
     * The EP ceiling while the stage containing the given age lasts: the cap the
     * line reaches at that stage's end. Always {@code >= stageLine(age)} for a
     * validated curve, so it can only ever clip values above the line.
     */
    public static double stageCeiling(int ageDays) {
        double[] pts = curve();
        int[] days = stageBoundaries();
        if (ageDays >= days[4]) {
            return pts[S5];
        }
        if (ageDays >= days[3]) {
            return pts[S4];
        }
        if (ageDays >= days[2]) {
            return pts[S3];
        }
        if (ageDays >= days[1]) {
            return pts[S2];
        }
        return pts[S1];
    }

    /**
     * The six curve vertices, read live from the config and validated
     * non-decreasing: broken values are clamped into order (each raised to at
     * least the vertex before it) with one warning per distinct bad config.
     */
    private static double[] curve() {
        double[] pts = new double[] {
                HATCH_EP_FLOOR.get(),
                STAGE1_EP_CAP.get(),
                STAGE2_EP_CAP.get(),
                STAGE3_EP_CAP.get(),
                STAGE4_EP_CAP.get(),
                STAGE5_EP_CAP.get()
        };
        boolean valid = true;
        for (int i = 1; i < pts.length; i++) {
            if (pts[i] < pts[i - 1]) {
                valid = false;
                pts[i] = pts[i - 1];
            }
        }
        if (!valid) {
            warnOnce(pts);
        }
        return pts;
    }

    /** The stretched day each stage boundary falls on; index 0 is day 0. */
    private static int[] stageBoundaries() {
        return new int[] {
                0,
                Growth.scaleDays(25),
                Growth.scaleDays(50),
                Growth.scaleDays(75),
                Growth.scaleDays(100),
                Growth.scaleDays(125)
        };
    }

    private static void warnOnce(double[] clamped) {
        StringBuilder raw = new StringBuilder();
        raw.append(HATCH_EP_FLOOR.get()).append('/').append(STAGE1_EP_CAP.get()).append('/')
                .append(STAGE2_EP_CAP.get()).append('/').append(STAGE3_EP_CAP.get()).append('/')
                .append(STAGE4_EP_CAP.get()).append('/').append(STAGE5_EP_CAP.get());
        if (raw.toString().equals(lastWarning)) {
            return;
        }
        lastWarning = raw.toString();
        TensuraIafGrowth.LOGGER.warn(
                "[dragon_ep] stage caps must not decrease (hatch/stage1..5 = {}); "
                        + "using clamped values {} for this load - fix the config file",
                raw, clamped[0] + "/" + clamped[1] + "/" + clamped[2] + "/" + clamped[3]
                        + "/" + clamped[4] + "/" + clamped[5]);
    }
}
