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

    private static final EnumFacing[] HORIZONTAL_PLANE = EnumFacing.Plane.HORIZONTAL.facings();
    private static final int[] COS = {1, 0, -1, 0};
    private static final int[] SIN = {0, -1, 0, 1};
    private static final double OFFSET = 0.5d;
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
        this.horizontal = new AxisAlignedBB[HORIZONTAL_PLANE.length];
        for (int i = 0, length = HORIZONTAL_PLANE.length; i < length; i++) {
            final EnumFacing facing = HORIZONTAL_PLANE[i];
            this.horizontal[facing.getHorizontalIndex()] = rotate(this.bb, facing);
        }
        return this.horizontal;
    }

    private static AxisAlignedBB rotate(AxisAlignedBB bb, final EnumFacing direction) {
        final int angle = direction.getAxis() == EnumFacing.Axis.Y ? direction.getAxisDirection().getOffset() : direction.getHorizontalIndex();
        if (angle == 0) {
            return bb;
        }

        final EnumFacing.Axis axis = direction.getAxis() == EnumFacing.Axis.Y ? EnumFacing.Axis.X : EnumFacing.Axis.Y;

        final int index = -angle & 0x3;
        final int sin = SIN[index];
        final int cos = COS[index];

        bb = bb.offset(-OFFSET, -OFFSET, -OFFSET);

        double x0 = bb.minX;
        double y0 = bb.minY;
        double z0 = bb.minZ;
        double x1 = bb.maxX;
        double y1 = bb.maxY;
        double z1 = bb.maxZ;

        if (axis == EnumFacing.Axis.X) {
            y0 = (bb.minY * cos) - (bb.minZ * sin);
            y1 = (bb.maxY * cos) - (bb.maxZ * sin);
            z0 = (bb.minY * sin) + (bb.minZ * cos);
            z1 = (bb.maxY * sin) + (bb.maxZ * cos);
        }

        if (axis == EnumFacing.Axis.Y) {
            x0 = (bb.minX * cos) - (bb.minZ * sin);
            x1 = (bb.maxX * cos) - (bb.maxZ * sin);
            z0 = (bb.minX * sin) + (bb.minZ * cos);
            z1 = (bb.maxX * sin) + (bb.maxZ * cos);
        }

        return new AxisAlignedBB(
            x0 + OFFSET, y0 + OFFSET, z0 + OFFSET,
            x1 + OFFSET, y1 + OFFSET, z1 + OFFSET
        );
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
