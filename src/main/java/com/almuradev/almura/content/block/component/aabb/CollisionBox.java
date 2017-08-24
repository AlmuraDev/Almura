/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.component.aabb;

import static com.google.common.base.Preconditions.checkArgument;

import com.almuradev.almura.Constants;
import net.minecraft.block.Block;
import net.minecraft.util.math.AxisAlignedBB;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public final class CollisionBox extends BlockAABB {

    /**
     * A constant value used to indicate that the vanilla collision box value should be used.
     */
    public static final CollisionBox VANILLA = new CollisionBox(Type.VANILLA, null);
    /**
     * A constant value used to indicate that no collision box should be used.
     */
    static final Optional<CollisionBox> NONE = Optional.of(new CollisionBox(Type.NONE, Block.NULL_AABB));
    /**
     * A collision box is only provided when it is a {@link Type#CUSTOM} or {@link Type#NONE} type.
     */
    public final boolean provided;

    private CollisionBox(final Type type, @Nullable final AxisAlignedBB bb) {
        super(bb);
        this.provided = type == Type.CUSTOM || type == Type.NONE;
    }

    public static Optional<CollisionBox> deserialize(final ConfigurationNode node) {
        if (node.isVirtual()) {
            return Optional.empty();
        }
        final Type type = Type.fromNode(node.getNode(Constants.Config.Block.AABB.TYPE));
        switch (type) {
            case CUSTOM:
                final List<Double> box = node.getNode(Constants.Config.Block.AABB.BOX).getList(Types::asDouble, Collections.emptyList());
                checkArgument(box.size() == 6, "box must have 6 components");
                return Optional.of(new CollisionBox(Type.CUSTOM, new AxisAlignedBB(
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
