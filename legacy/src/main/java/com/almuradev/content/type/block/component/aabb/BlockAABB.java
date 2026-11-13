/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.component.aabb;

import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

public interface BlockAABB {
    AxisAlignedBB box();

    BlockAABB copy();

    void shares(final int n);

    @Nullable
    static <B extends BlockAABB> B shares(@Nullable final B aabb, final int n) {
        if (aabb != null) {
            aabb.shares(n);
        }
        return aabb;
    }

    abstract class Impl implements BlockAABB {
        @Nullable protected final AxisAlignedBB bb;
        protected int shares;

        protected Impl(@Nullable final AxisAlignedBB bb) {
            this.bb = bb;
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public AxisAlignedBB box() {
            return this.bb;
        }

        @Override
        public void shares(final int n) {
            this.shares += n;
        }
    }

    interface Box extends BlockAABB {
        @Override
        Box copy();
    }

    interface Collision extends BlockAABB {
        @Nullable
        @Override
        @SuppressWarnings("NullableProblems")
        AxisAlignedBB box();

        @Override
        Collision copy();
    }

    interface WireFrame extends BlockAABB {
        @Override
        WireFrame copy();
    }
}
