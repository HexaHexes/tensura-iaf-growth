package org.bitwisemadness.tensuriafgrowth.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The rebalanced {@code entity_existence} rows for Ice and Fire's mobs.
 *
 * <p>Written over the {@code tensura_iaf} addon's own files at their exact resource path,
 * so the addon's bands are replaced rather than merged. Three rules decide each row:
 *
 * <ul>
 *   <li>magicule bands come from the approved rebalance table;</li>
 *   <li>aura is whatever the addon shipped - the table only pins a flat 50 for mobs that
 *       had no aura band of their own;</li>
 *   <li>{@code spiritualHealth} and the ability list stay verbatim, apart from the
 *       secondary element the three dragons are granted.</li>
 * </ul>
 *
 * <p>Mobs absent from the rebalance table are copied verbatim.
 */
public final class EntityExistenceContent {
    private EntityExistenceContent() {
    }

    /** Path of the addon's own entity_existence files, which these rows shadow. */
    public static final String PATH = "data/iceandfire/entity_existence/";
    private static final String NAMESPACE = "iceandfire";

    /** Dragons are granted their element's manipulation skill as a secondary pick. */
    private static final String FIRE_DRAGON_SECONDARY = "mysticism:light_manipulation";
    private static final String ICE_DRAGON_SECONDARY = "tensura:water_manipulation";
    private static final String LIGHTNING_DRAGON_SECONDARY = "tensura:wind_manipulation";

    public static Map<String, JsonObject> build() {
        Map<String, JsonObject> files = new LinkedHashMap<>();

        // --- Rebalanced bands -------------------------------------------------

        // The low tier: what a player meets first.
        add(files, "deathworm", 60, 4000, 6000, null, null, null, null);
        add(files, "amphithere", 200, 6500, 7500, 250, 500, null, null);
        add(files, "siren", 200, 7000, 7500, null, null,
                List.of("tensura:water_breathing", "tensura:charm"), null);
        add(files, "troll", 200, 9000, 10500, 250, 500, List.of("tensura:strength"), null);

        // The dread ramp: each form a clear step above the last.
        add(files, "dread_thrall", 120, 4000, 5000, null, null, COLD, null);
        add(files, "dread_horse", 120, 5000, 6000, null, null, COLD, null);
        add(files, "dread_ghoul", 60, 6000, 7000, null, null, COLD, null);
        add(files, "dread_beast", 60, 7000, 8000, null, null, COLD, null);
        add(files, "dread_scuttler", 100, 8000, 9000, null, null, COLD, null);
        add(files, "dread_knight", 100, 12000, 14000, null, null, COLD, null);

        // The mid game.
        add(files, "cyclops", 500, 12000, 14000, 3000, 4000, List.of("tensura:strengthen_body"), null);
        add(files, "sea_serpent", 1000, 24000, 28000, 1000, 2000,
                List.of("tensura:water_breathing", "tensura:hydraulic_propulsion"), null);

        // The end game: one band for all three dragons, each with its secondary element.
        add(files, "fire_dragon", 2000, 240000, 340000, 10000, 10000,
                List.of("tensura:dragon_skin", "tensura:dragon_eye", "tensura:dragon_ear",
                        "tensura:flame_breath", "tensura:flame_attack_resistance", FIRE_DRAGON_SECONDARY),
                null);
        add(files, "ice_dragon", 2000, 240000, 340000, 10000, 10000,
                List.of("tensura:dragon_skin", "tensura:dragon_eye", "tensura:dragon_ear",
                        "tensura:cold_resistance", "tensura:water_attack_resistance", "tensura:ice_breath",
                        ICE_DRAGON_SECONDARY),
                null);
        add(files, "lightning_dragon", 2000, 240000, 340000, 10000, 10000,
                List.of("tensura:dragon_skin", "tensura:dragon_eye", "tensura:dragon_ear",
                        "tensura:thunder_breath", "tensura:wind_attack_resistance",
                        "tensura:electricity_resistance", LIGHTNING_DRAGON_SECONDARY),
                null);

        // --- Verbatim: absent from the rebalance table -------------------------

        add(files, "dread_lich", 500, 140000, 155000, null, null, COLD, null);
        add(files, "cockatrice", 80, 5000, 6000, 1000, 2000,
                List.of("tensura:corrosion"), "tensura:basilisk");
        add(files, "ghost", 100, 2900, 3100, null, null, null, null);
        add(files, "gorgon", 2000, 85000, 91000, 10000, 20000, List.of("tensura:snake_eye"), null);
        add(files, "hippocampus", 100, 4500, 5600, 1000, 1000, List.of("tensura:water_breathing"), null);
        add(files, "hippogryph", 120, 8500, 10000, 1000, 2000, null, null);
        add(files, "hydra", 3000, 85000, 90000, 10000, 20000,
                List.of("tensura:poison_resistance", "tensura:water_attack_resistance",
                        "tensura:poisonous_breath", "tensura:poison"),
                null);
        add(files, "pixie", 100, 2750, 3500, null, null, null, null);
        add(files, "stymphalian_bird", 60, 1500, 2000, 250, 500, null, null);

        return files;
    }

    private static final List<String> COLD = List.of("tensura:cold_resistance");

    /**
     * @param minAura {@code null} when the mob has no aura band of its own; Tensura then
     *     falls back to its default of 50, which is exactly what the rebalance table
     *     asks for.
     */
    private static void add(Map<String, JsonObject> files, String entity, int spiritualHealth, int minMagicule,
            int maxMagicule, Integer minAura, Integer maxAura, List<String> abilities, String evolution) {
        JsonObject json = new JsonObject();
        json.addProperty("type", NAMESPACE + ":" + entity);
        if (abilities != null) {
            JsonArray array = new JsonArray();
            abilities.forEach(array::add);
            json.add("abilities", array);
        }
        if (evolution != null) {
            json.addProperty("evolution", evolution);
        }
        if (minAura != null) {
            json.addProperty("min_aura", minAura);
            json.addProperty("max_aura", maxAura);
        }
        json.addProperty("min_magicule", minMagicule);
        json.addProperty("max_magicule", maxMagicule);
        json.addProperty("spiritualHealth", spiritualHealth);

        String path = PATH + entity + ".json";
        if (files.put(path, json) != null) {
            throw new IllegalStateException("duplicate entity_existence entry: " + path);
        }
    }
}
