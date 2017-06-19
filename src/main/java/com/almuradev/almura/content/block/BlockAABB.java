/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.Constants;
import com.almuradev.almura.configuration.serializer.ConfigurationNodeDeserializer;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nullable;

public class BlockAABB {

    public static final Collision.Deserializer COLLISION = new Collision.Deserializer();
    public static final WireFrame.Deserializer WIRE_FRAME = new WireFrame.Deserializer();
    private static final EnumFacing[] HORIZONTAL_PLANE = EnumFacing.Plane.HORIZONTAL.facings();
    private static final int[] COS = {1, 0, -1, 0};
    private static final int[] SIN = {0, -1, 0, 1};
    @Nullable public final AxisAlignedBB bb;
    @Nullable private AxisAlignedBB[] horizontal;

    BlockAABB(@Nullable final AxisAlignedBB bb) {
        this.bb = bb;
    }

    public AxisAlignedBB[] horizontal() {
        if (this.horizontal != null) {
            return this.horizontal;
        }
        checkState(this.bb != null, "cannot create horizontal map for a null box");
        this.horizontal = new AxisAlignedBB[HORIZONTAL_PLANE.length];
        for (int i = 0, length = HORIZONTAL_PLANE.length; i < length; i++) {
            EnumFacing facing = HORIZONTAL_PLANE[i];
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

        bb = bb.offset(-0.5d, -0.5d, -0.5d);

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
            x0 + 0.5d, y0 + 0.5d, z0 + 0.5d,
            x1 + 0.5d, y1 + 0.5d, z1 + 0.5d
        );
    }

    public static class Collision extends BlockAABB {

        public static final Collision VANILLA = new Collision(Type.VANILLA, null);
        static final Optional<Collision> NONE = Optional.of(new Collision(Type.NONE, Block.NULL_AABB));
        public final boolean provided;

        Collision(final Type type, @Nullable final AxisAlignedBB bb) {
            super(bb);
            this.provided = type == Type.CUSTOM || type == Type.NONE;
        }

        public static final class Deserializer implements ConfigurationNodeDeserializer<Optional<Collision>> {

            private Deserializer() {
            }

            @Override
            public Optional<Collision> deserialize(final ConfigurationNode node) {
                if (node.isVirtual()) {
                    return Optional.empty();
                }
                final Type type = Type.fromString(node.getNode(Constants.Config.Block.AABB.TYPE).getString(Type.DEFAULT));
                switch (type) {
                    case CUSTOM:
                        final List<Double> box = node.getNode(Constants.Config.Block.AABB.BOX).getList(Types::asDouble, Collections.emptyList());
                        checkArgument(box.size() == 6, "box must have 6 components");
                        return Optional.of(new Collision(Type.CUSTOM, new AxisAlignedBB(
                                box.get(0), box.get(1), box.get(2),
                                box.get(3), box.get(4), box.get(5)
                        )));
                    case VANILLA:
                        return Optional.of(VANILLA);
                    case NONE:
                        return NONE;
                    default:
                        throw new IllegalArgumentException(type.name());
                }
            }
        }
    }

    public static class WireFrame extends BlockAABB {

        public static final WireFrame VANILLA = new WireFrame(null);

        WireFrame(@Nullable final AxisAlignedBB bb) {
            super(bb);
        }

        public static final class Deserializer implements ConfigurationNodeDeserializer<Optional<WireFrame>> {

            private Deserializer() {
            }

            @Override
            public Optional<WireFrame> deserialize(final ConfigurationNode node) {
                if (node.isVirtual()) {
                    return Optional.empty();
                }
                final Type type = Type.fromString(node.getNode(Constants.Config.Block.AABB.TYPE).getString(Type.DEFAULT));
                switch (type) {
                    case CUSTOM:
                        final List<Double> box = node.getNode(Constants.Config.Block.AABB.BOX).getList(Types::asDouble, Collections.emptyList());
                        checkArgument(box.size() == 6, "box must have 6 components");
                        return Optional.of(new WireFrame(new AxisAlignedBB(
                                box.get(0), box.get(1), box.get(2),
                                box.get(3), box.get(4), box.get(5)
                        )));
                    case VANILLA:
                        return Optional.of(VANILLA);
                    case NONE:
                        throw new IllegalArgumentException("'none' is not a supported type for block wireframes");
                    default:
                        throw new IllegalArgumentException(type.name());
                }
            }
        }
    }

    public enum Type {
        CUSTOM,
        NONE,
        VANILLA;

        public static final String DEFAULT = VANILLA.name().toLowerCase(Locale.ENGLISH);

        public static Type fromString(final String string) {
            return valueOf(string.toUpperCase(Locale.ENGLISH));
        }
    }
}
