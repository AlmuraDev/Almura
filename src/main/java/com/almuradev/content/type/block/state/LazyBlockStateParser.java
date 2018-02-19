/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.state;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.block.state.value.StateValue;
import com.almuradev.content.type.block.state.value.StateValues;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class LazyBlockStateParser {
    static LazyBlockState parse(final ConfigurationNode config) {
        if (config.getValue() instanceof String) {
            return new StatelessLazyBlockState(block(config.getString()));
        }
        if (config.getNode("state").isVirtual()) {
            return new StatelessLazyBlockState(block(config.getString()));
        }
        return new StatefulLazyBlockState(
                block(config.getNode("id").getString()),
                properties(config.getNode("state"))
        );
    }

    private static Delegate<BlockType> block(final String id) {
        return CatalogDelegate.namespaced(BlockType.class, id);
    }

    private static Map<String, StateValue<? extends Comparable<?>>> properties(final ConfigurationNode config) {
        if (config.isVirtual()) {
            return Collections.emptyMap();
        }
        final Map<String, StateValue<? extends Comparable<?>>> properties = new HashMap<>();
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : config.getChildrenMap().entrySet()) {
            properties.put(String.valueOf(entry.getKey()), StateValues.create(entry.getValue()));
        }
        return properties;
    }
}
