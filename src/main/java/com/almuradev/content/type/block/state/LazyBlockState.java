/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.state;

import com.almuradev.content.type.block.state.value.StateValue;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public interface LazyBlockState extends Supplier<IBlockState> {
    static LazyBlockState parse(final ConfigurationNode config) {
        return LazyBlockStateParser.parse(config);
    }

    Block block();

    @SuppressWarnings("RedundantCast")
    default Collection<IProperty<? extends Comparable<?>>> properties() {
        return (Collection<IProperty<? extends Comparable<?>>>) this.block().getBlockState().getProperties();
    }

    <V extends Comparable<V>> Optional<StateValue<V>> value(final IProperty<V> property);

    boolean fullTest(final IBlockState state);

    @SuppressWarnings("unused") // IntelliJ is not intelligent
    <V extends Comparable<V>> boolean partialTest(final IBlockState state);
}
