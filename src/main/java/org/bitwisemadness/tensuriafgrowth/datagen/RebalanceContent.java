package org.bitwisemadness.tensuriafgrowth.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The rebalanced datapack, expressed as data.
 *
 * <p>This class is the single source of truth for every EP chain the pack ships:
 * value tuning happens here rather than by hand-editing JSON, and
 * {@code ./gradlew runData} writes the result into {@code src/generated/resources},
 * which the mod jar then packages. It is a direct port of the retired
 * {@code tools/generate-content.ps1} tables plus the merge rules that let this jar
 * override both Gear Evolution's built-in Ice and Fire chains and the ones the
 * {@code tensura_iaf} addon ships itself.
 *
 * <p>Three gear ladders are emitted, all keyed by the same item ids:
 * <ul>
 *   <li>{@code data/gearevolution/gear_existence/iceandfire/} - read by Gear Evolution
 *       and by Tensura's {@code gear_existence} datapack registry alike;</li>
 *   <li>{@code data/iceandfire/gear_existence/} - shadows the addon's own files at their
 *       exact resource path, so the addon's thresholds can never outvote ours;</li>
 *   <li>{@code data/gearevolution/addon_chains/iceandfire/} - the branching ladder, which
 *       ChainEvolutionResolver consults first and which also works for items Gear
 *       Evolution has no built-in chain for (troll leather, tide guardian).</li>
 * </ul>
 *
 * <p>An item driven by an addon chain gets a gear_existence file only when it needs a
 * Tensura engraving, and that file carries maxEP 0 so Tensura never fights the addon
 * over EP or the evolution target.
 */
public final class RebalanceContent {
    private RebalanceContent() {
    }

    // --- Layout -------------------------------------------------------------

    private static final String NAMESPACE = "iceandfire";
    private static final String GE_GEAR = "data/gearevolution/gear_existence/" + NAMESPACE + "/";
    private static final String IAF_GEAR = "data/" + NAMESPACE + "/gear_existence/";
    private static final String CHAINS = "data/gearevolution/addon_chains/" + NAMESPACE + "/";
    private static final String MOVESETS = "data/" + NAMESPACE + "/weapon_attributes/";
    private static final String ENTITY_PATH = EntityExistenceContent.PATH;

    // --- Chain tables -------------------------------------------------------

    private static final List<String> SLOTS = List.of("helmet", "chestplate", "leggings", "boots");
    private static final List<String> TOOLS = List.of("sword", "axe", "pickaxe", "shovel", "hoe");

    /** The twelve dragon scale sets, grouped by the dragon element that forges them. */
    private static final Map<String, List<String>> SCALE_COLORS_BY_ELEMENT = scaleColorsByElement();

    /** The seven sea serpent (tide guardian) scale sets. */
    private static final List<String> TIDE_COLORS =
            List.of("blue", "bronze", "deepblue", "green", "purple", "red", "teal");

    /** Troll leather -> death worm chitin pairing (mountain / forest / frost). */
    private static final Map<String, String> TROLL_TO_CHITIN = trollToChitin();

    /**
     * EP gates. These are Tensura's own ladder values, so Ice and Fire gear climbs at the
     * same pace as Tensura gear.
     */
    private static final int SHEEP_TO_COPPER = 6000;
    private static final int COPPER_TO_SILVER = 10000;
    private static final int SILVER_TO_TROLL = 18000;
    private static final int TROLL_TO_CHITIN_GATE = 45000;
    private static final int CHITIN_TO_TIDE = 52000;
    private static final int TIDE_TO_SCALE = 80000;
    private static final int SCALE_TO_STEEL = 225000;
    private static final int TERMINAL = 750000;
    private static final int COPPER_TO_SILVER_TOOL = 6000;
    private static final int SILVER_TO_DRAGONBONE = 18000;
    private static final int DRAGONBONE_BRANCH = 52000;
    private static final int DREAD_KNIGHT = 18000;
    private static final int DREAD_QUEEN = 52000;
    private static final int GAUNTLET_WHITE = 18000;
    private static final int GAUNTLET_RED = 52000;

    private static final double EP_GAIN_EVOLVING = 0.02;
    private static final double EP_GAIN_TERMINAL = 0.003;

