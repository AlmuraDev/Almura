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
import javax.inject.Singleton;

@Singleton
public final class NormalBoxFactory implements BlockAABBFactory<BlockAABB.Box, BlockAABB.Collision, BlockAABB.WireFrame> {
    private static final NormalBox NULL_COLLISION = new NormalBox(null);

    @Override
    public BlockAABB.Box box(final AxisAlignedBB box) {
        return new NormalBox(box);
    }

    @Override
    public BlockAABB.Collision collision(@Nullable final AxisAlignedBB box) {
        if (box == null) {
            return NULL_COLLISION;
        }
        return new NormalBox(box);
    }

    @Override
    public BlockAABB.WireFrame wireFrame(final AxisAlignedBB box) {
        return new NormalBox(box);
    }
}
