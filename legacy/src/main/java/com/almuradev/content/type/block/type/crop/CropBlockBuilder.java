/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.AbstractParsedBlockBuilder;
import com.almuradev.content.type.block.BlockGenre;
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

public final class CropBlockBuilder extends AbstractParsedBlockBuilder<CropBlock, CropBlockStateDefinition, CropBlockStateDefinitionBuilder> implements CropBlock.Builder {
    private static final Pattern AGE = Pattern.compile("age=(\\d+)");
    /**
     * The {@link Block} constructor requires that {@link Block#createBlockState()} return a non-{@code null} value for
     * all properties, but this is not possible with {@code CropBlockImpl} as we do would not normally initialise the age
     * property until after the super constructor. We hack around this by maintaining a {@code static} property here that
     * is used during initialisation of a {@code CropBlockImpl}.
     */
    @Nullable static PropertyInteger property;
    // The same hack for how the crop being built grows in water: aquatic crops' states need the water level property (and surface ones a
    // wet flag) too
    static AquaticMode buildingAquatic = AquaticMode.NONE;
    // A surface aquatic crop keeps its wet flag in the meta's top bit, leaving three bits (ages 0-7) for its age
    private static final int MAXIMUM_SURFACE_AQUATIC_AGE = 7;
    // Ages 0-15: the age is the block's meta (the only property a crop has), which has room for 16
    private static final int MAXIMUM_AGE = 16;
    int age;
    Delegate<ItemType> seed;
    int sackHarvestResetAge = -1;
    AquaticMode aquatic = AquaticMode.NONE;

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
    public void sackHarvestResetAge(final int resetAge) {
        this.sackHarvestResetAge = resetAge;
    }

    @Override
    public void aquatic(final AquaticMode aquatic) {
        this.aquatic = aquatic;
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
        if (this.aquatic == AquaticMode.SURFACE && this.age > MAXIMUM_SURFACE_AQUATIC_AGE) {
            throw new IllegalStateException("A surface aquatic crop can't have more than " + (MAXIMUM_SURFACE_AQUATIC_AGE + 1) + " ages.");
        }
        try {
            property =  PropertyInteger.create("age", 0, this.age);
            buildingAquatic = this.aquatic;
            return new CropBlockImpl(this, states);
        } finally {
            property = null;
            buildingAquatic = AquaticMode.NONE;
        }
    }
}
