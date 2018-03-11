/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinition;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinitionBuilder;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinitionBuilderImpl;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import org.spongepowered.api.item.ItemType;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class CropBlockBuilder extends ContentBlockType.Builder.Impl.Parsed<CropBlock, CropBlockStateDefinition, CropBlockStateDefinitionBuilder> implements CropBlock.Builder {

    private static final Pattern AGE = Pattern.compile("age=(\\d)");
    /**
     * The {@link Block} constructor requires that {@link Block#createBlockState()} return a non-{@code null} value for
     * all properties, but this is not possible with {@code CropBlockImpl} as we do would not normally initialise the age
     * property until after the super constructor. We hack around this by maintaining a {@code static} property here that
     * is used during initialisation of a {@code CropBlockImpl}.
     */
    @Nullable static PropertyInteger property;
    private static final int MAXIMUM_AGE = 7;
    int age;
    Delegate<ItemType> seed;

    public CropBlockBuilder() {
        super(BlockGenre.CROP);
    }

    @Override
    protected Pattern pattern() {
        return AGE;
    }

    @Override
    protected String describePattern() {
        return "age=value";
    }

    @Override
    public void seed(final Delegate<ItemType> seed) {
        this.seed = seed;
    }

    @Override
    protected CropBlockStateDefinitionBuilder createDefinitionBuilder(final String id, final Matcher matcher) {
        final int age = Integer.parseInt(matcher.group(1));
        this.age = Math.max(this.age, age);
        if (this.age >= MAXIMUM_AGE) {
            throw new IllegalStateException("Cannot have more than " + MAXIMUM_AGE + " ages.");
        }
        return new CropBlockStateDefinitionBuilderImpl(age);
    }

    @Override
    public CropBlock build() {
        final List<CropBlockStateDefinition> states = this.stateBuilders().values().stream()
                .map(CropBlockStateDefinitionBuilder::build)
                .collect(Collectors.toList());
        try {
            property =  PropertyInteger.create("age", 0, this.age);
            return new CropBlockImpl(this, states);
        } finally {
            property = null;
        }
    }
}
