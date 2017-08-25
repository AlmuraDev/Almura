/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.component.aabb;

import com.almuradev.almura.Constants;
import net.minecraft.util.math.AxisAlignedBB;
import ninja.leaping.configurate.ConfigurationNode;

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
                return Optional.of(new WireFrame(Boxes.from(node.getNode(Constants.Config.Block.AABB.BOX))));
            case VANILLA:
                return Optional.of(VANILLA);
            case NONE:
                throw new IllegalArgumentException("'none' is not a supported type for block wireframes");
            default:
                throw new IllegalArgumentException(type.name());
        }
    }
}
