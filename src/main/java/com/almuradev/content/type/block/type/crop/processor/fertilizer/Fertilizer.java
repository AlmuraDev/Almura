/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.processor.fertilizer;

import com.almuradev.content.type.item.definition.ItemDefinition;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import com.almuradev.toolbox.util.math.DoubleRange;
import com.google.common.base.MoreObjects;
import com.google.common.collect.MoreCollectors;
import net.minecraft.item.ItemStack;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public final class Fertilizer {
    public static final ConfigurationNodeDeserializer<Fertilizer> PARSER = new ConfigurationNodeDeserializer<Fertilizer>() {
        @Override
        public Optional<Fertilizer> deserialize(final ConfigurationNode config) {
            if (config.isVirtual()) {
                return Optional.empty();
            }

            final Map<ItemDefinition, DoubleRange> itemDefinitionChances = new HashMap<>();

            if (config.getValue() instanceof Map) {
                final ConfigurationNode item = config.getNode(FertilizerConfig.ITEM);
                final ConfigurationNode chance = config.getNode(FertilizerConfig.CHANCE);
                this.deserializeDefinitions(itemDefinitionChances, item, chance);
            } else if (config.getValue() instanceof List) {
                for (final ConfigurationNode entry : config.getChildrenList()) {
                    final ConfigurationNode item = entry.getNode(FertilizerConfig.ITEM);
                    final ConfigurationNode chance = entry.getNode(FertilizerConfig.CHANCE);
                    this.deserializeDefinitions(itemDefinitionChances, item, chance);
                }
            }

            if (itemDefinitionChances.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new Fertilizer(itemDefinitionChances));
        }

        private void deserializeDefinitions(final Map<ItemDefinition, DoubleRange> itemDefinitionChances, final ConfigurationNode itemNode, final ConfigurationNode chanceNode) {
            if (chanceNode.isVirtual()) {
                return;
            }
            final DoubleRange chanceRange = DoubleRange.PARSER.deserialize(chanceNode).orElse(null);
            if (chanceRange != null) {
                if (itemNode.isVirtual()) {
                    return;
                }
                if (itemNode.getValue() instanceof List) {
                    for (final ConfigurationNode itemNodeEntry : itemNode.getChildrenList()) {
                        ItemDefinition.PARSER.deserialize(itemNodeEntry).ifPresent(definition -> itemDefinitionChances.put(definition, chanceRange));
                    }
                } else {
                    ItemDefinition.PARSER.deserialize(itemNode).ifPresent(definition -> itemDefinitionChances.put(definition, chanceRange));
                }
            }
        }
    };

    private final Map<ItemDefinition, DoubleRange> itemDefinitionChances;

    private Fertilizer(final Map<ItemDefinition, DoubleRange> itemDefinitionChances) {
        this.itemDefinitionChances = itemDefinitionChances;
    }

    @Nullable
    public DoubleRange getOrLoadChanceRangeForItem(final ItemStack item) {
        return this.itemDefinitionChances.entrySet().stream()
                .filter(entry -> entry.getKey().test(item))
                .map(Map.Entry::getValue)
                .collect(MoreCollectors.toOptional())
                .orElse(null);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("itemDefinitionChances", this.itemDefinitionChances)
                .toString();
    }
}
