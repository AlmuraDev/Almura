/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.component.aabb;

import static com.google.common.base.Preconditions.checkArgument;

import com.almuradev.almura.Constants;
import net.minecraft.util.math.AxisAlignedBB;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public final class WireFrame extends BlockAABB {

    /**
     * A constant value used to indicate that the vanilla wireframe value should be used.
     */
    public static final WireFrame VANILLA = new WireFrame(null);

    private WireFrame(@Nullable final AxisAlignedBB bb) {
        super(bb);
    }

    public static Optional<WireFrame> deserialize(final ConfigurationNode node) {
        if (node.isVirtual()) {
            return Optional.empty();
        }
        final Type type = Type.fromNode(node.getNode(Constants.Config.Block.AABB.TYPE));
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