    /**
     * Engravings granted by Tensura when the item is created, mirroring what Tensura puts
     * on its own gear at the same rung (silver = holy coat I, mithril tier = holy coat II,
     * hihiirokane tier = tsukumogami).
     */
    private static final JsonObject HOLY_COAT_1 = engravings("tensura:holy_coat", 1);
    private static final JsonObject HOLY_COAT_2 = engravings("tensura:holy_coat", 2);
    private static final JsonObject ELEMENTAL_BOOST = engravings("tensura:elemental_boost", 1);
    private static final JsonObject ELEMENTAL_RESISTANCE = engravings("tensura:elemental_resistance", 1);
    private static final JsonObject BREATHING_SUPPORT = engravings("tensura:breathing_support", 1);
    private static final JsonObject TSUKUMOGAMI = engravings("tensura:tsukumogami", 1);

    /**
     * Unique Ice and Fire weapons and accessories with no upgrade line: they stay usable
     * endgame and keep earning EP.
     */
    private static final List<String> TERMINAL_UNIQUES = List.of(
            "amphithere_macuahuitl",
            "blindfold",
            "cockatrice_scepter",
            "cyclops_eye",
            "earplugs",
            "fishing_spear",
            "ghost_sword",
            "gorgon_head",
            "hippocampus_slapper",
            "hippogryph_sword",
            "lich_staff",
            "pixie_wand",
            "siren_flute",
            "stymphalian_bird_dagger",
            "stymphalian_feather_bundle",
            "tide_trident");

    /**
     * BetterCombat movesets: I&amp;F weapons inherit the moveset of a Tensura-style weapon
     * class. Only parents Tensura itself ships are used.
     */
    private static final Map<String, List<String>> MOVESET_PARENTS = movesetParents();

    // --- Merge tables -------------------------------------------------------

    /**
     * Gear Evolution ships built-in gear_existence files for these 17 items, and an inert
     * file of our own at the same resource path replaces its thresholds outright. Each one
     * is driven by an addon_chains ladder instead.
     */
    private static final List<String> GE_BUILTIN_NEUTRALISED = neutralised();

    /**
     * The 32 files {@code tensura_iaf} ships under {@code data/iceandfire/gear_existence/}
     * that this pack has no gear_existence file of its own for. All of them still need a
     * shadow copy, or the addon's thresholds would outvote the addon_chains ladder.
     */
    private static final List<String> ADDON_ONLY = List.of(
            "deathworm_red_boots", "deathworm_red_chestplate", "deathworm_red_helmet", "deathworm_red_leggings",
            "deathworm_white_boots", "deathworm_white_chestplate", "deathworm_white_helmet", "deathworm_white_leggings",
            "dragonbone_axe", "dragonbone_hoe", "dragonbone_pickaxe", "dragonbone_shovel", "dragonbone_sword",
            "forest_troll_leather_boots", "forest_troll_leather_chestplate", "forest_troll_leather_helmet",
            "forest_troll_leather_leggings",
            "frost_troll_leather_boots", "frost_troll_leather_chestplate", "frost_troll_leather_helmet",
            "frost_troll_leather_leggings",
            "mountain_troll_leather_boots", "mountain_troll_leather_chestplate", "mountain_troll_leather_helmet",
            "mountain_troll_leather_leggings",
            "troll_weapon_axe", "troll_weapon_column", "troll_weapon_column_forest", "troll_weapon_column_frost",
            "troll_weapon_hammer", "troll_weapon_trunk", "troll_weapon_trunk_frost");

    /** The troll weapons have no upgrade line: they stay usable endgame. */
    private static final List<String> ADDON_TROLL_WEAPONS = List.of(
            "troll_weapon_axe", "troll_weapon_column", "troll_weapon_column_forest", "troll_weapon_column_frost",
            "troll_weapon_hammer", "troll_weapon_trunk", "troll_weapon_trunk_frost");

    /** Our own gear_existence files the addon does not ship, so no shadow copy is needed. */
    private static final Set<String> NOT_IN_ADDON = Set.of(
            "armor_copper_metal_boots", "armor_copper_metal_chestplate", "armor_copper_metal_helmet",
            "armor_copper_metal_leggings",
            "armor_silver_metal_boots", "armor_silver_metal_chestplate", "armor_silver_metal_helmet",
            "armor_silver_metal_leggings",
            "blindfold", "copper_axe", "copper_hoe", "copper_pickaxe", "copper_shovel", "copper_sword",
            "cyclops_eye", "deathworm_gauntlet_red", "deathworm_gauntlet_white", "deathworm_gauntlet_yellow",
            "dread_knight_sword", "earplugs", "fishing_spear", "gorgon_head",
            "sheep_boots", "sheep_chestplate", "sheep_helmet", "sheep_leggings",
            "silver_axe", "silver_hoe", "silver_pickaxe", "silver_shovel", "silver_sword",
            "siren_flute", "stymphalian_feather_bundle");

