package org.bitwisemadness.tensuriafgrowth;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.bitwisemadness.tensuriafgrowth.datagen.JsonFilesProvider;
import org.bitwisemadness.tensuriafgrowth.datagen.RebalanceContent;
import org.bitwisemadness.tensuriafgrowth.handler.DragonElementHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for Tensura - Ice and Fire Growth.
 *
 * <p>The mod has no config of its own: every lever it pulls is either a widened
 * I&amp;F config range or a generated datapack file, both shipped from this jar.
 */
@Mod(TensuraIafGrowth.MOD_ID)
public class TensuraIafGrowth {
    public static final String MOD_ID = "tensura_iaf_growth";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public TensuraIafGrowth(IEventBus modBus) {
        // Melee element tagging runs on the game bus: it is a per-hit concern,
        // not a mod-lifecycle one.
        NeoForge.EVENT_BUS.addListener(DragonElementHandler::onLivingAttack);
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
        event.addProvider(new JsonFilesProvider("addon_chains", output, content.addonChains()));
        event.addProvider(new JsonFilesProvider("weapon_attributes", output, content.weaponAttributes()));
        event.addProvider(new JsonFilesProvider("entity_existence", output, content.entityExistence()));
    }
}
