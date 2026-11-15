package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * The absorbed compat mod's entity tag constants (issue #80) - formerly
 * {@code io.github.manasmods.tensura_iaf.data.TensuraIafEntityTags}. The tag
 * FILES ship from this mod's datagen ({@code CompatBaselineContent}) at the
 * same paths, so datapacks and the ported mixins read the same ids.
 */
public final class CompatEntityTags {

    /** Entities the chain item must refuse ({@code data/tensura_iaf/...}). */
    public static final TagKey<EntityType<?>> CANNOT_BE_CHAINED =
            TagKey.create(Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath("tensura_iaf", "cannot_be_chained"));

    private CompatEntityTags() {
    }
}