    /**
     * Every item Gear Evolution ships a built-in Ice and Fire chain for. Each one must be
     * covered by a gear_existence or an addon_chains file, or Gear Evolution's own
     * thresholds would silently win.
     */
    private static final List<String> GE_BUILTIN_ITEMS = geBuiltinItems();

    // --- Output -------------------------------------------------------------

    /** Every file the pack ships, grouped by the ladder it belongs to. */
    public record Content(
            Map<String, JsonObject> geGearExistence,
            Map<String, JsonObject> iafGearExistence,
            Map<String, JsonObject> addonChains,
            Map<String, JsonObject> weaponAttributes,
            Map<String, JsonObject> entityExistence) {
    }

    public static Content build() {
        Map<String, JsonObject> ourGear = new LinkedHashMap<>();
        Map<String, JsonObject> chains = new LinkedHashMap<>();
        Map<String, JsonObject> movesets = new LinkedHashMap<>();

        addArmorChain(ourGear, chains);
        addToolChain(ourGear, chains);
        addSingularGear(ourGear);
        addMovesets(movesets);

        // Gear Evolution's built-in chains, neutralised at their own resource path.
        Map<String, JsonObject> geGear = new LinkedHashMap<>(ourGear);
        for (String item : GE_BUILTIN_NEUTRALISED) {
            put(geGear, GE_GEAR + file(item), inert(item));
        }

        // The addon's file set, reissued with our values: shared files keep our content,
        // the rest are either inert (the addon_chains ladder drives them) or terminal.
        Map<String, JsonObject> iafGear = new LinkedHashMap<>();
        for (String path : ourGear.keySet()) {
            String item = name(path);
            if (!NOT_IN_ADDON.contains(item)) {
                put(iafGear, IAF_GEAR + file(item), ourGear.get(path));
            }
        }
        for (String item : ADDON_ONLY) {
            put(iafGear, IAF_GEAR + file(item),
                    ADDON_TROLL_WEAPONS.contains(item) ? terminal(item) : inert(item));
        }

        Content content = new Content(geGear, iafGear, chains, movesets, EntityExistenceContent.build());
        validate(content);
        return content;
    }

    // --- Chain 1: armor -----------------------------------------------------
    //
    // sheep disguise -> copper -> silver -> troll leather -> death worm chitin ->
    // sea serpent scale -> dragon scale -> dragon steel

