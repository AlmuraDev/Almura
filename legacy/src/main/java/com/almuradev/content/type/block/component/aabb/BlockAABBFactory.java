/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.component.aabb;

import net.minecraft.util.math.AxisAlignedBB;
import ninja.leaping.configurate.ConfigurationNode;

import javax.annotation.Nullable;

public interface BlockAABBFactory<B extends BlockAABB.Box, C extends BlockAABB.Collision, W extends BlockAABB.WireFrame> {
    default B box(final ConfigurationNode config) {
        return this.box(Boxes.from(config));
    }

    B box(final AxisAlignedBB box);

    default C collision(final ConfigurationNode config) {
        return this.collision(Boxes.from(config));
    }

    C collision(@Nullable final AxisAlignedBB box);

    default W wireFrame(final ConfigurationNode config) {
        return this.wireFrame(Boxes.from(config));
    }

    W wireFrame(final AxisAlignedBB box);
}
