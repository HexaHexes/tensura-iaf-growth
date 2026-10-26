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
 * <p>Besides those three, files sit at {@code data/gearevolution/gear_existence/tensura/}:
 * a same-path override of Gear Evolution's own {@code short_bow} rung, retargeting the
 * vanilla/Tensura bow ladder through the dragon bone bow (see {@link #addToolChain}),
 * plus aligned copies of Tensura's silver rung that match the ones in Tensura's own
 * folder byte for byte, so neither provider's file can win the pack-priority race (see
 * {@link #addTensuraGear}).
 *
 * <p>The rest of Tensura's own folder ({@code data/tensura/gear_existence/}) holds the
 * silver and mithril merge - both tiers re-gated onto the Ice and Fire spine - plus
 * terminals for the goblin club and severer blade, and the kunai's step onto Tensura's
 * pure magisteel rung.
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
    private static final String GE_ROOT = "data/gearevolution/gear_existence/";
    private static final String GE_GEAR = GE_ROOT + NAMESPACE + "/";
    /**
     * Gear Evolution's own folder for Tensura-namespace rungs. Besides the short bow,
     * this pack writes aligned silver copies here - the same content it writes under
     * {@link #TENSURA_GEAR} - so the two provider files it displaces are both replaced
     * by identical values and pack priority can no longer pick a winner.
     */
    private static final String GE_TENSURA_GEAR = GE_ROOT + "tensura/";
    private static final String IAF_GEAR = "data/" + NAMESPACE + "/gear_existence/";
    /**
     * The short bow rung at Gear Evolution's path: a same-path override of Gear
     * Evolution's own value, retargeting the vanilla/Tensura bow ladder through the
     * dragon bone bow.
     */
    private static final String SHORT_BOW_PATH = GE_ROOT + "tensura/short_bow.json";
    /**
     * Tensura's own gear_existence folder, read by Tensura's datapack registry across
     * every namespace. The silver and mithril merge lives here (nineteen aligned
     * silver rungs and their nineteen mithril successors), plus the gap fixes: the
     * kunai's step onto pure magisteel and two terminals nothing else tracks.
     */
    private static final String TENSURA_GEAR = "data/tensura/gear_existence/";
    private static final String CHAINS = "data/gearevolution/addon_chains/" + NAMESPACE + "/";
    private static final String MOVESETS = "data/" + NAMESPACE + "/weapon_attributes/";
    private static final String ENTITY_PATH = EntityExistenceContent.PATH;

    // --- Chain tables -------------------------------------------------------

    private static final List<String> SLOTS = List.of("helmet", "chestplate", "leggings", "boots");
    private static final List<String> TOOLS = List.of("sword", "axe", "pickaxe", "shovel", "hoe");

    /**
     * Tensura's silver gear, split by what Gear Evolution already ships a rung for:
     * the twelve weapons and four armor pieces exist at both provider paths, the three
     * tools only in Tensura's own folder.
     */
    private static final List<String> TENSURA_SILVER_WEAPONS = List.of(
            "axe", "great_sword", "katana", "kodachi", "long_sword", "odachi",
            "scythe", "short_sword", "sickle", "spear", "sword", "tachi");
    private static final List<String> TENSURA_SILVER_TOOLS = List.of("hoe", "pickaxe", "shovel");

    /** The twelve dragon scale sets, grouped by the dragon element that forges them. */
    private static final Map<String, List<String>> SCALE_COLORS_BY_ELEMENT = scaleColorsByElement();

    /** The seven sea serpent (tide guardian) scale sets. */
    private static final List<String> TIDE_COLORS =
            List.of("blue", "bronze", "deepblue", "green", "purple", "red", "teal");

    /** Troll leather -> death worm chitin pairing (mountain / forest / frost). */
    private static final Map<String, String> TROLL_TO_CHITIN = trollToChitin();

    /**
     * EP gates. The first three rungs are the vanilla progression (leather, iron,
     * diamond); from there the ladder joins Tensura's own metals spine (low_magisteel,
     * high_magisteel, pure_magisteel, adamantite), so Ice and Fire gear starts on the
     * vanilla curve and climbs at Tensura's pace once the metals begin.
     */
    private static final int SHEEP_TO_COPPER = 550;
    private static final int COPPER_TO_SILVER = 3200;
    private static final int SILVER_TO_TROLL = 7800;
    private static final int TROLL_TO_CHITIN_GATE = 18000;
    private static final int CHITIN_TO_TIDE = 52000;
    private static final int TIDE_TO_SCALE = 225000;
    private static final int SCALE_TO_STEEL = 750000;

    /** The last gate of every dragonsteel ladder: its terminal hands off from here. */
    private static final int TERMINAL_MIN = 750000;

    /**
     * Tool and sword rungs, spread over the same spine as the armor ladder so both
     * ladders finish together: tools climb 3,200 / 52,000 / 750,000, swords
     * 3,200 / 52,000 / 225,000 / 750,000 (swords gain a blooded rung in between).
     * Tensura's silver gear mirrors the same silver rung - see {@link #TENSURA_MITHRIL_GATE}.
     */
    private static final int COPPER_TO_SILVER_TOOL = 3200;
    private static final int SILVER_TO_DRAGONBONE = 52000;
    private static final int DRAGONBONE_TO_BLOODED = 225000;
    private static final int DRAGONBONE_TO_STEEL = 750000;
    private static final int BLOODED_TO_STEEL = 750000;

    /** Singular ladders (dread line, death worm gauntlets), both on the 18k/52k rungs. */
    private static final int DREAD_KNIGHT = 18000;
    private static final int DREAD_QUEEN = 52000;
    private static final int GAUNTLET_WHITE = 18000;
    private static final int GAUNTLET_RED = 52000;

    /**
     * The silver and mithril merge. Tensura silver hands off where the matching Ice and
     * Fire silver item does - armor at the 7,800 troll rung, every weapon and tool at
     * the 52,000 dragon bone rung - and mithril forges on at 225,000, the floor
     * Tensura's own adamantite files carry, so the hand-off lands exactly where the
     * next tier starts earning. The kunai keeps its own step onto pure magisteel at
     * 52,000, where Tensura itself places it.
     */
    private static final int TENSURA_MITHRIL_GATE = 225000;
    private static final int KUNAI_GATE = 52000;

    /**
     * The bow ladder slots the dragon bone bow between Tensura's short and long bow by
     * damage and draw speed (short &lt; dragon bone &lt; long &lt; war). The two rungs
     * before and after it are Gear Evolution's own values, kept verbatim upstream; the
     * constants below only pin the slot so validation can prove the ordering.
     */
    private static final int UPSTREAM_BOW_GATE = 700;
    private static final int SHORT_BOW_GATE = 3600;
    private static final int DRAGONBONE_BOW_GATE = 7200;
    private static final int UPSTREAM_LONG_BOW_GATE = 12500;
    private static final int UPSTREAM_WAR_BOW_GATE = 16000;
    /**
     * The pack's one cross-namespace evolution target: the dragon bone bow slots into
     * Tensura's own ladder rather than retargeting an Ice and Fire item.
     */
    private static final String LONG_BOW = "tensura:long_bow";

    /**
     * Terminal climb rates: armour-grade gear keeps earning fastest, a singular ladder
     * that hands off at 52,000 keeps its middle rate, and gear with no ladder at all
     * still trickles. Climbing rates are derived from each rung's gate by
     * {@link #epGainFor(int)}.
     */
    private static final double EP_GAIN_TOP = 0.04;
    private static final double EP_GAIN_HANDOFF = 0.025;
    private static final double EP_GAIN_NO_LADDER = 0.003;

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
            "dragon_flute",
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

    /**
     * Our own gear_existence files the addon does not ship, so no shadow copy is needed.
     * Includes {@code short_bow}, which lives at Gear Evolution's path rather than ours.
     */
    private static final Set<String> NOT_IN_ADDON = Set.of(
            "armor_copper_metal_boots", "armor_copper_metal_chestplate", "armor_copper_metal_helmet",
            "armor_copper_metal_leggings",
            "armor_silver_metal_boots", "armor_silver_metal_chestplate", "armor_silver_metal_helmet",
            "armor_silver_metal_leggings",
            "blindfold", "copper_axe", "copper_hoe", "copper_pickaxe", "copper_shovel", "copper_sword",
            "cyclops_eye", "deathworm_gauntlet_red", "deathworm_gauntlet_white", "deathworm_gauntlet_yellow",
            "dragon_flute",
            "dread_knight_sword", "earplugs", "fishing_spear", "gorgon_head",
            "sheep_boots", "sheep_chestplate", "sheep_helmet", "sheep_leggings",
            "short_bow",
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
            Map<String, JsonObject> tensuraGearExistence,
            Map<String, JsonObject> addonChains,
            Map<String, JsonObject> weaponAttributes,
            Map<String, JsonObject> entityExistence) {
    }

    public static Content build() {
        Map<String, JsonObject> ourGear = new LinkedHashMap<>();
        Map<String, JsonObject> tensuraGear = new LinkedHashMap<>();
        Map<String, JsonObject> chains = new LinkedHashMap<>();
        Map<String, JsonObject> movesets = new LinkedHashMap<>();

        addArmorChain(ourGear, chains);
        addToolChain(ourGear, chains);
        addSingularGear(ourGear);

        // Gear Evolution's built-in chains, neutralised at their own resource path.
        Map<String, JsonObject> geGear = new LinkedHashMap<>(ourGear);
        for (String item : GE_BUILTIN_NEUTRALISED) {
            put(geGear, GE_GEAR + file(item), inert(item));
        }

        addTensuraGear(tensuraGear, geGear);
        addMovesets(movesets);

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
                    ADDON_TROLL_WEAPONS.contains(item) ? terminal(item, 0, EP_GAIN_NO_LADDER, null) : inert(item));
        }

        Content content = new Content(geGear, iafGear, tensuraGear, chains, movesets, EntityExistenceContent.build());
        validate(content);
        return content;
    }

    // --- Chain 1: armor -----------------------------------------------------
    //
    // sheep disguise -> copper -> (random) either Ice and Fire silver -> troll leather
    // -> death worm chitin -> sea serpent scale -> dragon scale -> dragon steel,
    // or Tensura silver -> mithril -> adamantite -> hihiirokane (see addTensuraGear)

    private static void addArmorChain(Map<String, JsonObject> gear, Map<String, JsonObject> chains) {
        for (String slot : SLOTS) {
            // Sheep disguise -> copper.
            put(gear, gearPath(id("sheep_" + slot)),
                    gear(id("sheep_" + slot), 0, SHEEP_TO_COPPER, epGainFor(SHEEP_TO_COPPER), null,
                            id("armor_copper_metal_" + slot)));

            // Copper -> either silver, rolled at random: the Ice and Fire set or
            // Tensura's, which then climb their own aligned rungs. The addon chain is
            // the only file for copper, so Gear Evolution's built-in copper thresholds
            // can never outvote the fork.
            put(chains, chainPath(id("armor_copper_metal_" + slot)),
                    chain(id("armor_copper_metal_" + slot), 0, COPPER_TO_SILVER, epGainFor(COPPER_TO_SILVER),
                            null, List.of(id("armor_silver_metal_" + slot), "tensura:silver_" + slot)));

            // Silver -> branch into the three troll leather sets. The engraving lives in
            // gear_existence (Tensura reads it) with maxEP 0 so Tensura does not manage EP
            // or a target here; the addon chain is authoritative for both.
            put(gear, gearPath(id("armor_silver_metal_" + slot)),
                    gear(id("armor_silver_metal_" + slot), 0, 0, 0, HOLY_COAT_1, null));
            put(chains, chainPath(id("armor_silver_metal_" + slot)),
                    chain(id("armor_silver_metal_" + slot), 0, SILVER_TO_TROLL, epGainFor(SILVER_TO_TROLL), null,
                            TROLL_TO_CHITIN.keySet().stream().map(troll -> id(troll + "_troll_leather_" + slot)).toList()));

            // Troll leather -> its paired death worm chitin (deterministic pairing).
            for (Map.Entry<String, String> pairing : TROLL_TO_CHITIN.entrySet()) {
                String troll = pairing.getKey();
                put(chains, chainPath(id(troll + "_troll_leather_" + slot)),
                        chain(id(troll + "_troll_leather_" + slot), 0, TROLL_TO_CHITIN_GATE,
                                epGainFor(TROLL_TO_CHITIN_GATE),
                                id("deathworm_" + pairing.getValue() + "_" + slot), null));
            }

            // Death worm chitin (all three, one tier) -> any of the seven sea serpent sets.
            for (String chitin : List.of("yellow", "white", "red")) {
                put(chains, chainPath(id("deathworm_" + chitin + "_" + slot)),
                        chain(id("deathworm_" + chitin + "_" + slot), 0, CHITIN_TO_TIDE, epGainFor(CHITIN_TO_TIDE),
                                null, ids(TIDE_COLORS, "_" + slot, "tide_")));
            }

            // Sea serpent scale -> any of the twelve dragon scale sets.
            for (String tide : TIDE_COLORS) {
                put(gear, gearPath(id("tide_" + tide + "_" + slot)),
                        gear(id("tide_" + tide + "_" + slot), 0, 0, 0, BREATHING_SUPPORT, null));
                put(chains, chainPath(id("tide_" + tide + "_" + slot)),
                        chain(id("tide_" + tide + "_" + slot), 0, TIDE_TO_SCALE, epGainFor(TIDE_TO_SCALE), null,
                                scaleArmorIds(slot)));
            }

            // Dragon scale -> the dragon steel set of its element (deterministic).
            for (Map.Entry<String, List<String>> element : SCALE_COLORS_BY_ELEMENT.entrySet()) {
                for (String color : element.getValue()) {
                    put(gear, gearPath(id("armor_" + color + "_" + slot)),
                            gear(id("armor_" + color + "_" + slot), 0, SCALE_TO_STEEL, epGainFor(SCALE_TO_STEEL),
                                    ELEMENTAL_RESISTANCE, id("dragonsteel_" + element.getKey() + "_" + slot)));
                }
            }

            // Dragon steel: top of the ladder, keeps earning from its own last gate,
            // engraved like mithril.
            for (String element : SCALE_COLORS_BY_ELEMENT.keySet()) {
                put(gear, gearPath(id("dragonsteel_" + element + "_" + slot)),
                        terminal("dragonsteel_" + element + "_" + slot, TERMINAL_MIN, EP_GAIN_TOP, HOLY_COAT_2));
            }
        }
    }

    // --- Chain 2: tools and weapons ----------------------------------------
    //
    // copper -> (random) either Ice and Fire silver -> dragon bone -> blooded
    // dragon bone (swords) -> dragon steel, or Tensura silver -> mithril (see
    // addTensuraGear); plus the vanilla/Tensura bow ladder, slotted through the
    // dragon bone bow

    private static void addToolChain(Map<String, JsonObject> gear, Map<String, JsonObject> chains) {
        for (String tool : TOOLS) {
            // Copper -> either silver, rolled at random: the Ice and Fire tool or
            // Tensura's, which then climb their own aligned rungs. The addon chain is
            // the only file for copper, so Gear Evolution's built-in copper thresholds
            // can never outvote the fork.
            put(chains, chainPath(id("copper_" + tool)),
                    chain(id("copper_" + tool), 0, COPPER_TO_SILVER_TOOL, epGainFor(COPPER_TO_SILVER_TOOL),
                            null, List.of(id("silver_" + tool), "tensura:silver_" + tool)));

            // Silver -> dragon bone, one rung for every tool now that swords carry
            // their blooded tier further up instead of joining the spine early.
            put(gear, gearPath(id("silver_" + tool)),
                    gear(id("silver_" + tool), 0, SILVER_TO_DRAGONBONE, epGainFor(SILVER_TO_DRAGONBONE),
                            HOLY_COAT_1, id("dragonbone_" + tool)));

            if (tool.equals("sword")) {
                // Dragon bone sword -> a blooded (elemental) dragon bone sword, rolled at
                // random; each blooded sword then forges into its matching dragon steel.
                put(chains, chainPath(id("dragonbone_sword")),
                        chain(id("dragonbone_sword"), 0, DRAGONBONE_TO_BLOODED, epGainFor(DRAGONBONE_TO_BLOODED),
                                null, SCALE_COLORS_BY_ELEMENT.keySet().stream()
                                        .map(element -> id("dragonbone_sword_" + element)).toList()));

                for (String element : SCALE_COLORS_BY_ELEMENT.keySet()) {
                    put(gear, gearPath(id("dragonbone_sword_" + element)),
                            gear(id("dragonbone_sword_" + element), 0, BLOODED_TO_STEEL, epGainFor(BLOODED_TO_STEEL),
                                    ELEMENTAL_BOOST, id("dragonsteel_" + element + "_sword")));
                }
            } else {
                // Tools have no blooded tier: dragon bone rolls straight into one of the
                // three dragon steel elements.
                put(chains, chainPath(id("dragonbone_" + tool)),
                        chain(id("dragonbone_" + tool), 0, DRAGONBONE_TO_STEEL, epGainFor(DRAGONBONE_TO_STEEL), null,
                                SCALE_COLORS_BY_ELEMENT.keySet().stream()
                                        .map(element -> id("dragonsteel_" + element + "_" + tool)).toList()));
            }

            // Dragon steel tools and weapons: terminal, engraved like hihiirokane.
            for (String element : SCALE_COLORS_BY_ELEMENT.keySet()) {
                put(gear, gearPath(id("dragonsteel_" + element + "_" + tool)),
                        terminal("dragonsteel_" + element + "_" + tool, TERMINAL_MIN, EP_GAIN_TOP, TSUKUMOGAMI));
            }
        }

        // The bow ladder, slotted by damage and draw speed: short bow -> dragon bone bow
        // -> Tensura's long bow -> war bow. The rungs outside our two files (bow at 700,
        // long bow at 12,500, war bow at 16,000) are Gear Evolution's own and stay
        // upstream; validation pins their ordering against ours.
        put(gear, gearPath(id("dragonbone_bow")),
                gear(id("dragonbone_bow"), 0, DRAGONBONE_BOW_GATE, epGainFor(DRAGONBONE_BOW_GATE), null, LONG_BOW));
        // Same-path override of Gear Evolution's short_bow rung, retargeted through the
        // dragon bone bow - the pack's one file outside the Ice and Fire namespace.
        put(gear, SHORT_BOW_PATH,
                gear("tensura:short_bow", 0, SHORT_BOW_GATE, epGainFor(SHORT_BOW_GATE), null, id("dragonbone_bow")));
    }

    // --- Single-item ladders ------------------------------------------------

    private static void addSingularGear(Map<String, JsonObject> gear) {
        // Death worm gauntlets: yellow -> white -> red, then hand off at 52,000.
        put(gear, gearPath(id("deathworm_gauntlet_yellow")),
                gear(id("deathworm_gauntlet_yellow"), 0, GAUNTLET_WHITE, epGainFor(GAUNTLET_WHITE), null,
                        id("deathworm_gauntlet_white")));
        put(gear, gearPath(id("deathworm_gauntlet_white")),
                gear(id("deathworm_gauntlet_white"), 0, GAUNTLET_RED, epGainFor(GAUNTLET_RED), null,
                        id("deathworm_gauntlet_red")));
        put(gear, gearPath(id("deathworm_gauntlet_red")),
                terminal("deathworm_gauntlet_red", GAUNTLET_RED, EP_GAIN_HANDOFF, null));

        // Dread line: Dread Thrall's Sword -> Dread Knight's Sword -> Blade of the
        // Dreaded Queen, then hand off at 52,000.
        put(gear, gearPath(id("dread_sword")),
                gear(id("dread_sword"), 0, DREAD_KNIGHT, epGainFor(DREAD_KNIGHT), null, id("dread_knight_sword")));
        put(gear, gearPath(id("dread_knight_sword")),
                gear(id("dread_knight_sword"), 0, DREAD_QUEEN, epGainFor(DREAD_QUEEN), null,
                        id("dread_queen_sword")));
        put(gear, gearPath(id("dread_queen_sword")),
                terminal("dread_queen_sword", DREAD_QUEEN, EP_GAIN_HANDOFF, null));
        put(gear, gearPath(id("dread_queen_staff")),
                terminal("dread_queen_staff", DREAD_QUEEN, EP_GAIN_HANDOFF, null));

        // Uniques without an upgrade line earn nothing from a ladder they never climb.
        for (String unique : TERMINAL_UNIQUES) {
            put(gear, gearPath(id(unique)), terminal(unique, 0, EP_GAIN_NO_LADDER, null));
        }
    }

    // --- Tensura silver and mithril merge -----------------------------------
    //
    // Both tiers climb the Ice and Fire spine: silver hands off where the matching
    // Ice and Fire silver item does, mithril forges on at 225,000. The kunai hand-off
    // and the two no-ladder terminals from the original gap fixes live alongside them;
    // the silver tools simply climbed onto the aligned rung.

    private static void addTensuraGear(Map<String, JsonObject> gear, Map<String, JsonObject> geGear) {
        // Silver, aligned with Ice and Fire's silver rung: armor mirrors the 7,800
        // step into troll leather, every weapon and tool the 52,000 step into dragon
        // bone. Gear Evolution ships the sixteen non-tool files itself and Tensura
        // ships inert copies of all nineteen, so those sixteen get the same JsonObject
        // at both resource paths: whichever file the pack-priority race picks, the
        // values match. The three tools have no Gear Evolution file - Tensura's own
        // folder is their only path.
        for (String slot : SLOTS) {
            putSilver(gear, geGear, "silver_" + slot, SILVER_TO_TROLL);
        }
        for (String weapon : TENSURA_SILVER_WEAPONS) {
            putSilver(gear, geGear, "silver_" + weapon, SILVER_TO_DRAGONBONE);
        }
        for (String tool : TENSURA_SILVER_TOOLS) {
            putSilver(gear, null, "silver_" + tool, SILVER_TO_DRAGONBONE);
        }

        // Mithril: Tensura's own gate (225,000) already sits on our spine, so only the
        // earning floor moves - down to where the aligned silver input arrives, so no
        // EP is stranded between the tiers - and the climb rate is re-derived from the
        // gate. Engravings stay exactly as Tensura ships them (holy coat II).
        for (String slot : SLOTS) {
            putMithril(gear, "mithril_" + slot, SILVER_TO_TROLL);
        }
        for (String weapon : TENSURA_SILVER_WEAPONS) {
            putMithril(gear, "mithril_" + weapon, SILVER_TO_DRAGONBONE);
        }
        for (String tool : TENSURA_SILVER_TOOLS) {
            putMithril(gear, "mithril_" + tool, SILVER_TO_DRAGONBONE);
        }

        // The kunai is the base form of pure_magisteel_kunai, which Tensura starts
        // earning from at 52,000 - so the hand-off happens exactly there, at the gate
        // Tensura's own high_magisteel gear evolves at.
        put(gear, TENSURA_GEAR + file("tensura:kunai"),
                gear("tensura:kunai", 0, KUNAI_GATE, epGainFor(KUNAI_GATE), null,
                        "tensura:pure_magisteel_kunai"));

        // Weapons with no ladder anywhere: terminal, and a trickle so they still pay.
        put(gear, TENSURA_GEAR + file("tensura:goblin_club"), tensuraTerminal("goblin_club"));
        put(gear, TENSURA_GEAR + file("tensura:severer_blade"), tensuraTerminal("severer_blade"));
    }

    /**
     * One Tensura silver rung at {@link #TENSURA_GEAR}, and - when Gear Evolution
     * ships the item too - the identical file at {@link #GE_TENSURA_GEAR}. Both maps
     * hold the same JsonObject instance on purpose: the content cannot diverge.
     *
     * @param geGear the Gear Evolution namespace map, or {@code null} for the tools
     *                Gear Evolution ships no file for
     * @param gate   the aligned Ice and Fire silver gate this item hands off at
     */
    private static void putSilver(Map<String, JsonObject> gear, Map<String, JsonObject> geGear, String item,
            int gate) {
        JsonObject json = gear("tensura:" + item, 0, gate, epGainFor(gate), HOLY_COAT_1,
                "tensura:mithril_" + item.substring("silver_".length()));
        put(gear, TENSURA_GEAR + file("tensura:" + item), json);
        if (geGear != null) {
            put(geGear, GE_TENSURA_GEAR + file("tensura:" + item), json);
        }
    }

    /** One Tensura mithril rung, floored at the silver gate it evolves from. */
    private static void putMithril(Map<String, JsonObject> gear, String item, int floor) {
        put(gear, TENSURA_GEAR + file("tensura:" + item),
                gear("tensura:" + item, floor, TENSURA_MITHRIL_GATE, epGainFor(TENSURA_MITHRIL_GATE),
                        HOLY_COAT_2, "tensura:adamantite_" + item.substring("mithril_".length())));
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
     * A top-of-the-ladder rung: no evolution target, and it keeps earning from its own
     * last gate ({@code minEP}) rather than from zero.
     *
     * @param item the bare Ice and Fire item name, without its namespace
     */
    private static JsonObject terminal(String item, int minEP, double epGain, JsonObject engravings) {
        return gear(id(item), minEP, null, epGain, engravings, null);
    }

    /**
     * A Tensura-namespace terminal: no ladder anywhere, so it only ever trickles.
     *
     * @param item the bare Tensura item name, without its namespace
     */
    private static JsonObject tensuraTerminal(String item) {
        return gear("tensura:" + item, 0, null, EP_GAIN_NO_LADDER, null, null);
    }

    /**
     * The climb rate for a rung that hands off to another item, derived from its gate.
     * The vanilla rungs and the whole bow ladder trickle at the no-ladder rate; once the
     * metals spine begins the rate climbs 0.01 / 0.02 / 0.025 / 0.03 across the 18,000 /
     * 52,000 / 225,000 / 750,000 rungs.
     *
     * @param gate the EP at which the item evolves
     */
    private static double epGainFor(int gate) {
        if (gate == TROLL_TO_CHITIN_GATE) {
            return 0.01d;
        }
        if (gate == CHITIN_TO_TIDE) {
            return 0.02d;
        }
        if (gate == TIDE_TO_SCALE) {
            return 0.025d;
        }
        if (gate == SCALE_TO_STEEL) {
            return 0.03d;
        }
        if (gate < TROLL_TO_CHITIN_GATE) {
            return EP_GAIN_NO_LADDER;
        }
        throw new IllegalStateException("no EP climb rate defined for gate " + gate);
    }

    // --- File builders ------------------------------------------------------

    /**
     * @param maxEP the EP at which the item evolves, or {@code null} for a terminal
     *              (the codec then applies its 2,000,000 ceiling); an explicit {@code 0}
     *              marks an inert file whose EP the addon_chains ladder drives
     */
    private static JsonObject gear(String item, int minEP, Integer maxEP, double epGain, JsonObject engravings,
            String evolution) {
        JsonObject json = new JsonObject();
        json.addProperty("type", item);
        json.addProperty("minEP", minEP);
        if (maxEP != null) {
            json.addProperty("maxEP", maxEP);
        }
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

        expect(errors, "gear_existence files under data/gearevolution", 178, content.geGearExistence.size());
        expect(errors, "gear_existence files under data/iceandfire", 151, content.iafGearExistence.size());
        expect(errors, "gear_existence files under data/tensura", 41, content.tensuraGearExistence.size());
        expect(errors, "addon_chains files", 70, content.addonChains.size());
        expect(errors, "weapon_attributes files", 37, content.weaponAttributes.size());
        expect(errors, "entity_existence files", 24, content.entityExistence.size());
        expect(errors, "neutralised Gear Evolution built-ins", 17, GE_BUILTIN_NEUTRALISED.size());
        expect(errors, "files the addon ships that we shadow", 32, ADDON_ONLY.size());
        expect(errors, "our files the addon does not ship", 35, NOT_IN_ADDON.size());
        expect(errors, "files in total", 501,
                content.geGearExistence.size() + content.iafGearExistence.size()
                        + content.tensuraGearExistence.size()
                        + content.addonChains.size() + content.weaponAttributes.size() + content.entityExistence.size());

        List<Map<String, JsonObject>> all = List.of(content.geGearExistence, content.iafGearExistence,
                content.tensuraGearExistence, content.addonChains, content.weaponAttributes,
                content.entityExistence);

        // Every file must land under the path the consuming mod reads it from - the
        // same item legitimately appears under two of them. Gear Evolution's short bow
        // and this pack's aligned Tensura silver copies live in Gear Evolution's own
        // Tensura folder, so that prefix is accepted alongside the Ice and Fire one.
        checkPrefix(errors, content.geGearExistence, GE_GEAR, GE_TENSURA_GEAR);
        checkPrefix(errors, content.iafGearExistence, IAF_GEAR);
        checkPrefix(errors, content.tensuraGearExistence, TENSURA_GEAR);
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

        // The item id in every file must carry a real namespace: Tensura matches it
        // against the item's registry name, and an unqualified id resolves to
        // minecraft:<name>, which would silently match nothing. Files under Tensura's
        // own folder or Gear Evolution's Tensura folder must be tensura ids; everywhere
        // else must be Ice and Fire ids.
        for (Map<String, JsonObject> files : all) {
            for (Map.Entry<String, JsonObject> entry : files.entrySet()) {
                JsonObject json = entry.getValue();
                for (String field : new String[] { "type", "item" }) {
                    if (!json.has(field)) {
                        continue;
                    }
                    String value = json.get(field).getAsString();
                    if (entry.getKey().startsWith(TENSURA_GEAR) || entry.getKey().startsWith(GE_TENSURA_GEAR)) {
                        if (!value.startsWith("tensura:")) {
                            errors.add(entry.getKey() + " -> " + field + " is not a tensura id");
                        }
                        continue;
                    }
                    if (!value.startsWith(NAMESPACE + ":")) {
                        errors.add(entry.getKey() + " -> " + field + " is missing its namespace");
                    }
                }
            }
        }

        // Every evolution target must itself be a generated item (catches typos). The ids
        // come from each file's own type/item fields, so the bow override registers as
        // tensura:short_bow rather than as an Ice and Fire item, and Tensura's own
        // silver/mithril rungs register as the tensura ids the copper fork targets.
        Set<String> generated = new LinkedHashSet<>();
        for (Map<String, JsonObject> files : List.of(content.geGearExistence, content.addonChains,
                content.tensuraGearExistence)) {
            for (JsonObject json : files.values()) {
                for (String field : new String[] { "type", "item" }) {
                    if (json.has(field)) {
                        generated.add(json.get(field).getAsString());
                    }
                }
            }
        }
        for (Map<String, JsonObject> files : List.of(content.geGearExistence, content.iafGearExistence,
                content.tensuraGearExistence, content.addonChains)) {
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

        // Everything written for Gear Evolution also needs a shadow under the addon's
        // path, or the addon's own thresholds would outvote ours. The neutralised
        // built-ins are the exception: they are inert, their EP comes from the
        // addon_chains ladder, and the addon only ships shadows for some of them.
        // Files in Gear Evolution's Tensura folder are exempt outright - the addon
        // never ships Ice and Fire shadows for Tensura-namespace items.
        for (String path : content.geGearExistence.keySet()) {
            String item = name(path);
            if (GE_BUILTIN_NEUTRALISED.contains(item)) {
                continue;
            }
            if (path.startsWith(GE_TENSURA_GEAR)) {
                continue;
            }
            if (!NOT_IN_ADDON.contains(item)
                    && !content.iafGearExistence.containsKey(IAF_GEAR + file(item))) {
                errors.add(path + " has no data/iceandfire shadow and is not listed in NOT_IN_ADDON");
            }
        }

        // maxEP semantics: an explicit 0 marks an inert file (the addon_chains ladder
        // drives the item, so Tensura must not manage EP or a target here), any other
        // explicit maxEP must name an evolution, and an omitted maxEP is a terminal
        // that keeps earning with no target - the codec's 2,000,000 default caps it.
        for (Map<String, JsonObject> files : List.of(content.geGearExistence, content.iafGearExistence,
                content.tensuraGearExistence)) {
            for (Map.Entry<String, JsonObject> entry : files.entrySet()) {
                JsonObject json = entry.getValue();
                boolean evolves = json.has("evolution") || json.has("evolutionOptions");
                if (!json.has("maxEP")) {
                    if (evolves) {
                        errors.add(entry.getKey() + " is terminal but still names an evolution");
                    }
                } else if (json.get("maxEP").getAsInt() == 0) {
                    if (evolves) {
                        errors.add(entry.getKey() + " is inert (maxEP 0) but still names an evolution");
                    }
                } else if (!evolves) {
                    errors.add(entry.getKey() + " gates at " + json.get("maxEP")
                            + " but names no evolution target");
                }
            }
        }

        // Every ladder must climb strictly: a stalled or reversed gate would strand
        // gear below a threshold it can never reach.
        checkAscending(errors, "armor spine",
                SHEEP_TO_COPPER, COPPER_TO_SILVER, SILVER_TO_TROLL, TROLL_TO_CHITIN_GATE, CHITIN_TO_TIDE,
                TIDE_TO_SCALE, SCALE_TO_STEEL);
        checkAscending(errors, "tool ladder", COPPER_TO_SILVER_TOOL, SILVER_TO_DRAGONBONE, DRAGONBONE_TO_STEEL);
        checkAscending(errors, "sword ladder", COPPER_TO_SILVER_TOOL, SILVER_TO_DRAGONBONE,
                DRAGONBONE_TO_BLOODED, BLOODED_TO_STEEL);
        // The Tensura merge climbs the same spine, entered from the copper fork's
        // aligned silver branch; both variants top out on Tensura's own hihiirokane
        // rung, which gates at 750,000 - the value our steel ladder lands on.
        checkAscending(errors, "tensura weapons spine", COPPER_TO_SILVER_TOOL, SILVER_TO_DRAGONBONE,
                TENSURA_MITHRIL_GATE, SCALE_TO_STEEL);
        checkAscending(errors, "tensura armor spine", COPPER_TO_SILVER, SILVER_TO_TROLL,
                TENSURA_MITHRIL_GATE, SCALE_TO_STEEL);
        checkAscending(errors, "dread line", DREAD_KNIGHT, DREAD_QUEEN);
        checkAscending(errors, "death worm gauntlets", GAUNTLET_WHITE, GAUNTLET_RED);
        checkAscending(errors, "bow ladder",
                UPSTREAM_BOW_GATE, SHORT_BOW_GATE, DRAGONBONE_BOW_GATE, UPSTREAM_LONG_BOW_GATE, UPSTREAM_WAR_BOW_GATE);

        // All three dragon steel ladders land on the same last gate, where the
        // terminal takes over.
        expect(errors, "dragon steel armor gate", SCALE_TO_STEEL, TERMINAL_MIN);
        expect(errors, "dragon steel tool gate", SCALE_TO_STEEL, DRAGONBONE_TO_STEEL);
        expect(errors, "blooded sword forge gate", SCALE_TO_STEEL, BLOODED_TO_STEEL);

        // Structural spot checks - the counts a future edit can silently break.
        expect(errors, "dragon scale gear_existence files", 48,
                (int) content.geGearExistence.keySet().stream().filter(RebalanceContent::isScale).count());
        expect(errors, "tide addon chains", 28,
                (int) content.addonChains.keySet().stream().filter(path -> name(path).startsWith("tide_")).count());
        expect(errors, "troll addon chains", 12,
                (int) content.addonChains.keySet().stream()
                        .filter(path -> name(path).matches("(mountain|forest|frost)_troll_leather_.*")).count());

        // Ladder spot checks, at literal plan values so a change to epGainFor or the
        // gate tables cannot quietly redefine what "correct" means: the one silver
        // rung every sword and tool shares, and the bow pair that reaches outside the
        // Ice and Fire namespace.
        expectRung(errors, content, "silver_sword", 52000, 0.02d, id("dragonbone_sword"));
        expectRung(errors, content, "silver_axe", 52000, 0.02d, id("dragonbone_axe"));
        expectRung(errors, content, "dragonbone_bow", 7200, 0.003d, LONG_BOW);
        expectRungAt(errors, "short_bow", content.geGearExistence.get(SHORT_BOW_PATH),
                3600, 0.003d, id("dragonbone_bow"));

        // Terminal spot checks across all three shapes: ladder top, singular hand-off,
        // and a unique that never climbs.
        expectTerminal(errors, content, "dragonsteel_fire_helmet", 750000, 0.04d);
        expectTerminal(errors, content, "dread_queen_sword", 52000, 0.025d);
        expectTerminal(errors, content, "pixie_wand", 0, 0.003d);
        expectTerminal(errors, content, "troll_weapon_axe", 0, 0.003d);
        expectTerminal(errors, content, "dragon_flute", 0, 0.003d);

        // The Tensura silver and mithril merge, pinned to the aligned rungs: armor
        // mirrors the Ice and Fire silver gate at 7,800, weapons and tools the 52,000
        // dragon bone rung; mithril floors where its silver input arrives and forges
        // on at Tensura's own 225,000 adamantite gate. The kunai hand-off and the two
        // terminals are unchanged gap fixes.
        expectLadderRung(errors, "tensura:silver_helmet",
                findTensuraGear(errors, content, "silver_helmet"), 0, 7800, 0.003d, "tensura:mithril_helmet");
        expectLadderRung(errors, "tensura:silver_katana",
                findTensuraGear(errors, content, "silver_katana"), 0, 52000, 0.02d, "tensura:mithril_katana");
        expectLadderRung(errors, "tensura:silver_hoe",
                findTensuraGear(errors, content, "silver_hoe"), 0, 52000, 0.02d, "tensura:mithril_hoe");
        expectLadderRung(errors, "tensura:silver_pickaxe",
                findTensuraGear(errors, content, "silver_pickaxe"), 0, 52000, 0.02d, "tensura:mithril_pickaxe");
        expectLadderRung(errors, "tensura:silver_shovel",
                findTensuraGear(errors, content, "silver_shovel"), 0, 52000, 0.02d, "tensura:mithril_shovel");
        expectLadderRung(errors, "tensura:mithril_helmet",
                findTensuraGear(errors, content, "mithril_helmet"), 7800, 225000, 0.025d,
                "tensura:adamantite_helmet");
        expectLadderRung(errors, "tensura:mithril_katana",
                findTensuraGear(errors, content, "mithril_katana"), 52000, 225000, 0.025d,
                "tensura:adamantite_katana");
        expectLadderRung(errors, "tensura:mithril_shovel",
                findTensuraGear(errors, content, "mithril_shovel"), 52000, 225000, 0.025d,
                "tensura:adamantite_shovel");
        expectRungAt(errors, "tensura:kunai",
                findTensuraGear(errors, content, "kunai"), 52000, 0.02d, "tensura:pure_magisteel_kunai");
        expectTerminalAt(errors, "tensura:goblin_club",
                findTensuraGear(errors, content, "goblin_club"), 0, 0.003d);
        expectTerminalAt(errors, "tensura:severer_blade",
                findTensuraGear(errors, content, "severer_blade"), 0, 0.003d);

        // Every silver item exists at both provider paths with identical content, so
        // the pack-priority race cannot pick a stale winner; the three tools Gear
        // Evolution ships nothing for exist only in Tensura's own folder.
        for (String slot : SLOTS) {
            expectSilverPair(errors, content, "silver_" + slot);
        }
        for (String weapon : TENSURA_SILVER_WEAPONS) {
            expectSilverPair(errors, content, "silver_" + weapon);
        }
        expect(errors, "Tensura silver files", 19,
                (int) content.tensuraGearExistence.keySet().stream()
                        .filter(path -> name(path).startsWith("silver_")).count());
        expect(errors, "Tensura mithril files", 19,
                (int) content.tensuraGearExistence.keySet().stream()
                        .filter(path -> name(path).startsWith("mithril_")).count());
        expect(errors, "Gear Evolution silver copies", 16,
                (int) content.geGearExistence.keySet().stream()
                        .filter(path -> path.startsWith(GE_TENSURA_GEAR) && name(path).startsWith("silver_"))
                        .count());

        // The copper fork: every copper item rolls between the two silvers at 3,200,
        // chain-driven only - a live gear_existence file would outvote the fork.
        for (String slot : SLOTS) {
            expectFork(errors, content, "armor_copper_metal_" + slot, 3200, 0.003d,
                    id("armor_silver_metal_" + slot), "tensura:silver_" + slot);
            if (content.geGearExistence.containsKey(GE_GEAR + file("armor_copper_metal_" + slot))) {
                errors.add("armor_copper_metal_" + slot + " must not ship a gear_existence file");
            }
        }
        for (String tool : TOOLS) {
            expectFork(errors, content, "copper_" + tool, 3200, 0.003d,
                    id("silver_" + tool), "tensura:silver_" + tool);
            if (content.geGearExistence.containsKey(GE_GEAR + file("copper_" + tool))) {
                errors.add("copper_" + tool + " must not ship a gear_existence file");
            }
        }

        // Inert files carry maxEP 0 and never name a target: one engraving-only file
        // and one neutralised Gear Evolution built-in.
        expectInert(errors, content, "armor_silver_metal_helmet");
        expectInert(errors, content, "deathworm_red_helmet");

        // Addon chain spot checks: the blooded branch, the dragon steel tool roll, and
        // the troll leather ladder the chains alone drive.
        expectChainRung(errors, content, "dragonbone_sword", 225000, 0.025d);
        expectChainRung(errors, content, "dragonbone_axe", 750000, 0.03d);
        expectChainRung(errors, content, "mountain_troll_leather_helmet", 18000, 0.01d);

        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                    "Rebalanced datapack failed validation:\n  " + String.join("\n  ", errors));
        }
    }

    private static void checkTarget(List<String> errors, String path, String target, Set<String> generated) {
        if (generated.contains(target)) {
            return;
        }
        if (target.startsWith("tensura:")) {
            // Files in either Tensura folder climb Tensura's own ladder: targets this
            // pack does not generate a file for (adamantite and above) are allowed
            // there, and each generated pair is spot-checked.
            if (path.startsWith(TENSURA_GEAR) || path.startsWith(GE_TENSURA_GEAR)) {
                return;
            }
            // The one grandfathered exception to "targets stay Ice and Fire": the dragon
            // bone bow slots into Tensura's own bow ladder - and it does so from both of
            // the paths its file ships under (Gear Evolution's folder and the addon's).
            if (target.equals(LONG_BOW)
                    && (path.equals(GE_GEAR + file("dragonbone_bow"))
                            || path.equals(IAF_GEAR + file("dragonbone_bow")))) {
                return;
            }
        }
        errors.add(path + " -> unknown evolution target '" + target + "'");
    }

    private static void expect(List<String> errors, String what, int wanted, int found) {
        if (wanted != found) {
            errors.add("expected " + wanted + " " + what + ", found " + found);
        }
    }

    /**
     * @param exempt exact paths that are allowed through, or - for entries ending in
     *               {@code "/"} - path prefixes that are
     */
    private static void checkPrefix(List<String> errors, Map<String, JsonObject> files, String prefix,
            String... exempt) {
        for (String path : files.keySet()) {
            if (path.startsWith(prefix)) {
                continue;
            }
            boolean allowed = false;
            for (String entry : exempt) {
                allowed |= entry.endsWith("/") ? path.startsWith(entry) : path.equals(entry);
            }
            if (allowed) {
                continue;
            }
            errors.add("file written outside " + prefix + ": " + path);
        }
    }

    private static void checkAscending(List<String> errors, String ladder, int... gates) {
        for (int i = 1; i < gates.length; i++) {
            if (gates[i] <= gates[i - 1]) {
                errors.add(ladder + " is not strictly ascending: gate " + gates[i]
                        + " does not follow " + gates[i - 1]);
            }
        }
    }

    // --- Ladder spot checks -------------------------------------------------

    /** The pack's gear_existence file for an item, whichever map it was written into. */
    private static JsonObject findGear(List<String> errors, Content content, String item) {
        JsonObject json = content.geGearExistence.get(GE_GEAR + file(item));
        if (json == null) {
            json = content.iafGearExistence.get(IAF_GEAR + file(item));
        }
        if (json == null) {
            errors.add("expected a gear_existence file for " + item);
        }
        return json;
    }

    private static void expectRung(List<String> errors, Content content, String item, int maxEP, double epGain,
            String target) {
        JsonObject json = findGear(errors, content, item);
        if (json != null) {
            expectRungAt(errors, item, json, maxEP, epGain, target);
        }
    }

    private static void expectRungAt(List<String> errors, String item, JsonObject json, int maxEP, double epGain,
            String target) {
        if (json == null) {
            errors.add(item + " has no generated file");
            return;
        }
        expectInt(errors, item, "maxEP", maxEP, json);
        expectRate(errors, item, epGain, json);
        String found = json.has("evolution") ? json.get("evolution").getAsString() : null;
        if (!target.equals(found)) {
            errors.add(item + " should evolve into " + target + ", found "
                    + (found == null ? "nothing" : found));
        }
    }

    private static void expectTerminal(List<String> errors, Content content, String item, int minEP, double epGain) {
        JsonObject json = findGear(errors, content, item);
        if (json != null) {
            expectTerminalAt(errors, item, json, minEP, epGain);
        }
    }

    private static void expectTerminalAt(List<String> errors, String item, JsonObject json, int minEP,
            double epGain) {
        if (json == null) {
            errors.add(item + " has no generated file");
            return;
        }
        if (json.has("maxEP")) {
            errors.add(item + " should be terminal but carries maxEP " + json.get("maxEP"));
        }
        if (json.has("evolution") || json.has("evolutionOptions")) {
            errors.add(item + " should be terminal but names an evolution");
        }
        expectInt(errors, item, "minEP", minEP, json);
        expectRate(errors, item, epGain, json);
    }

    /** The pack's file in Tensura's own gear_existence folder, for spot checks. */
    private static JsonObject findTensuraGear(List<String> errors, Content content, String item) {
        JsonObject json = content.tensuraGearExistence.get(TENSURA_GEAR + file(item));
        if (json == null) {
            errors.add("expected a Tensura gear_existence file for " + item);
        }
        return json;
    }

    private static void expectInert(List<String> errors, Content content, String item) {
        JsonObject json = findGear(errors, content, item);
        if (json == null) {
            return;
        }
        expectInt(errors, item, "maxEP", 0, json);
        if (json.has("evolution") || json.has("evolutionOptions")) {
            errors.add(item + " is inert but names an evolution");
        }
    }

    private static void expectChainRung(List<String> errors, Content content, String item, int maxEP, double epGain) {
        JsonObject json = content.addonChains.get(chainPath(id(item)));
        if (json == null) {
            errors.add("expected an addon_chains file for " + item);
            return;
        }
        expectInt(errors, item + " chain", "maxEP", maxEP, json);
        expectRate(errors, item + " chain", epGain, json);
        if (!json.has("evolution") && !json.has("evolutionOptions")) {
            errors.add(item + " chain names no evolution");
        }
    }

    /** A ladder rung pinned on both sides: the earning floor and the hand-off. */
    private static void expectLadderRung(List<String> errors, String item, JsonObject json, int minEP, int maxEP,
            double epGain, String target) {
        if (json == null) {
            errors.add(item + " has no generated file");
            return;
        }
        expectInt(errors, item, "minEP", minEP, json);
        expectRungAt(errors, item, json, maxEP, epGain, target);
    }

    /** An addon chain that forks, pinned to its gate, rate and exact option list. */
    private static void expectFork(List<String> errors, Content content, String item, int maxEP, double epGain,
            String... targets) {
        JsonObject json = content.addonChains.get(chainPath(id(item)));
        if (json == null) {
            errors.add("expected an addon_chains file for " + item);
            return;
        }
        expectInt(errors, item + " chain", "maxEP", maxEP, json);
        expectRate(errors, item + " chain", epGain, json);
        if (!json.has("evolutionOptions")) {
            errors.add(item + " chain does not fork");
            return;
        }
        com.google.gson.JsonArray options = json.getAsJsonArray("evolutionOptions");
        if (options.size() != targets.length) {
            errors.add(item + " chain should fork into " + targets.length + " items, found " + options.size());
            return;
        }
        for (int i = 0; i < targets.length; i++) {
            if (!targets[i].equals(options.get(i).getAsString())) {
                errors.add(item + " chain option " + i + " should be " + targets[i] + ", found " + options.get(i));
            }
        }
    }

    /**
     * A Tensura silver item that ships at both provider paths: present in each map,
     * with identical content so pack priority has no stale winner to pick.
     */
    private static void expectSilverPair(List<String> errors, Content content, String item) {
        JsonObject tensura = content.tensuraGearExistence.get(TENSURA_GEAR + file("tensura:" + item));
        JsonObject ge = content.geGearExistence.get(GE_TENSURA_GEAR + file("tensura:" + item));
        if (tensura == null) {
            errors.add("expected a Tensura folder file for " + item);
        }
        if (ge == null) {
            errors.add("expected a Gear Evolution folder copy for " + item);
        }
        if (tensura != null && ge != null && !tensura.equals(ge)) {
            errors.add(item + " differs between its two resource paths");
        }
    }

    private static void expectInt(List<String> errors, String item, String field, int wanted, JsonObject json) {
        if (!json.has(field) || json.get(field).getAsInt() != wanted) {
            errors.add(item + " should have " + field + " " + wanted + ", found "
                    + (json.has(field) ? json.get(field) : "nothing"));
        }
    }

    private static void expectRate(List<String> errors, String item, double wanted, JsonObject json) {
        if (!json.has("epGain") || json.get("epGain").getAsDouble() != wanted) {
            errors.add(item + " should earn " + wanted + " EP per point, found "
                    + (json.has("epGain") ? json.get("epGain") : "nothing"));
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
