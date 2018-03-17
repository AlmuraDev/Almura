/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.log;

import com.almuradev.content.type.block.type.log.state.LogBlockStateDefinition;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public final class LogBlockImpl extends BlockLog implements LogBlock {
    @Deprecated private static final int LEGACY_X_AXIS = 1;
    @Deprecated private static final int LEGACY_Y_AXIS = 0;
    @Deprecated private static final int LEGACY_Z_AXIS = 2;
    @Deprecated private static final int LEGACY_NONE_AXIS = 4;
    private final LogBlockStateDefinition definition;

    LogBlockImpl(final LogBlockBuilder builder) {
        this.displayOnCreativeTab = null;
        builder.fill(this);
        this.definition = builder.singleState();
        this.definition.fill(this);
        this.setDefaultState(this.blockState.getBaseState().withProperty(LOG_AXIS, EnumAxis.Y));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOG_AXIS);
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(final int data) {
        IBlockState state = this.getDefaultState();
        switch (data) {
            case LEGACY_Y_AXIS:
                state = state.withProperty(LOG_AXIS, EnumAxis.Y);
                break;
            case LEGACY_X_AXIS:
                state = state.withProperty(LOG_AXIS, EnumAxis.X);
                break;
            case LEGACY_Z_AXIS:
                state = state.withProperty(LOG_AXIS, EnumAxis.Z);
                break;
            case LEGACY_NONE_AXIS:
            default:
                state = state.withProperty(LOG_AXIS, EnumAxis.NONE);
                break;
        }
        return state;
    }

    @Deprecated
    @Override
    public int getMetaFromState(final IBlockState state) {
        int data = LEGACY_Y_AXIS;
        switch (state.getValue(LOG_AXIS)) {
            case X:
                data |= LEGACY_X_AXIS;
                break;
            case Z:
                data |= LEGACY_Z_AXIS;
                break;
            case NONE:
                data |= LEGACY_NONE_AXIS;
                break;
        }
        return data;
    }

    @Override
    public LogBlockStateDefinition definition(final IBlockState state) {
        return this.definition;
    }
}
