/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.tree.processor;

import com.almuradev.content.registry.predicate.ResourceLocationPredicateParser;
import com.almuradev.content.type.tree.Tree;
import com.almuradev.content.type.tree.TreeConfig;
import com.almuradev.content.util.MinimumIntWithVarianceFunctionPredicatePair;
import com.almuradev.toolbox.config.tag.ConfigTag;
import com.almuradev.toolbox.util.math.IntRange;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HeightProcessor implements AbstractTreeProcessor {
    private static final ResourceLocationPredicateParser<Biome> BIOMES = ResourceLocationPredicateParser.of(IForgeRegistryEntry.Impl::getRegistryName);
    private static final ConfigTag TAG = ConfigTag.create(TreeConfig.HEIGHT);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final Tree.Builder builder) {
        final AtomicBoolean foundDefault = new AtomicBoolean();
        final List<MinimumIntWithVarianceFunctionPredicatePair<Biome>> heights = new ArrayList<>();
        config.getChildrenList().forEach(child -> heights.add(new MinimumIntWithVarianceFunctionPredicatePair<>(
                BIOMES.allowingSingleAlwaysTrueDefault(foundDefault, child.getNode(TreeConfig.Height.BIOME), "height"),
                child.getNode(TreeConfig.Height.MIN).getInt(),
                IntRange.PARSER.deserialize(child.getNode(TreeConfig.Height.VARIANCE)).orElseThrow(() -> new IllegalArgumentException(TreeConfig.Height.VARIANCE +  "is missing"))
        )));
        builder.height(heights);
    }
}
