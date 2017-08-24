/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.component.aabb;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Locale;

import javax.annotation.Nullable;

public class BlockAABB {

    @Nullable public final AxisAlignedBB bb;
    @Nullable private AxisAlignedBB[] horizontal;

    BlockAABB(@Nullable final AxisAlignedBB bb) {
        this.bb = bb;
    }

    public AxisAlignedBB horizontal(final EnumFacing facing) {
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

    public enum Type {
        CUSTOM,
        NONE,
        VANILLA;

        private static final String DEFAULT = VANILLA.name().toLowerCase(Locale.ENGLISH);

        static Type fromNode(final ConfigurationNode node) {
            return valueOf(node.getString(DEFAULT).toUpperCase(Locale.ENGLISH));
        }
    }
}
