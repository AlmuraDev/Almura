/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.predicate;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.component.delegate.LazyDelegate;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockType;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class LazyBlockState implements Supplier<IBlockState> {

    private static final ConfigurationNodeDeserializer<Map<String, String>> PROPERTY_PARSER = config -> {
        if (config.isVirtual()) {
            return Optional.empty();
        }
        final Map<String, String> properties = new HashMap<>();
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : config.getChildrenMap().entrySet()) {
            properties.put(String.valueOf(entry.getKey()), entry.getValue().getString());
        }
        return Optional.of(properties);
    };
    private final Delegate<IBlockState> state;
    private final Set<IProperty<? extends Comparable<?>>> properties = new HashSet<>();

    public static LazyBlockState parse(final Delegate<BlockType> block, final ConfigurationNode properties) {
        return new LazyBlockState(block, PROPERTY_PARSER.deserialize(properties, Collections.emptyMap()));
    }

    private LazyBlockState(final Delegate<BlockType> block, final Map<String, String> properties) {
        this.state = new LazyDelegate<IBlockState>() {
            @Nullable
            @Override
            protected IBlockState load() {
                return LazyBlockState.this.resolve((Block) block.get(), properties);
            }
        };
    }

    @Nullable
    protected <T extends Comparable<T>> IBlockState resolve(@Nullable final Block block, final Map<String, String> properties) {
        if (block == null) {
            return null;
        }
        final BlockStateContainer definition = block.getBlockState();
        IBlockState state = block.getDefaultState();
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            @Nullable final IProperty<T> property = (IProperty<T>) definition.getProperty(entry.getKey());
            if (property != null) {
                @Nullable final T value = property.parseValue(entry.getValue()).orNull();
                if (value != null) {
                    state = state.withProperty(property, value);
                    this.properties.add(property);
                }
            }
        }
        return state;
    }

    @Override
    public IBlockState get() {
        return this.state.get();
    }

    public boolean fullTest(final IBlockState state) {
        return this.get().equals(state);
    }

    public boolean partialTest(final IBlockState state) {
        final IBlockState a = this.get();
        // Check state for matching first
        if (!a.getBlock().equals(state.getBlock())) {
            return false;
        }
        // Check to see if specified properties match now
        for (final IProperty<? extends Comparable<?>> property : this.properties) {
            if (!a.getValue(property).equals(state.getValue(property))) {
                return false;
            }
        }
        return true;
    }
}
