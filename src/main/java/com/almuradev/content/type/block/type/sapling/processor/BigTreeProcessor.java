/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling.processor;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.registry.predicate.ResourceLocationPredicateParser;
import com.almuradev.content.type.block.type.sapling.SaplingBlock;
import com.almuradev.content.type.block.type.sapling.SaplingBlockConfig;
import com.almuradev.content.type.block.type.sapling.state.SaplingBlockStateDefinitionBuilder;
import com.almuradev.content.type.tree.Tree;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import com.almuradev.content.util.DoubleRanges;
import com.almuradev.toolbox.config.tag.ConfigTag;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BigTreeProcessor implements SaplingBlockContentProcessor.State {
    private static final ResourceLocationPredicateParser<Biome> BIOMES = ResourceLocationPredicateParser.of(IForgeRegistryEntry.Impl::getRegistryName);
    private static final ConfigTag TAG = ConfigTag.create(SaplingBlockConfig.BIG_TREE);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(final ConfigurationNode config, final SaplingBlock.Builder builder, final SaplingBlockStateDefinitionBuilder definition) {
        final AtomicBoolean foundDefault = new AtomicBoolean();
        final List<DoubleRangeFunctionPredicatePair<Biome>> chances = new ArrayList<>();
        config.getNode(SaplingBlockConfig.BigTree.CHANCE).getChildrenList().forEach(child -> {
            chances.add(new DoubleRangeFunctionPredicatePair<>(
                    BIOMES.allowingSingleAlwaysTrueDefault(foundDefault, child.getNode(SaplingBlockConfig.BigTree.Chance.BIOME), "chance"),
                    DoubleRanges.deserialize(child).orElseThrow(() -> new IllegalArgumentException("chance value is missing"))
            ));
        });
        definition.bigTree(CatalogDelegate.namespaced(Tree.class, config.getNode(SaplingBlockConfig.BigTree.TYPE)), chances);
    }
}
