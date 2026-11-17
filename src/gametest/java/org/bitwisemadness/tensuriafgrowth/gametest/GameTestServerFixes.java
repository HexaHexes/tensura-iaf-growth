package org.bitwisemadness.tensuriafgrowth.gametest;

import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.bitwisemadness.tensuriafgrowth.TensuraIafGrowth;

/**
 * Pins the GameTestServer's structure position (issue #90).
 *
 * <p>This class lives only in the gametest source set (the jar packages main
 * alone) but registers like any other subscriber, because the dev runs join
 * both source sets into the mod - so it guards on {@link GameTestServer} and
 * leaves normal client, server and datagen runs untouched.
 *
 * <p>Vanilla's {@code GameTestServer.startTests} picks its structure origin
 * from {@code level.random.nextIntBetweenInclusive(-14999992, 14999992)} at
 * y=-59 on an 8-block grid, and that random is created unseeded - a fresh
 * position every run. Both observed flake modes (issue #90) are this lottery:
 * beyond the structure's prepared neighborhood a chunk can exist partial,
 * where a joined entity is never tracked (the world-wide query misses it
 * too), and a structure cell can sit in state whose completion never arrives,
 * leaving its test slot pending forever while the server keeps ticking. With
 * the position pinned, every run takes the same path: a pass stays a pass,
 * and a hang becomes a reproducible one instead of a ~25% burn.
 *
 * <p>The reseed happens on {@link ServerStartedEvent} - after the levels
 * exist, before the first tick, and {@code startTests} runs inside that first
 * tick. The world random is only drawn on the server thread in tick order
 * (worldgen uses chunk-seeded randoms of its own), so the whole sequence from
 * here on is deterministic and the origin draws identically every run. The
 * companion watchdog in {@code build.gradle} fails a stuck run at eight
 * minutes instead of letting it burn to the platform timeout.
 */
@EventBusSubscriber(modid = TensuraIafGrowth.MOD_ID)
public final class GameTestServerFixes {

    /** Whatever this seed's origin draws, it draws every run; tune only if it lands somewhere that hangs. */
    private static final long STRUCTURE_POSITION_SEED = 0L;

    private GameTestServerFixes() {
    }

    /** Re-seeds the overworld random so {@code startTests} draws the same origin every run. */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!(event.getServer() instanceof GameTestServer)) {
            return;
        }
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            overworld.random.setSeed(STRUCTURE_POSITION_SEED);
        }
    }
}
