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
    public final @Nullable Boolean opaque;
    /** Whether water drips from the bottom of this block, like water above stone or dirt. */
    public final boolean drip;
    /** Whether the block is a light that flickers on the client (the server always sees its steady light). */
    public final boolean flicker;
    /** Whether the block, placed against the side of another block, is turned a quarter so it continues out from that block (e.g. straight pipes). */
    public final boolean rotatePlacement;
    public final OptionalDouble resistance;
    @Nullable public final Delegate<BlockSoundGroup> sound;
    @Nullable public final Delegate<BlockDestroyAction> destroyAction;
    public final OptionalInt flammability;
    public final OptionalInt fireSpreadSpeed;

    protected AbstractBlockStateDefinition(final AbstractBlockStateDefinitionBuilder<? extends BlockStateDefinition, ? extends AbstractBlockStateDefinitionBuilder> builder) {
        this.box = (B) builder.box;
        this.collisionBox = (C) builder.collisionBox;
        this.wireFrame = (W) builder.wireFrame;
        this.blockFaceShape = builder.blockFaceShape;
        this.hardness = builder.hardness;
        this.lightEmission = builder.lightEmission;
        this.lightOpacity = builder.lightOpacity;
        this.opaque = builder.opaque;
        this.drip = builder.drip != null && builder.drip;
        this.flicker = builder.flicker != null && builder.flicker;
        this.rotatePlacement = builder.rotatePlacement != null && builder.rotatePlacement;
        this.resistance = builder.resistance;
        this.sound = builder.sound;
        this.destroyAction = builder.destroyAction;
        this.flammability = builder.flammability;
        this.fireSpreadSpeed = builder.fireSpreadSpeed;
    }
}
