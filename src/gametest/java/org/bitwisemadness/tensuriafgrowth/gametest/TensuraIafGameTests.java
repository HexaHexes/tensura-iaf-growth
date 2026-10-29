package org.bitwisemadness.tensuriafgrowth.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.tensuriafgrowth.TensuraIafGrowth;
import org.bitwisemadness.tensuriafgrowth.datagen.RebalanceContent;

/**
 * GameTests for the datapack this mod ships.
 *
 * <p>The gametest run stages the full pack stack (see {@code prepareRunMods}),
 * so this is the one environment where "every referenced id exists" is a
 * meaningful assertion - the absent-mod leniency is a <em>datapack</em>
 * property, exercised elsewhere, not here. Keep {@code expectedTests} in
 * {@code build.gradle} in sync with the {@code @GameTest} count, or the
 * harness fails the run.</p>
 */
@GameTestHolder(TensuraIafGrowth.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TensuraIafGameTests {

    private TensuraIafGameTests() {
    }

    /** The mod is loaded and the empty structure template resolves. */
    @GameTest(template = "empty")
    public static void mod_loads(GameTestHelper helper) {
        helper.succeed();
    }

    /**
     * The shipped datapack validates, is packaged on the classpath, and every id
     * it references resolves against the loaded stack.
     *
     * <p>{@link RebalanceContent#build()} runs the whole validation suite (file
     * counts, gates, milestone bounds, target coverage) and throws with the
     * error list, so a table regression fails this test with the exact reason.</p>
     */
    @GameTest(template = "empty")
    public static void datapack_references_resolve(GameTestHelper helper) {
        RebalanceContent.Content content = RebalanceContent.build();

        checkGearMap(content.geGearExistence());
        checkGearMap(content.iafGearExistence());
        checkGearMap(content.tensuraGearExistence());
        checkChains(content.addonChains());
        checkWeaponAttributes(content.weaponAttributes());
        checkEntities(content.entityExistence());

        helper.succeed();
    }

    /** Every generated file must be loadable - the proxy for "wired into the jar". */
    private static void checkGearMap(Map<String, JsonObject> files) {
        for (Map.Entry<String, JsonObject> entry : files.entrySet()) {
            requireResource(entry.getKey());
            JsonObject json = entry.getValue();
            requireItem(json.get("type").getAsString(), entry.getKey() + " type");
            if (json.has("evolution")) {
                requireItem(json.get("evolution").getAsString(), entry.getKey() + " evolution");
            }
        }
    }

    private static void checkChains(Map<String, JsonObject> chains) {
        for (Map.Entry<String, JsonObject> entry : chains.entrySet()) {
            requireResource(entry.getKey());
            JsonObject json = entry.getValue();
            requireItem(json.get("item").getAsString(), entry.getKey() + " item");
            if (json.has("evolution")) {
                requireItem(json.get("evolution").getAsString(), entry.getKey() + " evolution");
            }
            if (json.has("evolutionOptions")) {
                json.getAsJsonArray("evolutionOptions").forEach(option ->
                        requireItem(targetOf(option), entry.getKey() + " fork option"));
            }
        }
    }

    /** Moveset files are keyed by item id - each key must be a real item. */
    private static void checkWeaponAttributes(Map<String, JsonObject> movesets) {
        for (Map.Entry<String, JsonObject> entry : movesets.entrySet()) {
            requireResource(entry.getKey());
            String name = entry.getKey().substring(entry.getKey().lastIndexOf('/') + 1,
                    entry.getKey().length() - ".json".length());
            requireItem("iceandfire:" + name, entry.getKey() + " moveset key");
        }
    }

    private static void checkEntities(Map<String, JsonObject> entities) {
        for (Map.Entry<String, JsonObject> entry : entities.entrySet()) {
            requireResource(entry.getKey());
            String type = entry.getValue().get("type").getAsString();
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(type))) {
                throw new IllegalStateException(entry.getKey() + ": entity type " + type + " is not registered");
            }
        }
    }

    private static String targetOf(JsonElement option) {
        return option.isJsonObject()
                ? option.getAsJsonObject().get("id").getAsString()
                : option.getAsString();
    }

    private static void requireResource(String path) {
        if (TensuraIafGameTests.class.getClassLoader().getResource(path) == null) {
            throw new IllegalStateException(path
                    + " is generated but not on the classpath - src/generated/resources is not wired into the jar");
        }
    }

    private static void requireItem(String id, String label) {
        if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(id))) {
            throw new IllegalStateException(label + ": item " + id + " is not registered");
        }
    }
}
