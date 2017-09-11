/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.aabb;

import static com.google.common.base.Preconditions.checkArgument;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;

import java.util.Collections;
import java.util.List;

final class Boxes {

    static final EnumFacing[] HORIZONTAL_PLANE = EnumFacing.Plane.HORIZONTAL.facings();
    static final int HORIZONTAL_PLANES = HORIZONTAL_PLANE.length;
    private static final int[] COS = {1, 0, -1, 0};
    private static final int[] SIN = {0, -1, 0, 1};
    private static final double OFFSET = 0.5d;

    private Boxes() {
    }

    static AxisAlignedBB rotate(AxisAlignedBB bb, final EnumFacing direction) {
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

    static AxisAlignedBB from(final ConfigurationNode node) {
        final List<Double> box = node.getList(Types::asDouble, Collections.emptyList());
        checkArgument(box.size() == 6, "box must have 6 components");
        return new AxisAlignedBB(
                box.get(0), box.get(1), box.get(2),
                box.get(3), box.get(4), box.get(5)
        );
    }
}
