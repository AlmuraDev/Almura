/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal.component.aabb;

import static com.google.common.base.Preconditions.checkState;

import com.almuradev.content.type.block.component.aabb.BlockAABB;
import com.almuradev.content.type.block.component.aabb.Boxes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

public class HorizontalBox extends BlockAABB.Impl implements BlockAABB.Box, BlockAABB.Collision, BlockAABB.WireFrame {
    @Nullable private AxisAlignedBB[] horizontal;

    HorizontalBox(@Nullable final AxisAlignedBB bb) {
        super(bb);
    }

    public AxisAlignedBB facing(final EnumFacing facing) {
        if (this.shares == 0) {
            return this.bb;
        }
        return this.horizontal()[facing.getHorizontalIndex()];
    }

    private AxisAlignedBB[] horizontal() {
        if (this.horizontal != null) {
            return this.horizontal;
        }
        checkState(this.bb != null, "cannot create horizontal map for a null box");
        this.horizontal = new AxisAlignedBB[Boxes.HORIZONTAL_PLANES];
        for (int i = 0, length = Boxes.HORIZONTAL_PLANES; i < length; i++) {
            final EnumFacing facing = Boxes.HORIZONTAL_PLANE[i];
            this.horizontal[facing.getHorizontalIndex()] = Boxes.rotate(this.bb, facing);
        }
        return this.horizontal;
    }

    @Override
    public HorizontalBox copy() {
        return new HorizontalBox(this.bb);
    }

    public static class Collision extends HorizontalBox implements BlockAABB.Collision {
        Collision(@Nullable final AxisAlignedBB bb) {
            super(bb);
        }

        @Nullable
        @Override
        public AxisAlignedBB facing(final EnumFacing facing) {
            if (this.bb == null) {
                return null;
            }
            return super.facing(facing);
        }

        @Override
        public HorizontalBox.Collision copy() {
            return new HorizontalBox.Collision(this.bb);
        }
    }
}
