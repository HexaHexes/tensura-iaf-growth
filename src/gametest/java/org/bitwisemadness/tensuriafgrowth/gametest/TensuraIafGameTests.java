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
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.AABB;
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

    /**
     * The spawn-age stretch redirects attach (issues #82 and #84): the
     * structure piece and feature classes are loaded lazily by world
     * generation, so this test force-loads them without initialization to
     * trigger their transforms - with {@code defaultRequire: 1} a redirect
     * whose target Ice and Fire renamed or removed fails the run here instead
     * of at the first cave, roost or skeleton generation. The ponder storyboard
     * mixin is client-only (registered in the config's {@code client} array)
     * and deliberately not touched by this server-side test; it is proven by a
     * client launch instead.
     */
    @GameTest(template = "empty")
    public static void spawn_age_mixins_attach(GameTestHelper helper) {
        ClassLoader loader = TensuraIafGameTests.class.getClassLoader();
        String[] targets = {
            "com.iafenvoy.iceandfire.world.structure.DragonCaveStructure$DragonCavePiece",
            "com.iafenvoy.iceandfire.world.structure.DragonRoostStructure$DragonRoostPiece",
            "com.iafenvoy.iceandfire.world.feature.DragonSkeletonSpawnFeature",
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
     * The skeleton feature keeps the death stage on the stretched age axis
     * (issue #84). Ice and Fire renders and loots the placed remains as a
     * skeleton only while {@code deathStage >= ageInDays / 10}; the feature
     * derives the stage from the unstretched roll, so under a raised cap the
     * comparison would fail and every placement would come out a fresh,
     * flesh-lootable corpse - the regression this test pins. Both caps run the
     * real registered feature with a deterministic roll: at the default 128
     * the stage must equal {@code age / 10} exactly (the stock identity,
     * guarding against overcorrection) and at a 4x-stretched cap it must
     * still reach the threshold.
     */
    @GameTest(template = "empty")
    public static void worldgen_skeleton_death_stage_matches_age(GameTestHelper helper) {
        IafCommonConfig config = IafCommonConfig.INSTANCE;
        int oldCap = config.dragon.maxTamedDragonAge.getValue();
        double oldChance = config.dragon.generateSkeletonChance.getValue();
        boolean oldGenerate = config.dragon.generateSkeletons.getValue();
        try {
            config.dragon.generateSkeletonChance.setValue(1.0);
            config.dragon.generateSkeletons.setValue(true);
            placeAndCheckSkeleton(helper, 128, BlockPos.ZERO);
            placeAndCheckSkeleton(helper, 512, new BlockPos(32, 0, 0));
            helper.succeed();
        } finally {
            config.dragon.maxTamedDragonAge.setValue(oldCap);
            config.dragon.generateSkeletonChance.setValue(oldChance);
            config.dragon.generateSkeletons.setValue(oldGenerate);
        }
    }

    /** Places the registered skeleton feature once and pins the stage/age invariant. */
    private static void placeAndCheckSkeleton(GameTestHelper helper, int cap, BlockPos offset) {
        IafCommonConfig.INSTANCE.dragon.maxTamedDragonAge.setValue(cap);
        ResourceLocation featureId = ResourceLocation.parse("iceandfire:spawn_dragon_skeleton_fire");
        Feature<?> registered = BuiltInRegistries.FEATURE.get(featureId);
        if (registered == null) {
            throw new IllegalStateException("feature " + featureId + " is not registered");
        }
        @SuppressWarnings("unchecked")
        Feature<NoneFeatureConfiguration> feature = (Feature<NoneFeatureConfiguration>) registered;
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(offset);
        FeaturePlaceContext<NoneFeatureConfiguration> context = new FeaturePlaceContext<>(
                Optional.empty(),
                level,
                level.getChunkSource().getGenerator(),
                RandomSource.create(42L),
                origin,
                NoneFeatureConfiguration.INSTANCE);
        feature.place(context);

        List<DragonBaseEntity> placed =
                level.getEntitiesOfClass(DragonBaseEntity.class, new AABB(origin).inflate(64.0));
        if (placed.isEmpty()) {
            throw new IllegalStateException("cap " + cap + ": the skeleton feature placed no dragon");
        }
        DragonBaseEntity dragon = placed.get(0);
        try {
            if (!dragon.isModelDead()) {
                throw new IllegalStateException("cap " + cap + ": placed dragon is not marked dead");
            }
            int stage = dragon.getDeathStage();
            int threshold = dragon.getAgeInDays() / 10;
            if (stage != threshold) {
                throw new IllegalStateException("cap " + cap + ": death stage " + stage
                        + " != ageInDays / 10 (" + threshold + ") - the remains would"
                        + " render and loot as a fresh corpse instead of a skeleton");
            }
            if (!dragon.getDeadLootTable().getPath().contains("skeleton")) {
                throw new IllegalStateException("cap " + cap + ": dead loot table is "
                        + dragon.getDeadLootTable() + ", not the skeleton table");
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
