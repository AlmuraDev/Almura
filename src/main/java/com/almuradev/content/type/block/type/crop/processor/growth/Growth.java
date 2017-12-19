/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.processor.growth;

import com.almuradev.content.component.predicate.FunctionPredicate;
import com.almuradev.content.registry.predicate.ResourceLocationPredicateParser;
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
public final class Growth {

    private static final ResourceLocationPredicateParser<Biome> BIOME_FILTER_BY_REGISTRY_NAME = ResourceLocationPredicateParser.of(IForgeRegistryEntry.Impl::getRegistryName);

    public static final ConfigurationNodeDeserializer<Growth> PARSER = new ConfigurationNodeDeserializer<Growth>() {
        @Override
        public Optional<Growth> deserialize(ConfigurationNode config) {
            if (config.isVirtual()) {
                return Optional.empty();
            }

            final ConfigurationNode chanceNode = config.getNode(GrowthConfig.CHANCE);
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

            // Biome-specific light range
            final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeLightPredicates = new HashMap<>();
            // Handle global temperature required range
            @Nullable DoubleRange globalLightRange = null;
            final ConfigurationNode lightNode = config.getNode(GrowthConfig.LIGHT);

            if (lightNode.getValue() instanceof List) {
                for (final ConfigurationNode lightNodeEntry : lightNode.getChildrenList()) {
                    globalLightRange = this.deserializeLight(biomeLightPredicates, lightNodeEntry).orElse(globalLightRange);
                }
            } else if (lightNode.getValue() instanceof Map) {
                globalLightRange = this.deserializeLight(biomeLightPredicates, lightNode).orElse(null);
            }

            // Biome-specific temperature requirements
            final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeTemperatureRequiredPredicates = new HashMap<>();
            // Handle global temperature required range
            @Nullable DoubleRange globalTemperatureRequiredRange = null;

            final ConfigurationNode temperatureRequiredNode = config.getNode(GrowthConfig.TEMPERATURE_REQUIRED);
            if (temperatureRequiredNode.getValue() instanceof List) {
                for (final ConfigurationNode temperatureRequiredNodeEntry : temperatureRequiredNode.getChildrenList()) {
                    globalTemperatureRequiredRange = this.deserializeTemperature(biomeTemperatureRequiredPredicates, temperatureRequiredNodeEntry).orElse(globalTemperatureRequiredRange);
                }
            } else if (temperatureRequiredNode.getValue() instanceof Map) {
                globalTemperatureRequiredRange = this.deserializeTemperature(biomeTemperatureRequiredPredicates, temperatureRequiredNode).orElse(null);
            }

            return Optional.of(new Growth(biomeChancePredicates, globalChanceRange, biomeLightPredicates, globalLightRange, biomeTemperatureRequiredPredicates, globalTemperatureRequiredRange));
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

        private Optional<DoubleRange> deserializeLight(final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeLightPredicates, final ConfigurationNode lightNode) {
            final ConfigurationNode biomeLightNode = lightNode.getNode(GrowthConfig.BIOME);
            if (!biomeLightNode.isVirtual()) {
                BIOME_FILTER_BY_REGISTRY_NAME.deserialize(biomeLightNode)
                        .ifPresent(biomePredicate -> DoubleRange.PARSER.deserialize(biomeLightNode)
                                .ifPresent(lightRange -> biomeLightPredicates.put(biomePredicate, lightRange)));
                return Optional.empty();
            } else {
                return DoubleRange.PARSER.deserialize(biomeLightNode);
            }
        }

        private Optional<DoubleRange> deserializeTemperature(final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeTemperatureRequiredPredicates, final ConfigurationNode temperatureRequiredNodeEntry) {
            final ConfigurationNode biomeTemperatureRequiredNode = temperatureRequiredNodeEntry.getNode(GrowthConfig.BIOME);
            if (!biomeTemperatureRequiredNode.isVirtual()) {
                BIOME_FILTER_BY_REGISTRY_NAME.deserialize(biomeTemperatureRequiredNode)
                        .ifPresent(biomePredicate -> DoubleRange.PARSER.deserialize(temperatureRequiredNodeEntry).
                                ifPresent(temperatureRequiredRange -> biomeTemperatureRequiredPredicates.put(biomePredicate, temperatureRequiredRange)));
                return Optional.empty();
            } else {
                return DoubleRange.PARSER.deserialize(temperatureRequiredNodeEntry);
            }
        }
    };

    private final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeChancePredicates;
    @Nullable private final DoubleRange globalChanceRange;
    private final Map<Biome, DoubleRange> biomeChanceRanges = new HashMap<>();

    private final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeLightPredicates;
    @Nullable private final DoubleRange globalLightRange;
    private final Map<Biome, DoubleRange> biomeLightRanges = new HashMap<>();

    private final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeTemperatureRequiredPredicates;
    @Nullable private final DoubleRange globalTemperatureRequiredRange;
    private final Map<Biome, DoubleRange> biomeTemperatureRequiredRanges = new HashMap<>();

    private Growth(
            final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeChancePredicates, @Nullable final DoubleRange globalChanceRange,
            final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeLightPredicates, @Nullable final DoubleRange globalLightRange,
            final Map<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> biomeTemperatureRequiredPredicates, @Nullable final DoubleRange globalTemperatureRequiredRange
    ) {
        this.biomeChancePredicates = biomeChancePredicates;
        this.globalChanceRange = globalChanceRange;

        this.biomeLightPredicates = biomeLightPredicates;
        this.globalLightRange = globalLightRange;

        this.biomeTemperatureRequiredPredicates = biomeTemperatureRequiredPredicates;
        this.globalTemperatureRequiredRange = globalTemperatureRequiredRange;
    }

    @Nullable
    public DoubleRange getOrLoadChanceRangeForBiome(Biome biome) {
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
    public DoubleRange getOrLoadTemperatureRequiredRangeForBiome(Biome biome) {
        @Nullable DoubleRange found = this.biomeTemperatureRequiredRanges.get(biome);
        if (found == null) {
            for (final Map.Entry<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> entry : this.biomeTemperatureRequiredPredicates.entrySet()) {
                final FunctionPredicate<Biome, ResourceLocation> predicate = entry.getKey();
                if (predicate.test(biome)) {
                    final DoubleRange range = entry.getValue();
                    this.biomeTemperatureRequiredRanges.put(biome, range);
                    found = range;
                    break;
                }
            }
        }

        if (found == null && this.globalTemperatureRequiredRange != null) {
            this.biomeTemperatureRequiredRanges.put(biome, this.globalTemperatureRequiredRange);
            found = this.globalTemperatureRequiredRange;
        }

        return found;
    }

    @Nullable
    public DoubleRange getOrLoadLightRangeForBiome(Biome biome) {
        @Nullable DoubleRange found = this.biomeLightRanges.get(biome);
        if (found == null) {
            for (Map.Entry<FunctionPredicate<Biome, ResourceLocation>, DoubleRange> entry : this.biomeLightPredicates.entrySet()) {
                final FunctionPredicate<Biome, ResourceLocation> predicate = entry.getKey();
                if (predicate.test(biome)) {
                    final DoubleRange range = entry.getValue();
                    this.biomeLightRanges.put(biome, range);
                    found = range;
                    break;
                }
            }
        }

        if (found == null && this.globalLightRange != null) {
            this.biomeLightRanges.put(biome, this.globalLightRange);
            found = this.globalLightRange;
        }

        return found;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("biomeChancePredicates", this.biomeChancePredicates)
                .add("globalChanceRange", this.globalChanceRange)
                .add("biomeLightPredicates", this.biomeLightPredicates)
                .add("globalLightRange", this.globalLightRange)
                .add("biomeTemperatureRequiredPredicates", this.biomeTemperatureRequiredPredicates)
                .add("globalTemperatureRequiredRange", this.globalTemperatureRequiredRange)
                .toString();
    }
}
