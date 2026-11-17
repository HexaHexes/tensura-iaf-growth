package org.bitwisemadness.tensuriafgrowth.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.tensuriafgrowth.Growth;
import org.bitwisemadness.tensuriafgrowth.TensuraIafGrowth;
import org.bitwisemadness.tensuriafgrowth.config.DragonEpConfig;
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

    /**
     * The spawn-age stretch redirects attach (issues #82, #84 and #88): the
     * structure piece and feature classes are loaded lazily by world
     * generation, so this test force-loads them without initialization to
     * trigger their transforms - with {@code defaultRequire: 1} a redirect
     * whose target Ice and Fire renamed or removed fails the run here instead
     * of at the first cave, roost or skeleton generation. The dragon subtypes
     * and their base ride along so the corpse-axis redirects
     * ({@code DragonDeadLootTableMixin}, {@code DragonBaseEntitySkeletalMixin})
     * transform before any corpse exists; the skeleton feature itself creates
     * a fire dragon at placement, but all three are pinned here so the run
     * does not depend on test order. The ponder storyboard and skeleton
     * texture mixins are client-only (registered in the config's
     * {@code client} array) and deliberately not touched by this server-side
     * test; they are proven by a client launch instead.
     */
    @GameTest(template = "empty")
    public static void spawn_age_mixins_attach(GameTestHelper helper) {
        ClassLoader loader = TensuraIafGameTests.class.getClassLoader();
        String[] targets = {
            "com.iafenvoy.iceandfire.world.structure.DragonCaveStructure$DragonCavePiece",
            "com.iafenvoy.iceandfire.world.structure.DragonRoostStructure$DragonRoostPiece",
            "com.iafenvoy.iceandfire.world.feature.DragonSkeletonSpawnFeature",
            "com.iafenvoy.iceandfire.entity.DragonBaseEntity",
            "com.iafenvoy.iceandfire.entity.FireDragonEntity",
            "com.iafenvoy.iceandfire.entity.IceDragonEntity",
            "com.iafenvoy.iceandfire.entity.LightningDragonEntity",
        };
        for (String target : targets) {
            try {
                Class.forName(target, false, loader);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Ice and Fire class missing: " + target, e);
            }
        }
        helper.succeed();
    }

    /**
     * The skeleton feature places remains on the canonical corpse axis
     * (issues #84 and #88): skeleton-textured, skeleton-looted, above the
     * flesh windows and below the harvest ladder's refuse gate. Ice and Fire
     * refuses <em>all</em> interaction once the stage reaches
     * {@code min(age / 5, 25)} and corpses are damage-immune, so a stage on
     * the raw stretched axis (issue #86 - the {@code age >= 250} range at cap
     * 512) left worldgen skeletons unharvestable, while a stage below
     * {@code age / 10} brought back the fresh-corpse regression of issue #84.
     * Both caps run the real registered feature with a deterministic roll: at
     * the untouched 128 the stage must equal {@code age / 10} exactly (stock
     * identity, guarding against overcorrection) and at a 4x-stretched cap it
     * must satisfy the full invariant set. The loot-table assertion doubles as
     * the attach proof for {@code DragonDeadLootTableMixin}, and
     * {@code isSkeletal()} for {@code DragonBaseEntitySkeletalMixin}. The
     * fresh share (issue #85) is forced to 0.0 here so the default 1% cannot
     * flake these skeleton invariants - the fresh branch has its own test.
     */
    @GameTest(template = "empty")
    public static void worldgen_skeleton_death_stage_matches_age(GameTestHelper helper) {
        IafCommonConfig config = IafCommonConfig.INSTANCE;
        int oldCap = config.dragon.maxTamedDragonAge.getValue();
        double oldChance = config.dragon.generateSkeletonChance.getValue();
        boolean oldGenerate = config.dragon.generateSkeletons.getValue();
        double oldFresh = DragonEpConfig.FRESH_CORPSE_CHANCE.get();
        try {
            config.dragon.generateSkeletonChance.setValue(1.0);
            config.dragon.generateSkeletons.setValue(true);
            placeAndCheckSkeleton(helper, 128, BlockPos.ZERO);
            placeAndCheckSkeleton(helper, 512, new BlockPos(4, 0, 0));
            helper.succeed();
        } finally {
            config.dragon.maxTamedDragonAge.setValue(oldCap);
            config.dragon.generateSkeletonChance.setValue(oldChance);
            config.dragon.generateSkeletons.setValue(oldGenerate);
            DragonEpConfig.FRESH_CORPSE_CHANCE.set(oldFresh);
        }
    }

    /**
     * The fresh minority of generated dead dragons (issue #85) opens the whole
     * harvest ladder: freshest stage, flesh texture and the male/female loot
     * table, blood reachable below {@code ceiling / 2}, the heart reachable at
     * {@code ceiling / 2 - 1}, and the skeleton flag clear - the opposite
     * pole from the skeleton test, at the 4x cap where every axis is
     * stretched. One placement only, at the cell's origin (the most reliably
     * prepared spot - see {@code placeDragon}'s javadoc), because the fresh
     * branch's invariants do not depend on the cap's identity behaviour.
     */
    @GameTest(template = "empty")
    public static void worldgen_fresh_corpse_ladder_opens(GameTestHelper helper) {
        IafCommonConfig config = IafCommonConfig.INSTANCE;
        int oldCap = config.dragon.maxTamedDragonAge.getValue();
        double oldChance = config.dragon.generateSkeletonChance.getValue();
        boolean oldGenerate = config.dragon.generateSkeletons.getValue();
        double oldFresh = DragonEpConfig.FRESH_CORPSE_CHANCE.get();
        try {
            config.dragon.generateSkeletonChance.setValue(1.0);
            config.dragon.generateSkeletons.setValue(true);
            placeAndCheckFresh(helper, 512, BlockPos.ZERO);
            helper.succeed();
        } finally {
            config.dragon.maxTamedDragonAge.setValue(oldCap);
            config.dragon.generateSkeletonChance.setValue(oldChance);
            config.dragon.generateSkeletons.setValue(oldGenerate);
            DragonEpConfig.FRESH_CORPSE_CHANCE.set(oldFresh);
        }
    }

    /**
     * Places the registered skeleton feature once and pins the stage/age invariant.
     *
     * <p>The feature does not place at the origin: it uses
     * {@code heightmapPos(OCEAN_FLOOR_WG, origin + (8, 0, 8))}, and that height
     * is world state - a primed heightmap reports the real terrain surface, an
     * unprimed one reports y=0 (the gametest structure's own chunks are often
     * unprimed), and a chunk primed over void reports the world floor, where
     * the join does not survive. Before each placement this test plants a stone
     * at the structure plane so the read is position-independent (terrain keeps
     * its surface, void raises to the stone, unprimed keeps reporting y=0 - a
     * harmless mid-air join), and the query below mirrors the feature's own
     * placement for that same height, with a world-wide fallback that separates
     * "placed somewhere unexpected" from "never added" and reports the config
     * state when neither finds anything. The second placement stays tight to
     * the origin because the framework only fully prepares the structure's
     * immediate neighborhood: beyond it a chunk can exist partial (issue #90's
     * family), where a joined entity is never tracked and no query sees it.
     *
     * <p>All four placement gates are forced here - cap, chance, generate and
     * the fresh share (issue #85) - so no config callback or file default can
     * change what the caller asked to place.
     */
    private static DragonBaseEntity placeDragon(
            GameTestHelper helper, int cap, BlockPos offset, double freshChance) {
        IafCommonConfig config = IafCommonConfig.INSTANCE;
        config.dragon.maxTamedDragonAge.setValue(cap);
        config.dragon.generateSkeletonChance.setValue(1.0);
        config.dragon.generateSkeletons.setValue(true);
        DragonEpConfig.FRESH_CORPSE_CHANCE.set(freshChance);
        ResourceLocation featureId = ResourceLocation.parse("iceandfire:spawn_dragon_skeleton_fire");
        Feature<?> registered = BuiltInRegistries.FEATURE.get(featureId);
        if (registered == null) {
            throw new IllegalStateException("feature " + featureId + " is not registered");
        }
        @SuppressWarnings("unchecked")
        Feature<NoneFeatureConfiguration> feature = (Feature<NoneFeatureConfiguration>) registered;
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(offset);
        // Pin the placement heightmap to the structure plane; rationale in the javadoc.
        helper.setBlock(offset.offset(8, 0, 8), Blocks.STONE);
        FeaturePlaceContext<NoneFeatureConfiguration> context = new FeaturePlaceContext<>(
                Optional.empty(),
                level,
                level.getChunkSource().getGenerator(),
                RandomSource.create(42L),
                origin,
                NoneFeatureConfiguration.INSTANCE);
        feature.place(context);

        BlockPos placement =
                level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, origin.offset(8, 0, 8));
        List<DragonBaseEntity> placed =
                level.getEntitiesOfClass(DragonBaseEntity.class, new AABB(placement).inflate(8.0));
        if (placed.isEmpty()) {
            placed = level.getEntitiesOfClass(DragonBaseEntity.class,
                    new AABB(-3.0E7, -3.0E7, -3.0E7, 3.0E7, 3.0E7, 3.0E7));
        }
        if (placed.isEmpty()) {
            throw new IllegalStateException("cap " + cap + ": the skeleton feature added no dragon"
                    + " (placement " + placement + ", chance="
                    + config.dragon.generateSkeletonChance.getValue() + ", generate="
                    + config.dragon.generateSkeletons.getValue() + ", fresh="
                    + DragonEpConfig.FRESH_CORPSE_CHANCE.get() + ", configured cap="
                    + config.dragon.maxTamedDragonAge.getValue() + ")");
        }
        return placed.get(0);
    }

    /** Places with the fresh share forced off and pins the skeleton invariant set. */
    private static void placeAndCheckSkeleton(GameTestHelper helper, int cap, BlockPos offset) {
        DragonBaseEntity dragon = placeDragon(helper, cap, offset, 0.0);
        try {
            if (!dragon.isModelDead()) {
                throw new IllegalStateException("cap " + cap + ": placed dragon is not marked dead");
            }
            int age = dragon.getAgeInDays();
            int stage = dragon.getDeathStage();
            int expected = Growth.corpseStage(age);
            if (stage != expected) {
                throw new IllegalStateException("cap " + cap + ": death stage " + stage
                        + " != corpseStage(" + age + ") = " + expected
                        + " - texture, loot and ladder disagree with the placement");
            }
            if (cap == Growth.VANILLA_MAX_AGE && stage != age / 10) {
                throw new IllegalStateException("cap " + cap + ": stock identity broken - stage "
                        + stage + " != ageInDays / 10 (" + (age / 10) + ")");
            }
            int ceiling = Math.min(age / 5, 25);
            if (stage >= ceiling) {
                throw new IllegalStateException("cap " + cap + ": death stage " + stage
                        + " >= harvest ceiling " + ceiling + " - mobInteract would refuse"
                        + " all interaction, leaving the corpse unharvestable (issue #88)");
            }
            if (stage < ceiling / 2) {
                throw new IllegalStateException("cap " + cap + ": death stage " + stage
                        + " sits below the flesh window " + (ceiling / 2)
                        + " - blood and heart would open on skeleton remains");
            }
            if (!dragon.isSkeletal()) {
                throw new IllegalStateException("cap " + cap + ": isSkeletal() is false at stage "
                        + stage + " - the flag disagrees with the placed stage");
            }
            if (!dragon.getDeadLootTable().getPath().contains("skeleton")) {
                throw new IllegalStateException("cap " + cap + ": dead loot table is "
                        + dragon.getDeadLootTable() + ", not the skeleton table");
            }
        } finally {
            dragon.discard();
        }
    }

    /** Places at 100% fresh (issue #85) and pins the open-ladder invariant set. */
    private static void placeAndCheckFresh(GameTestHelper helper, int cap, BlockPos offset) {
        DragonBaseEntity dragon = placeDragon(helper, cap, offset, 1.0);
        try {
            if (!dragon.isModelDead()) {
                throw new IllegalStateException("cap " + cap + ": placed dragon is not marked dead");
            }
            int age = dragon.getAgeInDays();
            int stage = dragon.getDeathStage();
            if (stage != 0) {
                throw new IllegalStateException("cap " + cap + ": fresh corpse at death stage "
                        + stage + ", expected the freshest stage 0 (issue #85)");
            }
            int threshold = Growth.corpseStage(age);
            if (stage >= threshold) {
                throw new IllegalStateException("cap " + cap + ": stage " + stage
                        + " >= corpseStage " + threshold + " - the remains would render and"
                        + " loot as a skeleton instead of a fresh corpse");
            }
            int ceiling = Math.min(age / 5, 25);
            if (stage >= ceiling / 2) {
                throw new IllegalStateException("cap " + cap + ": blood window closed - stage "
                        + stage + " >= ceiling / 2 (" + (ceiling / 2) + ")");
            }
            if (stage > ceiling / 2 - 1) {
                throw new IllegalStateException("cap " + cap + ": heart unreachable - stage "
                        + stage + " > ceiling / 2 - 1 (" + (ceiling / 2 - 1) + ")");
            }
            if (dragon.isSkeletal()) {
                throw new IllegalStateException("cap " + cap + ": isSkeletal() is true at stage "
                        + stage + " - the flag disagrees with a fresh placement");
            }
            String loot = dragon.getDeadLootTable().getPath();
            if (!loot.contains("male") && !loot.contains("female")) {
                throw new IllegalStateException("cap " + cap + ": dead loot table is "
                        + dragon.getDeadLootTable() + ", not the flesh table");
            }
        } finally {
            dragon.discard();
        }
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
