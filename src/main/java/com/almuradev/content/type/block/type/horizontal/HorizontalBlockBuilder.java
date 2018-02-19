/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal;

import com.almuradev.content.type.block.AbstractParsedBlockBuilder;
import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.type.horizontal.state.HorizontalBlockStateDefinition;
import com.almuradev.content.type.block.type.horizontal.state.HorizontalBlockStateDefinitionBuilder;
import com.almuradev.content.type.block.type.horizontal.state.HorizontalBlockStateDefinitionBuilderImpl;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.EnumFacing;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class HorizontalBlockBuilder extends AbstractParsedBlockBuilder<HorizontalBlock, HorizontalBlockStateDefinition, HorizontalBlockStateDefinitionBuilder> implements HorizontalBlock.Builder {
    private static final Pattern FACING = Pattern.compile("facing=([a-z]*)");
    private final IntSet facings = new IntArraySet();

    public HorizontalBlockBuilder() {
        super(BlockGenre.HORIZONTAL);
    }

    @Override
    protected Pattern pattern() {
        return FACING;
    }

    @Override
    protected String describePattern() {
        return "facing=value";
    }

    @Override
    protected HorizontalBlockStateDefinitionBuilder createDefinitionBuilder(final String id, final Matcher matcher) {
        final EnumFacing facing = EnumFacing.valueOf(matcher.group(1).toUpperCase(Locale.ENGLISH));
        if (!this.facings.add(facing.ordinal())) {
            throw new IllegalStateException("Facing " + facing + " already defined.");
        }
        return new HorizontalBlockStateDefinitionBuilderImpl(facing);
    }

    @Override
    public HorizontalBlock build() {
        final List<HorizontalBlockStateDefinition> states = this.stateBuilders().values().stream()
                .map(HorizontalBlockStateDefinitionBuilder::build)
                .collect(Collectors.toList());
        return new HorizontalBlockImpl(this, states);
    }
}
