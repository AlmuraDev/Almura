/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.state;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.state.value.StateValue;
import com.google.common.base.Suppliers;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

final class StatefulLazyBlockState extends AbstractLazyBlockState {
    private final Supplier<Map<IProperty<? extends Comparable<?>>, StateValue<? extends Comparable<?>>>> properties;

    StatefulLazyBlockState(final Delegate<BlockType> block, final Map<String, StateValue<? extends Comparable<?>>> properties) {
        super(block);
        this.properties = Suppliers.memoize(() -> this.resolveProperties(properties));
    }

    private <T extends Comparable<T>> Map<IProperty<? extends Comparable<?>>, StateValue<? extends Comparable<?>>> resolveProperties(final Map<String, StateValue<? extends Comparable<?>>> source) {
        final Block block = (Block) this.block.require();
        final Map<IProperty<? extends Comparable<?>>, StateValue<? extends Comparable<?>>> target = new HashMap<>();
        final BlockStateContainer definition = block.getBlockState();
        for (final Map.Entry<String, StateValue<? extends Comparable<?>>> entry : source.entrySet()) {
            @Nullable final IProperty<T> property = (IProperty<T>) definition.getProperty(entry.getKey());
            if (property != null) {
                target.put(property, entry.getValue());
            }
        }
        return target;
    }

    @Override
    <T extends Comparable<T>> IBlockState createState() {
        final Block block = (Block) this.block.require();
        IBlockState state = block.getDefaultState();
        for (final Map.Entry<IProperty<? extends Comparable<?>>, StateValue<? extends Comparable<?>>> entry : this.properties.get().entrySet()) {
            @Nullable final IProperty<T> property = (IProperty<T>) entry.getKey();
            if (property != null) {
                @Nullable final T value = ((StateValue<T>) entry.getValue()).get(property);
                if (value != null) {
                    state = state.withProperty(property, value);
                }
            }
        }
        return state;
    }

    @Override
    public <V extends Comparable<V>> Optional<StateValue<V>> value(final IProperty<V> property) {
        return Optional.ofNullable((StateValue<V>) this.properties.get().get(property));
    }

    @Override
    protected <V extends Comparable<V>> boolean partialTest0(final IBlockState state) {
        // Check to see if specified properties match now
        for (final Map.Entry<IProperty<? extends Comparable<?>>, StateValue<? extends Comparable<?>>> entry : this.properties.get().entrySet()) {
            final IProperty<V> property = (IProperty<V>) entry.getKey();
            final StateValue<V> value = (StateValue<V>) entry.getValue();

            if (!value.test(property, state)) {
                return false;
            }
        }
        return true;
    }
}
