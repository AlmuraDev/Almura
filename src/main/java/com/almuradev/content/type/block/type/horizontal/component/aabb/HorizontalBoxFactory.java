/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal.component.aabb;

import com.almuradev.content.type.block.component.aabb.BlockAABBFactory;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import javax.inject.Singleton;

@Singleton
public final class HorizontalBoxFactory implements BlockAABBFactory<HorizontalBox, HorizontalBox.Collision, HorizontalBox> {

    private static final HorizontalBox.Collision NULL_COLLISION = new HorizontalBox.Collision(null);

    @Override
    public HorizontalBox box(final AxisAlignedBB box) {
        return new HorizontalBox(box);
    }

    @Override
    public HorizontalBox.Collision collision(@Nullable final AxisAlignedBB box) {
        if (box == null) {
            return NULL_COLLISION;
        }
        return new HorizontalBox.Collision(box);
    }

    @Override
    public HorizontalBox wireFrame(final AxisAlignedBB box) {
        return new HorizontalBox(box);
    }
}
