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
public abstract class AbstractBlockStateDefinitionBuilder<D extends BlockStateDefinition, B extends AbstractBlockStateDefinitionBuilder<D, B>> implements BlockStateDefinition.Builder<D> {
    @Nullable B parent;

    @Nullable protected BlockAABB.Box box;
    @Nullable protected BlockAABB.Collision collisionBox;
    @Nullable protected BlockAABB.WireFrame wireFrame;
    protected BlockFaceShape blockFaceShape;
    protected OptionalDouble hardness = OptionalDouble.empty();
    protected OptionalDouble lightEmission = OptionalDouble.empty();
    protected OptionalInt lightOpacity = OptionalInt.empty();
    protected @Nullable Boolean opaque;
    protected OptionalDouble resistance = OptionalDouble.empty();
    @Nullable protected Delegate<BlockSoundGroup> sound;
    @Nullable protected Delegate<BlockDestroyAction> destroyAction;
    public OptionalInt flammability = OptionalInt.empty();
    public OptionalInt fireSpreadSpeed = OptionalInt.empty();

    @Override
    public void parent(@Nullable final BlockStateDefinition.Builder<D> that) {
        this.parent = (B) that;
    }

    @Override
    public void box(@Nullable final BlockAABB.Box box) {
        BlockAABB.shares(this.box, -1);
        this.box = box;
    }

    @Override
    public void collisionBox(@Nullable final BlockAABB.Collision collisionBox) {
        BlockAABB.shares(this.collisionBox, -1);
        this.collisionBox = collisionBox;
    }

    @Override
    public void wireFrame(@Nullable final BlockAABB.WireFrame wireFrame) {
        BlockAABB.shares(this.wireFrame, -1);
        this.wireFrame = wireFrame;
    }

    @Override
    public void blockFaceShape(final BlockFaceShape blockFaceShape) {
        this.blockFaceShape = blockFaceShape;
    }

    @Override
    public void hardness(final float hardness) {
        this.hardness = OptionalDouble.of(hardness);
    }

    @Override
    public void lightEmission(final float emission) {
        if (emission > 1f) {
            this.lightEmission = OptionalDouble.of(emission / 15f);
        } else {
            this.lightEmission = OptionalDouble.of(emission);
        }
    }

    @Override
    public void lightOpacity(final int opacity) {
        this.lightOpacity = OptionalInt.of(opacity);
    }

    @Override
    public void opaque(final boolean opaque) {
        this.opaque = opaque;
    }

    @Override
    public void resistance(final float resistance) {
        this.resistance = OptionalDouble.of(resistance);
    }

    @Override
    public void sound(final Delegate<BlockSoundGroup> sound) {
        this.sound = sound;
    }

    @Override
    public void destroyAction(final Delegate<BlockDestroyAction> destroyAction) {
        this.destroyAction = destroyAction;
    }

    @Override
    public void flammability(final int flammability) {
        this.flammability = OptionalInt.of(flammability);
    }

    @Override
    public void fireSpreadSpeed(final int fireSpreadSpeed) {
        this.fireSpreadSpeed = OptionalInt.of(fireSpreadSpeed);
    }

    @Override
    public final D build() {
        this.inherit();
        return this.build0();
    }

    private void inherit() {
        this.inherit((B) this);
    }

    protected void inherit(final B that) {
        if (this.parent != null) {
            this.parent.inherit((B) this);
        }

        if (this != that) {
            if (that.box == null && this.box != null) that.box = BlockAABB.shares(this.box,1);
            if (that.collisionBox == null && this.collisionBox != null) that.collisionBox = BlockAABB.shares(this.collisionBox, 1);
            if (that.wireFrame == null && this.wireFrame != null) that.wireFrame = BlockAABB.shares(this.wireFrame, 1);
            if (!that.hardness.isPresent() && this.hardness.isPresent()) that.hardness = this.hardness;
            if (!that.lightEmission.isPresent() && this.lightEmission.isPresent()) that.lightEmission = this.lightEmission;
            if (!that.lightOpacity.isPresent() && this.lightOpacity.isPresent()) that.lightOpacity = this.lightOpacity;
            if (that.opaque == null && this.opaque != null) that.opaque = this.opaque;
            if (!that.resistance.isPresent() && this.resistance.isPresent()) that.resistance = this.resistance;
            if (that.sound == null && this.sound != null) that.sound = this.sound;
            if (that.destroyAction == null && this.destroyAction != null) that.destroyAction = this.destroyAction;
            if (!that.flammability.isPresent() && this.flammability.isPresent()) that.flammability = this.flammability;
            if (!that.fireSpreadSpeed.isPresent() && this.fireSpreadSpeed.isPresent())  that.fireSpreadSpeed = this.fireSpreadSpeed;
            that.blockFaceShape = this.blockFaceShape;
        }
    }

    protected abstract D build0();
}
