/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.processor.hydration;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import com.google.common.base.MoreObjects;
import net.minecraft.block.state.IBlockState;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.HashSet;
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

        final ConfigurationNode blockStateNode = config.getNode(HydrationConfig.BLOCK);
        if (blockStateNode.isVirtual()) {
            return Optional.empty();
        }

        final Set<LazyBlockState> blocks = new HashSet<>();
        for (final ConfigurationNode block : config.getNode(HydrationConfig.BLOCK).getChildrenList()) {
            blocks.add(LazyBlockState.parse(block));
        }

        return Optional.of(new Hydration(blocks, maxRadius));
    };

    private final Set<LazyBlockState> blockStates;
    private final int maxRadius;

    private Hydration(final Set<LazyBlockState> blockStates, final int maxRadius) {
        this.blockStates = blockStates;
        this.maxRadius = maxRadius;
    }

    public Set<LazyBlockState> blockStates() {
        return this.blockStates;
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
