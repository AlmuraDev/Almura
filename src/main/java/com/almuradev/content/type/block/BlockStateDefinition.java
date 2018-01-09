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
import net.minecraft.block.Block;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.annotation.Nullable;

public interface BlockStateDefinition {

    interface Builder<D extends BlockStateDefinition> {

        void parent(@Nullable final Builder<D> that);

        void box(@Nullable final BlockAABB.Box box);

        void collisionBox(@Nullable final BlockAABB.Collision collisionBox);

        void wireFrame(@Nullable final BlockAABB.WireFrame wireFrame);

        void hardness(final float hardness);

        void lightEmission(final float emission);

        void lightOpacity(final int opacity);

        void resistance(final float resistance);

        void sound(final Delegate<BlockSoundGroup> sound);

        void destroyAction(final Delegate<BlockDestroyAction> destroyAction);

        D build();

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        abstract class Impl<D extends BlockStateDefinition, B extends Impl<D, B>> implements Builder<D> {

            @Nullable B parent;

            @Nullable protected BlockAABB.Box box;
            @Nullable protected BlockAABB.Collision collisionBox;
            @Nullable protected BlockAABB.WireFrame wireFrame;
            protected OptionalDouble hardness = OptionalDouble.empty();
            protected OptionalDouble lightEmission = OptionalDouble.empty();
            protected OptionalInt lightOpacity = OptionalInt.empty();
            protected OptionalDouble resistance = OptionalDouble.empty();
            @Nullable protected Delegate<BlockSoundGroup> sound;
            @Nullable protected Delegate<BlockDestroyAction> destroyAction;

            @Override
            public void parent(@Nullable final Builder<D> that) {
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
                    if (this.box != null) that.box = BlockAABB.shares(this.box, 1);
                    if (this.collisionBox != null) that.collisionBox = BlockAABB.shares(this.collisionBox, 1);
                    if (this.wireFrame != null) that.wireFrame = BlockAABB.shares(this.wireFrame, 1);
                    if (this.hardness.isPresent()) that.hardness = this.hardness;
                    if (this.lightEmission.isPresent()) that.lightEmission = this.lightEmission;
                    if (this.lightOpacity.isPresent())  that.lightOpacity = this.lightOpacity;
                    if (this.resistance.isPresent()) that.resistance = this.resistance;
                    if (this.sound != null) that.sound = this.sound;
                    if (this.destroyAction != null) that.destroyAction = this.destroyAction;
                }
            }

            protected abstract D build0();
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    abstract class Impl<B extends BlockAABB.Box, C extends BlockAABB.Collision, W extends BlockAABB.WireFrame> implements BlockStateDefinition {

        @Nullable public final B box;
        @Nullable public final C collisionBox;
        @Nullable public final W wireFrame;
        public final OptionalDouble hardness;
        public final OptionalDouble lightEmission;
        public final OptionalInt lightOpacity;
        public final OptionalDouble resistance;
        public final Delegate<BlockSoundGroup> sound;
        @Nullable public final Delegate<BlockDestroyAction> destroyAction;

        protected Impl(final Builder.Impl<? extends BlockStateDefinition, ? extends Builder.Impl> builder) {
            this.box = (B) builder.box;
            this.collisionBox = (C) builder.collisionBox;
            this.wireFrame = (W) builder.wireFrame;
            this.hardness = builder.hardness;
            this.lightEmission = builder.lightEmission;
            this.lightOpacity = builder.lightOpacity;
            this.resistance = builder.resistance;
            this.sound = builder.sound;
            this.destroyAction = builder.destroyAction;
        }

        public static class Single<B extends BlockAABB.Box, C extends BlockAABB.Collision, W extends BlockAABB.WireFrame> extends Impl<B, C, W> {

            protected Single(final Builder.Impl<? extends BlockStateDefinition, ? extends Builder.Impl> builder) {
                super(builder);
            }

            public void fill(final Block block) {
                this.hardness.ifPresent(hardness -> block.setHardness((float) hardness));
                this.lightEmission.ifPresent(emission -> block.setLightLevel((float) emission));
                this.lightOpacity.ifPresent(block::setLightOpacity);
                this.resistance.ifPresent(resistance -> block.setResistance((float) resistance));
            }
        }
    }
}
