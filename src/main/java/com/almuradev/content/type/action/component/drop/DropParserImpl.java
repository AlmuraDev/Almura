/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.component.drop;

import com.almuradev.content.component.DoubleRanges;
import com.almuradev.content.type.item.definition.ItemDefinition;
import com.almuradev.content.type.item.definition.ItemDefinitionConfig;
import com.almuradev.toolbox.util.math.DoubleRange;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Singleton;

@Singleton
public final class DropParserImpl implements DropParser {

    @Override
    public List<? extends Drop> parse(final ConfigurationNode config) {
        if (config.isVirtual()) {
            return Collections.emptyList();
        }

        // Assume item if drop is a string
        if (config.getValue() instanceof String) {
            return parseSimpleItem(config)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }

        final List<? extends ConfigurationNode> children = config.getChildrenList();
        final List<Drop> drops = new ArrayList<>(children.size());
        for (final ConfigurationNode child : children) {
            if (child.getValue() instanceof String) {
                parseSimpleItem(child).ifPresent(drops::add);
            } else if (child.getValue() instanceof Map) {
                final DoubleRange amount = DoubleRanges.deserialize(child.getNode(DoubleRanges.Config.AMOUNT), DoubleRanges.FIXED_1);
                @Nullable final DoubleRange bonusAmount =
                        DoubleRanges.deserialize(child.getNode(DoubleRanges.Config.BONUS, DoubleRanges.Config.BONUS_AMOUNT)).orElse(null);
                @Nullable final DoubleRange bonusChance =
                        DoubleRanges.deserialize(child.getNode(DoubleRanges.Config.BONUS, DoubleRanges.Config.BONUS_CHANCE)).orElse(null);

                final ConfigurationNode item = child.getNode(ItemDefinitionConfig.ITEM);

                if (!item.isVirtual()) {
                    parseFullItem(item).map(itemDef -> itemDef.asDrop(amount, bonusAmount, bonusChance)).ifPresent
                            (drops::add);
                } else if (!child.getNode(ExperienceDrop.EXPERIENCE).isVirtual()) {
                    drops.add(new ExperienceDrop(amount, bonusAmount, bonusChance));
                }
            }
        }

        return drops;
    }

    private static Optional<ItemDrop> parseSimpleItem(final ConfigurationNode config) {
        return ItemDefinition.PARSER.deserialize(config)
                .map(item -> item.asDrop(DoubleRanges.FIXED_1, null, null));
    }

    private static Optional<ItemDefinition> parseFullItem(final ConfigurationNode config) {
        return ItemDefinition.PARSER.deserialize(config);
    }
}
