package org.bitwisemadness.tensuriafgrowth.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The {@code tensura_iaf} compat mod's baseline data that this mod's datagen
 * did not already shadow (issue #80, step 1 of the absorption): the Tensura
 * wiring tags that carry the Ice and Fire entities into Tensura's systems
 * (EP, skills, drops, possessions, the no_* gates), the skill block tags,
 * the dubious-ingredient item tags, the Dread Land's magicule modifier, and
 * the IaF-side entity tags the compat's behaviors consult.
 *
 * <p>Every file ships at its exact compat resource path - the additive tags
 * merge with the ecosystem's other contributors, and the shadowed existence
 * files keep the pack-priority race this mod already wins. Parsed through
 * Gson at datagen time; a corrupt port fails the run. The compat mod is
 * All Rights Reserved (by MinhEragon), so these are its DATA files carried
 * at the same paths - the behavior mixins are reimplemented separately and
 * credited.</p>
 */
public final class CompatBaselineContent {

    private CompatBaselineContent() {
    }

    private static final String F0 = """
                {
                  "modifiers": [
                    {
                      "mode": "ADD",
                      "value": 50000.0
                    }
                  ],
                  "regen_modifiers": [
                    {
                      "mode": "MULTIPLY",
                      "value": 2.0
                    }
                  ],
                  "world": "iceandfire:dread_land"
                }
                """;

    private static final String F1 = """
                {
                  "values": [
                    "#tensura:boss_for_hero",
                    "tensura:hinata_sakaguchi",
                    "tensura:gazel_dwargo",
                    "tensura:ifrit_clone",
                    {
                      "id": "tensura_neb:rimuru_ogre_fight",
                      "required": false
                    },
                    {
                      "id": "tensura_neb:rimuru_clone",
                      "required": false
                    },
                    {
                      "id": "tensura_neb:carrion",
                      "required": false
                    },
                    {
                      "id": "tensura_neb:luminous_valentine",
                      "required": false
                    }
                  ]
                }
                """;

    private static final String F2 = """
                {
                  "values": [
                    "#tensura:spiritual",
                    "tensura:basilisk",
                    "tensura:elemental_colossus",
                    "tensura:hinata_sakaguchi",
                    "tensura:charybdis",
                    "tensura:gazel_dwargo",
                    "tensura:ifrit_clone",
                    {
                      "id": "tensura_neb:rimuru_ogre_fight",
                      "required": false
                    },
                    {
                      "id": "tensura_neb:rimuru_clone",
                      "required": false
                    },
                    {
                      "id": "tensura_neb:carrion",
                      "required": false
                    },
                    {
                      "id": "tensura_neb:luminous_valentine",
                      "required": false
                    }
                  ]
                }
                """;

    private static final String F3 = """
                {
                  "values": [
                    "iceandfire:dragonforge_fire_core",
                    "iceandfire:dragonforge_lightning_core"
                  ]
                }
                """;

    private static final String F4 = """
                {
                  "values": [
                    "iceandfire:dragon_ice_spikes"
                  ]
                }
                """;

    private static final String F5 = """
                {
                  "values": [
                    "iceandfire:ghost_chest"
                  ]
                }
                """;

    private static final String F6 = """
                {
                  "values": [
                    "iceandfire:dread_spawner",
                    "iceandfire:dread_portal",
                    "iceandfire:dread_stone",
                    "iceandfire:dread_stone_bricks",
                    "iceandfire:dread_stone_stairs",
                    "iceandfire:dread_stone_slab",
                    "iceandfire:dread_stone_bricks_chiseled",
                    "iceandfire:dread_stone_bricks_cracked",
                    "iceandfire:dread_stone_bricks_mossy",
                    "iceandfire:dread_stone_tile",
                    "iceandfire:dread_stone_face",
                    "iceandfire:dread_torch_wall",
                    "iceandfire:dread_torch",
                    "iceandfire:dreadwood_log",
                    "iceandfire:dreadwood_planks",
                    "iceandfire:dreadwood_planks_lock"
                  ]
                }
                """;

    private static final String F7 = """
                {
                  "values": [
                    "iceandfire:dread_spawner",
                    "iceandfire:dread_portal",
                    "iceandfire:dread_stone",
                    "iceandfire:dread_stone_bricks",
                    "iceandfire:dread_stone_stairs",
                    "iceandfire:dread_stone_slab",
                    "iceandfire:dread_stone_bricks_chiseled",
                    "iceandfire:dread_stone_bricks_cracked",
                    "iceandfire:dread_stone_bricks_mossy",
                    "iceandfire:dread_stone_tile",
                    "iceandfire:dread_stone_face",
                    "iceandfire:dread_torch_wall",
                    "iceandfire:dread_torch",
                    "iceandfire:dreadwood_log",
                    "iceandfire:dreadwood_planks",
                    "iceandfire:dreadwood_planks_lock"
                  ]
                }
                """;

    private static final String F8 = """
                {
                  "values": [
                    "iceandfire:fire_dragon",
                    "iceandfire:dread_lich",
                    "iceandfire:gorgon",
                    "iceandfire:hydra",
                    "iceandfire:ice_dragon",
                    "iceandfire:lightning_dragon"
                  ]
                }
                """;

    private static final String F9 = """
                {
                  "values": [
                    "iceandfire:amphithere",
                    "iceandfire:cockatrice",
                    "iceandfire:deathworm",
                    "iceandfire:hippocampus",
                    "iceandfire:hippogryph",
                    "iceandfire:pixie",
                    "iceandfire:fire_dragon",
                    "iceandfire:ice_dragon",
                    "iceandfire:lightning_dragon"
                  ]
                }
                """;

    private static final String F10 = """
                {
                  "values": [
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F11 = """
                {
                  "values": [
                    "iceandfire:hippocampus",
                    "iceandfire:sea_serpent",
                    "iceandfire:siren",
                    "iceandfire:hydra",
                    "iceandfire:cockatrice",
                    "iceandfire:fire_dragon",
                    "iceandfire:lightning_dragon",
                    "iceandfire:ice_dragon"
                  ]
                }
                """;

    private static final String F12 = """
                {
                  "values": [
                    "iceandfire:dread_beast",
                    "iceandfire:dread_ghoul",
                    "iceandfire:dread_knight",
                    "iceandfire:dread_horse",
                    "iceandfire:dread_lich",
                    "iceandfire:dread_scuttler",
                    "iceandfire:dread_thrall",
                    "iceandfire:ghost",
                    "iceandfire:ice_dragon"
                  ]
                }
                """;

    private static final String F13 = """
                {
                  "values": [
                    "iceandfire:amphithere",
                    "iceandfire:cockatrice",
                    "iceandfire:cyclops",
                    "iceandfire:deathworm",
                    "iceandfire:dread_beast",
                    "iceandfire:dread_ghoul",
                    "iceandfire:dread_knight",
                    "iceandfire:dread_lich",
                    "iceandfire:dread_scuttler",
                    "iceandfire:dread_thrall",
                    "iceandfire:fire_dragon",
                    "iceandfire:ghost",
                    "iceandfire:gorgon",
                    "iceandfire:hippocampus",
                    "iceandfire:hippogryph",
                    "iceandfire:hydra",
                    "iceandfire:ice_dragon",
                    "iceandfire:lightning_dragon",
                    "iceandfire:pixie",
                    "iceandfire:sea_serpent",
                    "iceandfire:siren",
                    "iceandfire:stymphalian_bird",
                    "iceandfire:troll"
                  ]
                }
                """;

    private static final String F14 = """
                {
                  "values": [
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F15 = """
                {
                  "values": [
                    "iceandfire:fire_dragon",
                    "iceandfire:ice_dragon",
                    "iceandfire:dread_lich",
                    "iceandfire:lightning_dragon"
                  ]
                }
                """;

    private static final String F16 = """
                {
                  "values": [
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F17 = """
                {
                  "values": [
                    "iceandfire:fire_dragon"
                  ]
                }
                """;

    private static final String F18 = """
                {
                  "values": [
                    "iceandfire:deathworm",
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F19 = """
                {
                  "values": [
                    "iceandfire:cockatrice",
                    "iceandfire:cyclops",
                    "iceandfire:deathworm",
                    "iceandfire:dread_beast",
                    "iceandfire:dread_ghoul",
                    "iceandfire:dread_knight",
                    "iceandfire:dread_lich",
                    "iceandfire:dread_horse",
                    "iceandfire:dread_scuttler",
                    "iceandfire:dread_thrall",
                    "iceandfire:fire_dragon",
                    "iceandfire:ghost",
                    "iceandfire:gorgon",
                    "iceandfire:hydra",
                    "iceandfire:ice_dragon",
                    "iceandfire:lightning_dragon",
                    "iceandfire:sea_serpent",
                    "iceandfire:siren",
                    "iceandfire:stymphalian_bird",
                    "iceandfire:troll"
                  ]
                }
                """;

    private static final String F20 = """
                {
                  "values": [
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F21 = """
                {
                  "values": [
                    "iceandfire:fire_dragon",
                    "iceandfire:dread_lich",
                    "iceandfire:gorgon",
                    "iceandfire:hydra",
                    "iceandfire:ice_dragon",
                    "iceandfire:lightning_dragon",
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F22 = """
                {
                  "values": [
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F23 = """
                {
                  "values": [
                    "iceandfire:fire_dragon",
                    "iceandfire:dread_lich",
                    "iceandfire:gorgon",
                    "iceandfire:hydra",
                    "iceandfire:ice_dragon",
                    "iceandfire:lightning_dragon",
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F24 = """
                {
                  "values": [
                    "iceandfire:fire_dragon",
                    "iceandfire:dread_lich",
                    "iceandfire:gorgon",
                    "iceandfire:hydra",
                    "iceandfire:ice_dragon",
                    "iceandfire:lightning_dragon",
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F25 = """
                {
                  "values": [
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F26 = """
                {
                  "values": [
                    "iceandfire:dragon_egg",
                    "iceandfire:dragon_skull",
                    "iceandfire:hippogryph_egg",
                    "iceandfire:stone_statue",
                    "iceandfire:deathworm_egg",
                    "iceandfire:cockatrice_egg",
                    "iceandfire:stymphalian_feather",
                    "iceandfire:sea_serpent_bubbles",
                    "iceandfire:chain_tie",
                    "iceandfire:mob_skull",
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F27 = """
                {
                  "values": [
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F28 = """
                {
                  "values": [
                    "iceandfire:stone_statue"
                  ]
                }
                """;

    private static final String F29 = """
                {
                  "values": [
                    "iceandfire:ghost",
                    "iceandfire:pixie"
                  ]
                }
                """;

    private static final String F30 = """
                {
                  "values": [
                    "iceandfire:siren_tear"
                  ]
                }
                """;

    private static final String F31 = """
                {
                  "values": [
                    "iceandfire:summoning_crystal_fire",
                    "iceandfire:summoning_crystal_ice",
                    "iceandfire:summoning_crystal_lightning"
                  ]
                }
                """;

    private static final String F32 = """
                {
                  "values": [
                    "iceandfire:lightning_stew",
                    "iceandfire:fire_stew",
                    "iceandfire:frost_stew",
                    "iceandfire:ambrosia"
                  ]
                }
                """;

    private static final String F33 = """
                {
                  "values": [
                    "iceandfire:pixie_wings",
                    "iceandfire:ectoplasm"
                  ]
                }
                """;

    private static final String F34 = """
                {
                  "values": [
                    "iceandfire:rotten_egg",
                    "iceandfire:cockatrice_eye",
                    "iceandfire:hydra_fang"
                  ]
                }
                """;

    private static final String F35 = """
                {
                  "values": [
                    "iceandfire:fire_dragon_flesh",
                    "iceandfire:fire_dragon_heart",
                    "iceandfire:ice_dragon_flesh",
                    "iceandfire:ice_dragon_heart",
                    "iceandfire:lightning_dragon_flesh",
                    "iceandfire:lightning_dragon_heart",
                    "iceandfire:hydra_heart"
                  ]
                }
                """;

    private static final String F36 = """
                {
                  "values": [
                    "#tensura:boss_for_hero",
                    "tensura:hinata_sakaguchi",
                    "tensura:gazel_dwargo",
                    "tensura:ifrit_clone",
                    {
                      "id": "tensura_neb:rimuru_ogre_fight",
                      "required": false
                    },
                    {
                      "id": "tensura_neb:rimuru_clone",
                      "required": false
                    },
                    {
                      "id": "tensura_neb:carrion",
                      "required": false
                    },
                    {
                      "id": "tensura_neb:luminous_valentine",
                      "required": false
                    }
                  ]
                }
                """;

    /** Every absorbed baseline file, keyed by its path inside the jar. */
    public static Map<String, JsonElement> files() {
        Map<String, JsonElement> files = new LinkedHashMap<>();
        files.put("data/iceandfire/level_magicule/dread_land.json", parse(F0));
        files.put("data/iceandfire/tags/entity_type/cyclops_unliftables.json", parse(F1));
        files.put("data/iceandfire/tags/entity_type/immune_to_gorgon_stone.json", parse(F2));
        files.put("data/tensura/tags/block/skill/heat_source_blocks.json", parse(F3));
        files.put("data/tensura/tags/block/skill/trap_blocks.json", parse(F4));
        files.put("data/tensura/tags/block/skill/treasure_blocks.json", parse(F5));
        files.put("data/tensura/tags/block/skill/unbreakable.json", parse(F6));
        files.put("data/tensura/tags/block/skill/unobtainable.json", parse(F7));
        files.put("data/tensura/tags/entity_type/boss_for_hero.json", parse(F8));
        files.put("data/tensura/tags/entity_type/can_be_named.json", parse(F9));
        files.put("data/tensura/tags/entity_type/can_die_in_labyrinth.json", parse(F10));
        files.put("data/tensura/tags/entity_type/cold_blooded.json", parse(F11));
        files.put("data/tensura/tags/entity_type/cold_source.json", parse(F12));
        files.put("data/tensura/tags/entity_type/drop_crystal.json", parse(F13));
        files.put("data/tensura/tags/entity_type/ep_initiate_excluded.json", parse(F14));
        files.put("data/tensura/tags/entity_type/full_gravity_control.json", parse(F15));
        files.put("data/tensura/tags/entity_type/hinata_neutral.json", parse(F16));
        files.put("data/tensura/tags/entity_type/hot_source.json", parse(F17));
        files.put("data/tensura/tags/entity_type/no_blood.json", parse(F18));
        files.put("data/tensura/tags/entity_type/no_charisma.json", parse(F19));
        files.put("data/tensura/tags/entity_type/no_current_ep_drain.json", parse(F20));
        files.put("data/tensura/tags/entity_type/no_fear.json", parse(F21));
        files.put("data/tensura/tags/entity_type/no_max_ep_plunder.json", parse(F22));
        files.put("data/tensura/tags/entity_type/no_mind_control.json", parse(F23));
        files.put("data/tensura/tags/entity_type/no_possession.json", parse(F24));
        files.put("data/tensura/tags/entity_type/no_skill_plunder.json", parse(F25));
        files.put("data/tensura/tags/entity_type/no_sound.json", parse(F26));
        files.put("data/tensura/tags/entity_type/no_spiritual_damage.json", parse(F27));
        files.put("data/tensura/tags/entity_type/non_living.json", parse(F28));
        files.put("data/tensura/tags/entity_type/spiritual.json", parse(F29));
        files.put("data/tensura/tags/item/dubious/ingredient_brewing.json", parse(F30));
        files.put("data/tensura/tags/item/dubious/ingredient_crystal.json", parse(F31));
        files.put("data/tensura/tags/item/dubious/ingredient_effect.json", parse(F32));
        files.put("data/tensura/tags/item/dubious/ingredient_magic.json", parse(F33));
        files.put("data/tensura/tags/item/dubious/ingredient_poison.json", parse(F34));
        files.put("data/tensura/tags/item/raw_monster_consumables.json", parse(F35));
        files.put("data/tensura_iaf/tags/entity_type/cannot_be_chained.json", parse(F36));
        return files;
    }

    /** Parses one embedded document; a parse failure fails datagen loudly. */
    private static JsonElement parse(String json) {
        return JsonParser.parseString(json);
    }
}
