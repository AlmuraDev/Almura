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

final class NormalBox extends BlockAABB.Impl implements BlockAABB.Box, BlockAABB.Collision, BlockAABB.WireFrame {

    NormalBox(@Nullable final AxisAlignedBB bb) {
        super(bb);
    }

    @Override
    public NormalBox copy() {
        return new NormalBox(this.bb);
    }
}
