/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed.processor.grass;

import com.almuradev.content.component.predicate.FunctionPredicate;
import com.almuradev.content.registry.predicate.ResourceLocationPredicateParser;
import com.almuradev.content.type.block.type.crop.processor.growth.GrowthConfig;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import com.almuradev.toolbox.util.math.DoubleRange;
import com.almuradev.toolbox.util.math.IntRange;
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

public final class Grass {

    private static final ResourceLocationPredicateParser<Biome>
            BIOME_FILTER_BY_REGISTRY_NAME = ResourceLocationPredicateParser.of(IForgeRegistryEntry.Impl::getRegistryName);

    public static final ConfigurationNodeDeserializer<Grass> PARSER = new ConfigurationNodeDeserializer<Grass>() {
        @Override
        public Optional<Grass> deserialize(final ConfigurationNode rootNode) {
            if (rootNode.isVirtual()) {
                return Optional.empty();
            }

            final ConfigurationNode chanceNode = rootNode.getNode(GrassConfig.CHANCE);
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

            // Biome-specific amount range
            final Map<FunctionPredicate<Biome, ResourceLocation>, IntRange> biomeAmountPredicates = new HashMap<>();
            // Handle global temperature required range
            @Nullable IntRange globalAmountRange = null;
            final ConfigurationNode amountNode = rootNode.getNode(GrassConfig.AMOUNT);

            if (amountNode.getValue() instanceof List) {
                for (final ConfigurationNode amountNodeEntry : amountNode.getChildrenList()) {
                    globalAmountRange = this.deserializeAmount(biomeAmountPredicates, amountNodeEntry).orElse(globalAmountRange);
                }
            } else if (amountNode.getValue() instanceof Map) {
                globalAmountRange = this.deserializeAmount(biomeAmountPredicates, amountNode).orElse(null);
            }

            return Optional.of(new Grass(biomeChancePredicates, globalChanceRange, biomeAmountPredicates, globalAmountRange));
        }

        private Optional<DoubleRange> deserializeChance(final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeChancePredicates, final ConfigurationNode chanceNodeEntry) {
            final ConfigurationNode biomeChanceNode = chanceNodeEntry.getNode(GrowthConfig.BIOME);
            if (!biomeChanceNode.isVirtual()) {
                BIOME_FILTER_BY_REGISTRY_NAME.deserialize(biomeChanceNode)
                        .ifPresent(biomePredicate -> DoubleRange.PARSER.deserialize(chanceNodeEntry)
                                .ifPresent(chanceRange -> biomeChancePredicates.put(biomePredicate, chanceRange)));
                return Optional.empty();
            } else {
                return DoubleRange.PARSER.deserialize(chanceNodeEntry);
            }
        }

        private Optional<IntRange> deserializeAmount(final Map<FunctionPredicate<Biome, ResourceLocation>, IntRange> biomeAmountPredicates, final
        ConfigurationNode amountNode) {
            final ConfigurationNode biomeAmountNode = amountNode.getNode(GrassConfig.BIOME);
            if (!biomeAmountNode.isVirtual()) {
                BIOME_FILTER_BY_REGISTRY_NAME.deserialize(biomeAmountNode)
                        .ifPresent(biomePredicate -> IntRange.PARSER.deserialize(amountNode).ifPresent(amountRange -> biomeAmountPredicates.put
                                (biomePredicate, amountRange)));
                return Optional.empty();
            } else {
                return IntRange.PARSER.deserialize(amountNode);
            }
        }
    };

    private final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeChancePredicates;
    @Nullable private final DoubleRange globalChanceRange;
    private final Map<Biome, DoubleRange> biomeChanceRanges = new HashMap<>();

    private final Map<FunctionPredicate<Biome, ResourceLocation>, IntRange> biomeAmountPredicates;
    @Nullable private final IntRange globalAmountRange;
    private final Map<Biome, IntRange> biomeAmountRanges = new HashMap<>();

    private Grass(
            final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeChancePredicates, @Nullable final DoubleRange globalChanceRange,
            final Map<FunctionPredicate<Biome, ResourceLocation>, IntRange> biomeAmountPredicates, @Nullable final IntRange globalAmountRange
    ) {
        this.biomeChancePredicates = biomeChancePredicates;
        this.globalChanceRange = globalChanceRange;

        this.biomeAmountPredicates = biomeAmountPredicates;
        this.globalAmountRange = globalAmountRange;
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

    @Nullable
    public IntRange getOrLoadAmountRequiredRangeForBiome(final Biome biome) {
        @Nullable IntRange found = this.biomeAmountRanges.get(biome);
        if (found == null) {
            for (final Map.Entry<FunctionPredicate<Biome, ResourceLocation>, IntRange> entry : this.biomeAmountPredicates.entrySet()) {
                final FunctionPredicate<Biome, ResourceLocation> predicate = entry.getKey();
                if (predicate.test(biome)) {
                    final IntRange range = entry.getValue();
                    this.biomeAmountRanges.put(biome, range);
                    found = range;
                    break;
                }
            }
        }

        if (found == null && this.globalAmountRange != null) {
            this.biomeAmountRanges.put(biome, this.globalAmountRange);
            found = this.globalAmountRange;
        }

        return found;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("biomeChancePredicates", this.biomeChancePredicates)
                .add("globalChanceRange", this.globalChanceRange)
                .add("biomeAmountPredicates", this.biomeAmountPredicates)
                .add("globalAmountRange", this.globalAmountRange)
                .toString();
    }
}