    private static void addArmorChain(Map<String, JsonObject> gear, Map<String, JsonObject> chains) {
        for (String slot : SLOTS) {
            // Sheep disguise -> copper.
            put(gear, gearPath(id("sheep_" + slot)),
                    gear(id("sheep_" + slot), 0, SHEEP_TO_COPPER, EP_GAIN_EVOLVING, null,
                            id("armor_copper_metal_" + slot)));

            // Copper -> silver.
            put(gear, gearPath(id("armor_copper_metal_" + slot)),
                    gear(id("armor_copper_metal_" + slot), 0, COPPER_TO_SILVER, EP_GAIN_EVOLVING, null,
                            id("armor_silver_metal_" + slot)));

            // Silver -> branch into the three troll leather sets. The engraving lives in
            // gear_existence (Tensura reads it) with maxEP 0 so Tensura does not manage EP
            // or a target here; the addon chain is authoritative for both.
            put(gear, gearPath(id("armor_silver_metal_" + slot)),
                    gear(id("armor_silver_metal_" + slot), 0, 0, 0, HOLY_COAT_1, null));
            put(chains, chainPath(id("armor_silver_metal_" + slot)),
                    chain(id("armor_silver_metal_" + slot), 0, SILVER_TO_TROLL, EP_GAIN_EVOLVING, null,
                            TROLL_TO_CHITIN.keySet().stream().map(troll -> id(troll + "_troll_leather_" + slot)).toList()));

            // Troll leather -> its paired death worm chitin (deterministic pairing).
            for (Map.Entry<String, String> pairing : TROLL_TO_CHITIN.entrySet()) {
                String troll = pairing.getKey();
                put(chains, chainPath(id(troll + "_troll_leather_" + slot)),
                        chain(id(troll + "_troll_leather_" + slot), 0, TROLL_TO_CHITIN_GATE, EP_GAIN_EVOLVING,
                                id("deathworm_" + pairing.getValue() + "_" + slot), null));
            }

            // Death worm chitin (all three, one tier) -> any of the seven sea serpent sets.
            for (String chitin : List.of("yellow", "white", "red")) {
                put(chains, chainPath(id("deathworm_" + chitin + "_" + slot)),
                        chain(id("deathworm_" + chitin + "_" + slot), 0, CHITIN_TO_TIDE, EP_GAIN_EVOLVING, null,
                                ids(TIDE_COLORS, "_" + slot, "tide_")));
            }

            // Sea serpent scale -> any of the twelve dragon scale sets.
            for (String tide : TIDE_COLORS) {
                put(gear, gearPath(id("tide_" + tide + "_" + slot)),
                        gear(id("tide_" + tide + "_" + slot), 0, 0, 0, BREATHING_SUPPORT, null));
                put(chains, chainPath(id("tide_" + tide + "_" + slot)),
                        chain(id("tide_" + tide + "_" + slot), 0, TIDE_TO_SCALE, EP_GAIN_EVOLVING, null,
                                scaleArmorIds(slot)));
            }

            // Dragon scale -> the dragon steel set of its element (deterministic).
            for (Map.Entry<String, List<String>> element : SCALE_COLORS_BY_ELEMENT.entrySet()) {
                for (String color : element.getValue()) {
                    put(gear, gearPath(id("armor_" + color + "_" + slot)),
                            gear(id("armor_" + color + "_" + slot), 0, SCALE_TO_STEEL, EP_GAIN_EVOLVING,
                                    ELEMENTAL_RESISTANCE, id("dragonsteel_" + element.getKey() + "_" + slot)));
                }
            }

            // Dragon steel: top of the ladder, keeps earning, engraved like mithril.
            for (String element : SCALE_COLORS_BY_ELEMENT.keySet()) {
                put(gear, gearPath(id("dragonsteel_" + element + "_" + slot)),
                        gear(id("dragonsteel_" + element + "_" + slot), 0, TERMINAL, EP_GAIN_TERMINAL,
                                HOLY_COAT_2, null));
            }
        }
    }

    // --- Chain 2: tools and weapons ----------------------------------------
    //
    // copper -> silver -> dragon bone -> blooded dragon bone (swords) -> dragon steel

    private static void addToolChain(Map<String, JsonObject> gear, Map<String, JsonObject> chains) {
        for (String tool : TOOLS) {
            // Copper -> silver.
            put(gear, gearPath(id("copper_" + tool)),
                    gear(id("copper_" + tool), 0, COPPER_TO_SILVER_TOOL, EP_GAIN_EVOLVING, null,
                            id("silver_" + tool)));

            // Silver -> dragon bone (deterministic), with the holy coat engraving.
            put(gear, gearPath(id("silver_" + tool)),
                    gear(id("silver_" + tool), 0, SILVER_TO_DRAGONBONE, EP_GAIN_EVOLVING, HOLY_COAT_1,
                            id("dragonbone_" + tool)));

            if (tool.equals("sword")) {
                // Dragon bone sword -> a blooded (elemental) dragon bone sword, rolled at
                // random; each blooded sword then forges into its matching dragon steel.
                put(chains, chainPath(id("dragonbone_sword")),
                        chain(id("dragonbone_sword"), 0, DRAGONBONE_BRANCH, EP_GAIN_EVOLVING, null,
                                SCALE_COLORS_BY_ELEMENT.keySet().stream()
                                        .map(element -> id("dragonbone_sword_" + element)).toList()));

                for (String element : SCALE_COLORS_BY_ELEMENT.keySet()) {
                    put(gear, gearPath(id("dragonbone_sword_" + element)),
                            gear(id("dragonbone_sword_" + element), 0, SCALE_TO_STEEL, EP_GAIN_EVOLVING,
                                    ELEMENTAL_BOOST, id("dragonsteel_" + element + "_sword")));
                }
            } else {
                // Tools have no blooded tier: dragon bone rolls straight into one of the
                // three dragon steel elements.
                put(chains, chainPath(id("dragonbone_" + tool)),
                        chain(id("dragonbone_" + tool), 0, DRAGONBONE_BRANCH, EP_GAIN_EVOLVING, null,
                                SCALE_COLORS_BY_ELEMENT.keySet().stream()
                                        .map(element -> id("dragonsteel_" + element + "_" + tool)).toList()));
            }

            // Dragon steel tools and weapons: terminal, engraved like hihiirokane.
            for (String element : SCALE_COLORS_BY_ELEMENT.keySet()) {
                put(gear, gearPath(id("dragonsteel_" + element + "_" + tool)),
                        gear(id("dragonsteel_" + element + "_" + tool), 0, TERMINAL, EP_GAIN_TERMINAL,
                                TSUKUMOGAMI, null));
            }
        }

        // Dragon bone bow: no dragon steel bow exists, so it tops out on its own.
        put(gear, gearPath(id("dragonbone_bow")),
                gear(id("dragonbone_bow"), 0, TERMINAL, EP_GAIN_TERMINAL, null, null));
    }

