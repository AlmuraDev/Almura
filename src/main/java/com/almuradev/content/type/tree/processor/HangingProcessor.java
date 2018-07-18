/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.tree.processor;

import com.almuradev.content.registry.predicate.ResourceLocationPredicateParser;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.tree.Tree;
import com.almuradev.content.type.tree.TreeConfig;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import com.almuradev.content.util.DoubleRanges;
import com.almuradev.toolbox.config.tag.ConfigTag;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HangingProcessor implements AbstractTreeProcessor {
    private static final ResourceLocationPredicateParser<Biome> BIOMES = ResourceLocationPredicateParser.of(IForgeRegistryEntry.Impl::getRegistryName);
    private static final ConfigTag TAG = ConfigTag.create(TreeConfig.HANGING);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final Tree.Builder builder) {
        final AtomicBoolean foundDefault = new AtomicBoolean();
        final List<DoubleRangeFunctionPredicatePair<Biome>> heights = new ArrayList<>();
        config.getNode(TreeConfig.Block.CHANCE).getChildrenList().forEach(child -> heights.add(new DoubleRangeFunctionPredicatePair<>(
                BIOMES.allowingSingleAlwaysTrueDefault(foundDefault, child.getNode(TreeConfig.Block.Chance.BIOME), "hanging"),
                DoubleRanges.deserialize(child).orElseThrow(() -> new IllegalArgumentException(TreeConfig.Block.CHANCE +  "is missing"))
        )));
        builder.hanging(LazyBlockState.parse(config.getNode(TreeConfig.Block.BLOCK)), heights);
    }
}
