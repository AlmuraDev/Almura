/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.tree.processor;

import com.almuradev.content.registry.predicate.ResourceLocationPredicateParser;
import com.almuradev.content.type.generation.type.feature.tree.TreeGenerator;
import com.almuradev.content.type.generation.type.feature.tree.TreeGeneratorConfig;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import com.almuradev.content.util.DoubleRanges;
import com.almuradev.toolbox.config.tag.ConfigTag;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ChanceProcessor implements AbstractTreeProcessor {
    private static final ResourceLocationPredicateParser<Biome> BIOMES = ResourceLocationPredicateParser.of(IForgeRegistryEntry.Impl::getRegistryName);
    private static final ConfigTag TAG = ConfigTag.create(TreeGeneratorConfig.CHANCE);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final TreeGenerator.Builder builder) {
        final AtomicBoolean foundDefault = new AtomicBoolean();
        final List<DoubleRangeFunctionPredicatePair<Biome>> biomes = new ArrayList<>();
        config.getChildrenList().forEach(child -> {
            biomes.add(new DoubleRangeFunctionPredicatePair<>(
                    BIOMES.allowingSingleAlwaysTrueDefault(foundDefault, child.getNode(TreeGeneratorConfig.Chance.BIOME), "chance"),
                    DoubleRanges.deserialize(child).orElseThrow(() -> new IllegalArgumentException("chance value is missing"))
            ));
        });
        builder.biomes(biomes);
    }
}