    // --- Single-item ladders ------------------------------------------------

    private static void addSingularGear(Map<String, JsonObject> gear) {
        // Death worm gauntlets: yellow -> white -> red, then terminal.
        put(gear, gearPath(id("deathworm_gauntlet_yellow")),
                gear(id("deathworm_gauntlet_yellow"), 0, GAUNTLET_WHITE, EP_GAIN_EVOLVING, null,
                        id("deathworm_gauntlet_white")));
        put(gear, gearPath(id("deathworm_gauntlet_white")),
                gear(id("deathworm_gauntlet_white"), 0, GAUNTLET_RED, EP_GAIN_EVOLVING, null,
                        id("deathworm_gauntlet_red")));
        put(gear, gearPath(id("deathworm_gauntlet_red")),
                gear(id("deathworm_gauntlet_red"), 0, TERMINAL, EP_GAIN_TERMINAL, null, null));

        // Dread line: Dread Thrall's Sword -> Dread Knight's Sword -> Blade of the
        // Dreaded Queen.
        put(gear, gearPath(id("dread_sword")),
                gear(id("dread_sword"), 0, DREAD_KNIGHT, EP_GAIN_EVOLVING, null, id("dread_knight_sword")));
        put(gear, gearPath(id("dread_knight_sword")),
                gear(id("dread_knight_sword"), 0, DREAD_QUEEN, EP_GAIN_EVOLVING, null, id("dread_queen_sword")));
        put(gear, gearPath(id("dread_queen_sword")),
                gear(id("dread_queen_sword"), 0, TERMINAL, EP_GAIN_TERMINAL, null, null));
        put(gear, gearPath(id("dread_queen_staff")),
                gear(id("dread_queen_staff"), 0, TERMINAL, EP_GAIN_TERMINAL, null, null));

        // Uniques without an upgrade line stay terminal.
        for (String unique : TERMINAL_UNIQUES) {
            put(gear, gearPath(id(unique)), gear(id(unique), 0, TERMINAL, EP_GAIN_TERMINAL, null, null));
        }
    }

    private static void addMovesets(Map<String, JsonObject> movesets) {
        for (Map.Entry<String, List<String>> parent : MOVESET_PARENTS.entrySet()) {
            for (String item : parent.getValue()) {
                JsonObject json = new JsonObject();
                json.addProperty("parent", parent.getKey());
                put(movesets, MOVESETS + item + ".json", json);
            }
        }
    }

    // --- Merge helpers ------------------------------------------------------

    /**
     * Replaces Tensura's EP management for an item the addon_chains ladder drives.
     *
     * @param item the bare Ice and Fire item name, without its namespace
     */
    private static JsonObject inert(String item) {
        return gear(id(item), 0, 0, 0, null, null);
    }

    /**
     * A plain top-of-the-ladder rung for items with no upgrade line.
     *
     * @param item the bare Ice and Fire item name, without its namespace
     */
    private static JsonObject terminal(String item) {
        return gear(id(item), 0, TERMINAL, EP_GAIN_TERMINAL, null, null);
    }

    // --- File builders ------------------------------------------------------

