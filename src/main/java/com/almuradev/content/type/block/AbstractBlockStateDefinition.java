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

import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.annotation.Nullable;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class AbstractBlockStateDefinition<B extends BlockAABB.Box, C extends BlockAABB.Collision, W extends BlockAABB.WireFrame> implements BlockStateDefinition {
    @Nullable public final B box;
    @Nullable public final C collisionBox;
    @Nullable public final W wireFrame;
    public final BlockFaceShape blockFaceShape;
    public final OptionalDouble hardness;
    public final OptionalDouble lightEmission;
    public final OptionalInt lightOpacity;
    public final OptionalDouble resistance;
    @Nullable public final Delegate<BlockSoundGroup> sound;
    @Nullable public final Delegate<BlockDestroyAction> destroyAction;

    protected AbstractBlockStateDefinition(final AbstractBlockStateDefinitionBuilder<? extends BlockStateDefinition, ? extends AbstractBlockStateDefinitionBuilder> builder) {
        this.box = (B) builder.box;
        this.collisionBox = (C) builder.collisionBox;
        this.wireFrame = (W) builder.wireFrame;
        this.blockFaceShape = builder.blockFaceShape;
        this.hardness = builder.hardness;
        this.lightEmission = builder.lightEmission;
        this.lightOpacity = builder.lightOpacity;
        this.resistance = builder.resistance;
        this.sound = builder.sound;
        this.destroyAction = builder.destroyAction;
    }
}
