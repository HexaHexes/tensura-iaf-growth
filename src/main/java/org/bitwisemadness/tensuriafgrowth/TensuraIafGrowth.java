package org.bitwisemadness.tensuriafgrowth;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.bitwisemadness.tensuriafgrowth.config.DragonEpConfig;
import org.bitwisemadness.tensuriafgrowth.datagen.JsonFilesProvider;
import org.bitwisemadness.tensuriafgrowth.datagen.RebalanceContent;
import org.bitwisemadness.tensuriafgrowth.handler.DragonElementHandler;
import org.bitwisemadness.tensuriafgrowth.handler.DragonGrowthStatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for Tensura - Ice and Fire Growth.
 *
 * <p>The mod's own config is {@link DragonEpConfig}
 * ({@code config/tensura_iaf_growth-common.toml}, {@code [dragon_ep]}): the
 * stage-banded EP curve dragon existence is pinned to. Everything else it pulls
 * is either a widened I&amp;F config range or a generated datapack file, both
 * shipped from this jar.
 */
@Mod(TensuraIafGrowth.MOD_ID)
public class TensuraIafGrowth {
    public static final String MOD_ID = "tensura_iaf_growth";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public TensuraIafGrowth(IEventBus modBus, ModContainer container) {
        // The stage-banded EP curve lives in this mod's own common config and is
        // read live on every refresh, so world reloads pick up file edits.
        container.registerConfig(ModConfig.Type.COMMON, DragonEpConfig.SPEC);
        // Melee element tagging runs on the game bus: it is a per-hit concern,
        // not a mod-lifecycle one.
        NeoForge.EVENT_BUS.addListener(DragonElementHandler::onLivingAttack);
        // The join-time pin catches spawn rolls and load-time EP before anything
        // else observes them; the growth-mixin tail covers every later change.
        NeoForge.EVENT_BUS.addListener(DragonGrowthStatHandler::onJoin);
        // The rebalanced datapack ships from this jar, and ./gradlew runData is
        // what writes it - there is no generator script left to keep in step.
        modBus.addListener(TensuraIafGrowth::gatherData);
        LOGGER.info("Tensura - Ice and Fire Growth loaded");
    }

    private static void gatherData(GatherDataEvent event) {
        if (!event.includeServer()) {
            return;
        }
        RebalanceContent.Content content = RebalanceContent.build();
        var output = event.getGenerator().getPackOutput();
        event.addProvider(new JsonFilesProvider("gear_existence", output, content.geGearExistence()));
        event.addProvider(new JsonFilesProvider("addon_gear_existence", output, content.iafGearExistence()));
        event.addProvider(new JsonFilesProvider("tensura_gear_existence", output, content.tensuraGearExistence()));
        event.addProvider(new JsonFilesProvider("addon_chains", output, content.addonChains()));
        event.addProvider(new JsonFilesProvider("weapon_attributes", output, content.weaponAttributes()));
        event.addProvider(new JsonFilesProvider("entity_existence", output, content.entityExistence()));
    }
}