    private static JsonObject gear(String item, int minEP, int maxEP, double epGain, JsonObject engravings,
            String evolution) {
        JsonObject json = new JsonObject();
        json.addProperty("type", item);
        json.addProperty("minEP", minEP);
        json.addProperty("maxEP", maxEP);
        json.addProperty("epGain", number(epGain));
        if (engravings != null) {
            json.add("engravings", engravings);
        }
        if (evolution != null) {
            json.addProperty("evolution", evolution);
        }
        return json;
    }

    private static JsonObject chain(String item, int minEP, int maxEP, double epGain, String evolution,
            List<String> options) {
        if (evolution == null == (options == null)) {
            throw new IllegalStateException("addon chain " + item + " needs exactly one of evolution or options");
        }
        JsonObject json = new JsonObject();
        json.addProperty("item", item);
        json.addProperty("minEP", minEP);
        json.addProperty("maxEP", maxEP);
        json.addProperty("epGain", number(epGain));
        if (evolution != null) {
            json.addProperty("evolution", evolution);
        } else {
            JsonArray array = new JsonArray();
            options.forEach(array::add);
            json.add("evolutionOptions", array);
        }
        return json;
    }

    /**
     * Writes 0 as an integer rather than {@code 0.0}: the packs these files replace all
     * spell a zero rate that way, and a JSON-semantics diff should not have to care.
     */
    private static Number number(double value) {
        return value == 0.0d ? Integer.valueOf(0) : (Number) Double.valueOf(value);
    }

    private static JsonObject engravings(String enchantment, int level) {
        JsonObject json = new JsonObject();
        json.addProperty(enchantment, level);
        return json;
    }

    // --- Validation ---------------------------------------------------------

