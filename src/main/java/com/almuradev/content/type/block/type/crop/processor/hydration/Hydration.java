/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.processor.hydration;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.block.predicate.LazyBlockState;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import com.google.common.base.MoreObjects;
import net.minecraft.block.state.IBlockState;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class Hydration {

    public static final ConfigurationNodeDeserializer<Hydration> PARSER = config -> {
        if (config.isVirtual()) {
            return Optional.empty();
        }

        final ConfigurationNode maxRadiusNode = config.getNode(HydrationConfig.MAX_RADIUS);
        if (maxRadiusNode.isVirtual()) {
            return Optional.empty();
        }

        final int maxRadius = maxRadiusNode.getInt();

        final ConfigurationNode stateNode = config.getNode(HydrationConfig.STATE);
        if (stateNode.isVirtual()) {
            return Optional.empty();
        }

        final Set<LazyBlockState> states = new HashSet<>();

        if (stateNode.getValue() instanceof List) {
            for (final ConfigurationNode stateEntryNode : stateNode.getChildrenList()) {
                if (stateEntryNode.getValue() instanceof Map) {
                    for (final Map.Entry<Object, ? extends ConfigurationNode> blockStateEntryNodeValueEntry : stateEntryNode.getChildrenMap().entrySet()) {
                        states.add(LazyBlockState.parse(CatalogDelegate.create(BlockType.class, String.valueOf(blockStateEntryNodeValueEntry.getKey())), blockStateEntryNodeValueEntry.getValue()));
                    }
                } else {
                    states.add(LazyBlockState.parse(CatalogDelegate.create(BlockType.class, stateEntryNode.getString()), stateEntryNode));
                }
            }
        } else if (stateNode.getValue() instanceof Map) {
            for (final Map.Entry<Object, ? extends ConfigurationNode> stateEntryNode : stateNode.getChildrenMap().entrySet()) {
                states.add(LazyBlockState.parse(CatalogDelegate.create(BlockType.class, String.valueOf(stateEntryNode.getKey())), stateEntryNode.getValue()));
            }
        } else {
            states.add(LazyBlockState.parse(CatalogDelegate.create(BlockType.class, stateNode.getString()), stateNode));
        }

        return Optional.of(new Hydration(states, maxRadius));
    };

    private final Set<LazyBlockState> states;
    private final int maxRadius;

    private Hydration(final Set<LazyBlockState> states, final int maxRadius) {
        this.states = states;
        this.maxRadius = maxRadius;
    }

    public int getMaxRadius() {
        return this.maxRadius;
    }

    public boolean doesStateMatch(final IBlockState state) {
        for (final LazyBlockState lazy : this.states) {
            if (lazy.partialTest(state)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("states", this.states)
                .add("maxRadius", this.maxRadius)
                .toString();
    }
}
