/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.component.aabb;

import static com.google.common.base.Preconditions.checkArgument;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;

import java.util.Collections;
import java.util.List;

public final class Boxes {
    public static final EnumFacing[] HORIZONTAL_PLANE = EnumFacing.Plane.HORIZONTAL.facings();
    public static final int HORIZONTAL_PLANES = HORIZONTAL_PLANE.length;
    private static final int[] COS = {1, 0, -1, 0};
    private static final int[] SIN = {0, -1, 0, 1};
    private static final double OFFSET = 0.5d;

    private Boxes() {
    }

    public static AxisAlignedBB rotate(AxisAlignedBB bb, final EnumFacing direction) {
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

    static AxisAlignedBB from(final ConfigurationNode config) {
        final List<Double> min = config.getNode(AABBConfig.Box.MIN).getList(Types::asDouble, Collections.emptyList());
        final List<Double> max = config.getNode(AABBConfig.Box.MAX).getList(Types::asDouble, Collections.emptyList());
        checkArgument(min.size() == 3, "min must have 3 components");
        checkArgument(max.size() == 3, "max must have 3 components");

        //Todo: incorrect assumptions made on this.  The collision box for the player CAN be greater than 1 and less than 0
        //Todo: in certain scenarios; its the ability to break the block is only 0/1.

        //Todo: Collisions work, wireframe works; but the client/servers ability to rayTrace beyond 0/1 is not possible for a single x/y/z location.
        /*double x0 = min.get(0) > 1 ? 1 : min.get(0) < 0 ? 0 : min.get(0);
        double y0 = min.get(1) > 1 ? 1 : min.get(1) < 0 ? 0 : min.get(1);
        double z0 = min.get(2) > 1 ? 1 : min.get(2) < 0 ? 0 : min.get(2);
        double x1 = max.get(0) > 1 ? 1 : max.get(0) < 0 ? 0 : max.get(0);
        double y1 = max.get(1) > 1 ? 1 : max.get(1) < 0 ? 0 : max.get(1);
        double z1 = max.get(2) > 1 ? 1 : max.get(2) < 0 ? 0 : max.get(2);

        return new AxisAlignedBB(x0, y0, z0, x1, y1, z1); */
        return new AxisAlignedBB(
            min.get(0), min.get(1), min.get(2),
            max.get(0), max.get(1), max.get(2)
        );
    }
}
