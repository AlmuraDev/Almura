/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.aabb;

import com.almuradev.almura.content.type.block.BlockConfig;
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
        final Type type = Type.fromNode(node.getNode(BlockConfig.State.AABB.TYPE));
        switch (type) {
            case CUSTOM:
                return Optional.of(new WireFrame(Boxes.from(node.getNode(BlockConfig.State.AABB.BOX))));
            case VANILLA:
                return Optional.of(VANILLA);
            case NONE:
                throw new IllegalArgumentException("'none' is not a supported type for block wireframes");
            default:
                throw new IllegalArgumentException(type.name());
        }
    }
}
