/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf.processor.preventdecay;

import com.almuradev.content.component.predicate.FunctionPredicate;
import com.almuradev.content.registry.predicate.ResourceLocationPredicateParser;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import com.almuradev.toolbox.util.math.DoubleRange;
import com.google.common.base.MoreObjects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class PreventDecay {
    private static final ResourceLocationPredicateParser<Biome> BIOME_FILTER_BY_REGISTRY_NAME = ResourceLocationPredicateParser.of(IForgeRegistryEntry.Impl::getRegistryName);
    public static final ConfigurationNodeDeserializer<PreventDecay> PARSER = new ConfigurationNodeDeserializer<PreventDecay>() {
        @Override
        public Optional<PreventDecay> deserialize(final ConfigurationNode config) {
            if (config.isVirtual()) {
                return Optional.empty();
            }

            final ConfigurationNode chanceNode = config.getNode(PreventDecayConfig.CHANCE);
            if (chanceNode.isVirtual()) {
                return Optional.empty();
            }

            // Biome-specific chance ranges
            final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeChancePredicates = new HashMap<>();
            // Handle global chance range
            @Nullable DoubleRange globalChanceRange = null;

            if (chanceNode.getValue() instanceof List) {
                for (final ConfigurationNode chanceNodeEntry : chanceNode.getChildrenList()) {
                    globalChanceRange = this.deserializeChance(biomeChancePredicates, chanceNodeEntry).orElse(globalChanceRange);
                }
            } else if (chanceNode.getValue() instanceof Map) {
                globalChanceRange = this.deserializeChance(biomeChancePredicates, chanceNode).orElse(null);
            }

            final ConfigurationNode blockNode = config.getNode(PreventDecayConfig.BLOCK);
            if (blockNode.isVirtual()) {
                return Optional.empty();
            }

            final LazyBlockState state = LazyBlockState.parse(blockNode);

            return Optional.of(new PreventDecay(biomeChancePredicates, globalChanceRange, state));
        }

        private Optional<DoubleRange> deserializeChance(final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeChancePredicates, final ConfigurationNode chanceNodeEntry) {
            final ConfigurationNode biomeChanceNode = chanceNodeEntry.getNode(
                    PreventDecayConfig.BIOME);
            if (!biomeChanceNode.isVirtual()) {
                BIOME_FILTER_BY_REGISTRY_NAME.deserialize(biomeChanceNode)
                        .ifPresent(biomePredicate -> DoubleRange.PARSER.deserialize(chanceNodeEntry)
                                .ifPresent(chanceRange -> biomeChancePredicates.put(biomePredicate, chanceRange)));
                return Optional.empty();
            } else {
                return DoubleRange.PARSER.deserialize(chanceNodeEntry);
            }
        }
    };

    private final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeChancePredicates;
    @Nullable private final DoubleRange globalChanceRange;
    private final Map<Biome, DoubleRange> biomeChanceRanges = new HashMap<>();

    private final LazyBlockState block;

    private PreventDecay(final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeChancePredicates,
                         @Nullable final DoubleRange globalChanceRange, final LazyBlockState block) {
        this.biomeChancePredicates = biomeChancePredicates;
        this.globalChanceRange = globalChanceRange;
        this.block = block;
    }

    @Nullable
    public DoubleRange getOrLoadChanceRangeForBiome(final Biome biome) {
        @Nullable DoubleRange found = this.biomeChanceRanges.get(biome);
        if (found == null) {
            for (final Map.Entry<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> entry : this.biomeChancePredicates.entrySet()) {
                final FunctionPredicate<Biome, ResourceLocation> predicate = entry.getKey();
                if (predicate.test(biome)) {
                    final DoubleRange range = entry.getValue();
                    this.biomeChanceRanges.put(biome, range);
                    found = range;
                    break;
                }
            }
        }

        if (found == null && this.globalChanceRange != null) {
            this.biomeChanceRanges.put(biome, this.globalChanceRange);
            found = this.globalChanceRange;
        }

        return found;
    }

    public LazyBlockState getBlock() {
        return this.block;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("biomeChancePredicates", this.biomeChancePredicates)
                .add("globalChanceRange", this.globalChanceRange)
                .add("block", this.block)
                .toString();
    }
}
