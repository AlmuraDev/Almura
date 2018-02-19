/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal.state;

import com.almuradev.content.type.block.AbstractBlockStateDefinition;
import com.almuradev.content.type.block.type.horizontal.component.aabb.HorizontalBox;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public final class HorizontalBlockStateDefinition extends AbstractBlockStateDefinition<HorizontalBox, HorizontalBox.Collision, HorizontalBox> {
    public final EnumFacing facing;
    public final boolean nullCollisionBox;

    HorizontalBlockStateDefinition(final HorizontalBlockStateDefinitionBuilderImpl builder) {
        super(builder);
        this.facing = builder.facing;
        this.nullCollisionBox = this.collisionBox != null && this.collisionBox.box() == null;
    }

    @Nullable
    public AxisAlignedBB box(final EnumFacing facing) {
        return this.box != null ? this.box.facing(facing) : null;
    }

    @Nullable
    public AxisAlignedBB collisionBox(final EnumFacing facing) {
        return this.collisionBox != null ? this.collisionBox.facing(facing) : null;
    }

    @Nullable
    public AxisAlignedBB wireFrame(final EnumFacing facing, final BlockPos pos) {
        return this.wireFrame != null ? this.wireFrame.facing(facing).offset(pos) : null;
    }
}
