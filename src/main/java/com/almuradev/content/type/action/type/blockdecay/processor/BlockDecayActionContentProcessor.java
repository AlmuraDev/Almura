/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdecay.processor;

import com.almuradev.content.registry.predicate.ResourceLocationPredicateParser;
import com.almuradev.content.type.action.ActionContentProcessor;
import com.almuradev.content.type.action.component.drop.DropParser;
import com.almuradev.content.type.action.type.blockdecay.BlockDecayAction;
import com.almuradev.content.type.action.type.blockdecay.BlockDecayActionConfig;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import com.almuradev.content.util.DoubleRanges;
import com.almuradev.toolbox.config.processor.AbstractArrayConfigProcessor;
import com.almuradev.toolbox.config.processor.ArrayConfigProcessor;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

public final class BlockDecayActionContentProcessor implements ArrayConfigProcessor<BlockDecayAction.Builder>, AbstractArrayConfigProcessor.EqualTreatment<BlockDecayAction.Builder>,
        ActionContentProcessor<BlockDecayAction, BlockDecayAction.Builder> {
    private static final ResourceLocationPredicateParser<Biome> BIOMES = ResourceLocationPredicateParser.of(IForgeRegistryEntry.Impl::getRegistryName);
    private final DropParser drop;

    @Inject
    private BlockDecayActionContentProcessor(final DropParser drop) {
        this.drop = drop;
    }

    @Override
    public void processChild(final ConfigurationNode config, final int index, final BlockDecayAction.Builder builder) {
        final BlockDecayAction.Entry.Builder entry = builder.entry(index);

        final AtomicBoolean foundDefault = new AtomicBoolean();
        final List<DoubleRangeFunctionPredicatePair<Biome>> chances = new ArrayList<>();
        config.getNode(BlockDecayActionConfig.CHANCE).getChildrenList().forEach(child -> chances.add(new DoubleRangeFunctionPredicatePair<>(
                BIOMES.allowingSingleAlwaysTrueDefault(foundDefault, child.getNode(BlockDecayActionConfig.Chance.BIOME), "chance"),
                DoubleRanges.deserialize(child).orElseThrow(() -> new IllegalArgumentException("chance value is missing"))
        )));

        entry.chance(chances);
        entry.drop(this.drop.parse(config.getNode(BlockDecayActionConfig.DROP)));
    }
}
