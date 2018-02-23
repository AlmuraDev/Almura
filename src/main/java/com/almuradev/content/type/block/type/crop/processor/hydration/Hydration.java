/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.processor.hydration;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.block.state.LazyBlockState;
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

        final ConfigurationNode blockStateNode = config.getNode(HydrationConfig.BLOCKSTATE);
        if (blockStateNode.isVirtual()) {
            return Optional.empty();
        }

        final Set<LazyBlockState> blockStates = new HashSet<>();

        if (blockStateNode.getValue() instanceof List) {
            for (final ConfigurationNode blockStateEntryNode : blockStateNode.getChildrenList()) {
                if (blockStateEntryNode.getValue() instanceof Map) {
                    for (final Map.Entry<Object, ? extends ConfigurationNode> blockStateEntryNodeValueEntry : blockStateEntryNode.getChildrenMap().entrySet()) {
                        blockStates.add(LazyBlockState.parse(CatalogDelegate.create(BlockType.class, String.valueOf(blockStateEntryNodeValueEntry.getKey())), blockStateEntryNodeValueEntry.getValue()));
                    }
                } else {
                    blockStates.add(LazyBlockState.parse(CatalogDelegate.create(BlockType.class, blockStateEntryNode.getString()), blockStateEntryNode));
                }
            }
        } else if (blockStateNode.getValue() instanceof Map) {
            for (final Map.Entry<Object, ? extends ConfigurationNode> blockStateEntryNode : blockStateNode.getChildrenMap().entrySet()) {
                blockStates.add(LazyBlockState.parse(CatalogDelegate.create(BlockType.class, String.valueOf(blockStateEntryNode.getKey())), blockStateEntryNode.getValue()));
            }
        } else {
            blockStates.add(LazyBlockState.parse(CatalogDelegate.create(BlockType.class, blockStateNode.getString()), blockStateNode));
        }

        return Optional.of(new Hydration(blockStates, maxRadius));
    };

    private final Set<LazyBlockState> blockStates;
    private final int maxRadius;

    private Hydration(final Set<LazyBlockState> blockStates, final int maxRadius) {
        this.blockStates = blockStates;
        this.maxRadius = maxRadius;
    }

    public int getMaxRadius() {
        return this.maxRadius;
    }

    public boolean doesStateMatch(final IBlockState blockState) {
        for (final LazyBlockState lazy : this.blockStates) {
            if (lazy.partialTest(blockState)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("blockStates", this.blockStates)
                .add("maxRadius", this.maxRadius)
                .toString();
    }
}
