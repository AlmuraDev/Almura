/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;
import com.almuradev.content.type.block.component.aabb.BlockAABB;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroup;
import net.minecraft.block.state.BlockFaceShape;

import javax.annotation.Nullable;

public interface BlockStateDefinition {
    interface Builder<D extends BlockStateDefinition> {
        void parent(@Nullable final Builder<D> that);

        void box(@Nullable final BlockAABB.Box box);

        void collisionBox(@Nullable final BlockAABB.Collision collisionBox);

        void wireFrame(@Nullable final BlockAABB.WireFrame wireFrame);

        void blockFaceShape(final BlockFaceShape blockFaceShape);

        void hardness(final float hardness);

        void lightEmission(final float emission);

        void lightOpacity(final int opacity);

        void resistance(final float resistance);

        void sound(final Delegate<BlockSoundGroup> sound);

        void destroyAction(final Delegate<BlockDestroyAction> destroyAction);

        D build();
    }
}