    private static void validate(Content content) {
        List<String> errors = new ArrayList<>();

        expect(errors, "gear_existence files under data/gearevolution", 169, content.geGearExistence.size());
        expect(errors, "gear_existence files under data/iceandfire", 151, content.iafGearExistence.size());
        expect(errors, "addon_chains files", 61, content.addonChains.size());
        expect(errors, "weapon_attributes files", 37, content.weaponAttributes.size());
        expect(errors, "entity_existence files", 24, content.entityExistence.size());
        expect(errors, "neutralised Gear Evolution built-ins", 17, GE_BUILTIN_NEUTRALISED.size());
        expect(errors, "files the addon ships that we shadow", 32, ADDON_ONLY.size());
        expect(errors, "our files the addon does not ship", 33, NOT_IN_ADDON.size());

        List<Map<String, JsonObject>> all = List.of(content.geGearExistence, content.iafGearExistence,
                content.addonChains, content.weaponAttributes, content.entityExistence);

        // Every file must land under the path the consuming mod reads it from - the
        // same item legitimately appears under two of them.
        checkPrefix(errors, content.geGearExistence, GE_GEAR);
        checkPrefix(errors, content.iafGearExistence, IAF_GEAR);
        checkPrefix(errors, content.addonChains, CHAINS);
        checkPrefix(errors, content.weaponAttributes, MOVESETS);
        checkPrefix(errors, content.entityExistence, ENTITY_PATH);

        // File names must be clean: the old hand-made pack shipped a file with a space
        // before .json, which silently broke that entry.
        for (Map<String, JsonObject> files : all) {
            for (String path : files.keySet()) {
                if (!path.endsWith(".json") || !name(path).matches("[a-z0-9_]+")) {
                    errors.add("bad file name: " + path);
                }
            }
        }

        // The item id in every file must carry its namespace: Tensura matches it against
        // the item's registry name, and an unqualified id resolves to minecraft:<name>,
        // which would silently match nothing.
        for (Map<String, JsonObject> files : all) {
            for (Map.Entry<String, JsonObject> entry : files.entrySet()) {
                JsonObject json = entry.getValue();
                for (String field : new String[] { "type", "item" }) {
                    if (json.has(field) && !json.get(field).getAsString().startsWith(NAMESPACE + ":")) {
                        errors.add(entry.getKey() + " -> " + field + " is missing its namespace");
                    }
                }
            }
        }

        // Every evolution target must itself be a generated item (catches typos).
        Set<String> generated = new LinkedHashSet<>();
        for (String path : content.geGearExistence.keySet()) {
            generated.add(id(name(path)));
        }
        for (String path : content.addonChains.keySet()) {
            generated.add(id(name(path)));
        }
        for (Map<String, JsonObject> files : List.of(content.geGearExistence, content.iafGearExistence,
                content.addonChains)) {
            for (Map.Entry<String, JsonObject> entry : files.entrySet()) {
                JsonObject json = entry.getValue();
                if (json.has("evolution")) {
                    checkTarget(errors, entry.getKey(), json.get("evolution").getAsString(), generated);
                }
                if (json.has("evolutionOptions")) {
                    for (com.google.gson.JsonElement option : json.getAsJsonArray("evolutionOptions")) {
                        checkTarget(errors, entry.getKey(), option.getAsString(), generated);
                    }
                }
            }
        }

        // Every item Gear Evolution ships a built-in chain for must be covered, or Gear
        // Evolution's own thresholds would silently win.
        for (String item : GE_BUILTIN_ITEMS) {
            if (!generated.contains(id(item))) {
                errors.add("Gear Evolution ships a built-in chain for '" + item
                        + "' but this pack generates no file for it");
            }
        }

        // Structural spot checks - the counts a future edit can silently break.
        expect(errors, "dragon scale gear_existence files", 48,
                (int) content.geGearExistence.keySet().stream().filter(RebalanceContent::isScale).count());
        expect(errors, "tide addon chains", 28,
                (int) content.addonChains.keySet().stream().filter(path -> name(path).startsWith("tide_")).count());
        expect(errors, "troll addon chains", 12,
                (int) content.addonChains.keySet().stream()
                        .filter(path -> name(path).matches("(mountain|forest|frost)_troll_leather_.*")).count());

        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                    "Rebalanced datapack failed validation:\n  " + String.join("\n  ", errors));
        }
    }

    private static void checkTarget(List<String> errors, String path, String target, Set<String> generated) {
        if (!generated.contains(target)) {
            errors.add(path + " -> unknown evolution target '" + target + "'");
        }
    }

    private static void expect(List<String> errors, String what, int wanted, int found) {
        if (wanted != found) {
            errors.add("expected " + wanted + " " + what + ", found " + found);
        }
    }

    private static void checkPrefix(List<String> errors, Map<String, JsonObject> files, String prefix) {
        for (String path : files.keySet()) {
            if (!path.startsWith(prefix)) {
                errors.add("file written outside " + prefix + ": " + path);
            }
        }
    }

    private static boolean isScale(String path) {
        String name = name(path);
        for (List<String> colors : SCALE_COLORS_BY_ELEMENT.values()) {
            for (String color : colors) {
                for (String slot : SLOTS) {
                    if (name.equals("armor_" + color + "_" + slot)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // --- Small helpers ------------------------------------------------------

    private static void put(Map<String, JsonObject> files, String path, JsonObject json) {
        if (files.put(path, json) != null) {
            throw new IllegalStateException("duplicate entry: " + path);
        }
    }

    private static String gearPath(String item) {
        return GE_GEAR + file(item);
    }

    private static String chainPath(String item) {
        return CHAINS + file(item);
    }

    private static String file(String item) {
        return item.substring(item.indexOf(':') + 1) + ".json";
    }

    private static String name(String path) {
        return path.substring(path.lastIndexOf('/') + 1, path.length() - ".json".length());
    }

    private static String id(String path) {
        return NAMESPACE + ":" + path;
    }

    /** {@code id("iceandfire:" + prefix + color + suffix)} for every value in {@code values}. */
    private static List<String> ids(List<String> values, String suffix, String prefix) {
        List<String> items = new ArrayList<>(values.size());
        for (String value : values) {
            items.add(id(prefix + value + suffix));
        }
        return items;
    }

    /** Every dragon scale set, flattened in element then colour order. */
    private static List<String> scaleColors() {
        List<String> items = new ArrayList<>();
        for (List<String> colors : SCALE_COLORS_BY_ELEMENT.values()) {
            for (String color : colors) {
                items.add(color);
            }
        }
        return items;
    }

    /** All twelve dragon scale armor pieces of one slot, as evolution targets. */
    private static List<String> scaleArmorIds(String slot) {
        List<String> items = new ArrayList<>();
        for (String color : scaleColors()) {
            items.add(id("armor_" + color + "_" + slot));
        }
        return items;
    }

    private static Map<String, List<String>> scaleColorsByElement() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("fire", List.of("red", "green", "bronze", "gray"));
        map.put("ice", List.of("blue", "white", "sapphire", "silver"));
        map.put("lightning", List.of("electric", "amethyst", "copper", "black"));
        return map;
    }

    private static Map<String, String> trollToChitin() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("mountain", "red");
        map.put("forest", "yellow");
        map.put("frost", "white");
        return map;
    }

    private static List<String> neutralised() {
        List<String> items = new ArrayList<>();
        for (String color : List.of("red", "white", "yellow")) {
            for (String slot : SLOTS) {
                items.add("deathworm_" + color + "_" + slot);
            }
        }
        items.addAll(TOOLS.stream().map(tool -> "dragonbone_" + tool).toList());
        return List.copyOf(items);
    }

    private static Map<String, List<String>> movesetParents() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("bettercombat:sword", List.of(
                "copper_sword", "silver_sword", "dragonbone_sword",
                "dragonbone_sword_fire", "dragonbone_sword_ice", "dragonbone_sword_lightning",
                "dragonsteel_fire_sword", "dragonsteel_ice_sword", "dragonsteel_lightning_sword",
                "hippogryph_sword", "amphithere_macuahuitl"));
        map.put("bettercombat:claymore", List.of("dread_sword", "dread_knight_sword", "dread_queen_sword"));
        map.put("bettercombat:rapier", List.of("ghost_sword"));
        map.put("bettercombat:heavy_axe", List.of(
                "copper_axe", "silver_axe", "dragonbone_axe",
                "dragonsteel_fire_axe", "dragonsteel_ice_axe", "dragonsteel_lightning_axe"));
        map.put("bettercombat:pickaxe", List.of(
                "copper_pickaxe", "silver_pickaxe", "dragonbone_pickaxe",
                "dragonsteel_fire_pickaxe", "dragonsteel_ice_pickaxe", "dragonsteel_lightning_pickaxe"));
        map.put("bettercombat:fist", List.of(
                "deathworm_gauntlet_yellow", "deathworm_gauntlet_white", "deathworm_gauntlet_red",
                "hippocampus_slapper"));
        map.put("bettercombat:spear", List.of("fishing_spear"));
        map.put("bettercombat:trident", List.of("tide_trident"));
        map.put("bettercombat:staff", List.of("lich_staff", "dread_queen_staff", "cockatrice_scepter"));
        map.put("bettercombat:dagger", List.of("stymphalian_bird_dagger"));
        return map;
    }

    private static List<String> geBuiltinItems() {
        List<String> items = new ArrayList<>();
        items.add("amphithere_macuahuitl");
        for (List<String> colors : SCALE_COLORS_BY_ELEMENT.values()) {
            for (String color : colors) {
                for (String slot : SLOTS) {
                    items.add("armor_" + color + "_" + slot);
                }
            }
        }
        for (String slot : SLOTS) {
            items.add("armor_copper_metal_" + slot);
            items.add("armor_silver_metal_" + slot);
            items.add("sheep_" + slot);
        }
        items.add("blindfold");
        items.add("cockatrice_scepter");
        items.addAll(TOOLS.stream().map(tool -> "copper_" + tool).toList());
        items.add("cyclops_eye");
        for (String color : List.of("yellow", "white", "red")) {
            items.add("deathworm_gauntlet_" + color);
            for (String slot : SLOTS) {
                items.add("deathworm_" + color + "_" + slot);
            }
        }
        items.addAll(TOOLS.stream().map(tool -> "dragonbone_" + tool).toList());
        items.add("dragonbone_bow");
        for (String element : SCALE_COLORS_BY_ELEMENT.keySet()) {
            items.add("dragonbone_sword_" + element);
            for (String tool : TOOLS) {
                items.add("dragonsteel_" + element + "_" + tool);
            }
            for (String slot : SLOTS) {
                items.add("dragonsteel_" + element + "_" + slot);
            }
        }
        items.add("dread_knight_sword");
        items.add("dread_queen_staff");
        items.add("dread_queen_sword");
        items.add("dread_sword");
        items.add("earplugs");
        items.add("fishing_spear");
        items.add("ghost_sword");
        items.add("gorgon_head");
        items.add("hippocampus_slapper");
        items.add("hippogryph_sword");
        items.add("lich_staff");
        items.add("pixie_wand");
        items.addAll(TOOLS.stream().map(tool -> "silver_" + tool).toList());
        items.add("siren_flute");
        items.add("stymphalian_bird_dagger");
        items.add("stymphalian_feather_bundle");
        items.add("tide_trident");
        return List.copyOf(items);
    }
}
