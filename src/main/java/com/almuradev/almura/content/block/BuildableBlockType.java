/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block;

import com.almuradev.almura.content.material.MapColor;
import com.almuradev.almura.content.material.Material;
import com.almuradev.almura.content.material.MaterialType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public interface BuildableBlockType extends MaterialType, BlockType {

    @SuppressWarnings("unchecked")
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder<BLOCK extends BuildableBlockType, BUILDER extends Builder<BLOCK, BUILDER>> extends MaterialType.Builder<BLOCK, BUILDER> {

        Material material();

        BUILDER material(Material material);

        MapColor mapColor();

        BUILDER mapColor(MapColor mapColor);

        BlockAABB.Collision collisionAABB();

        BUILDER collisionAABB(final BlockAABB.Collision bb);

        BlockAABB.WireFrame wireFrameAABB();

        BUILDER wireFrameAABB(final BlockAABB.WireFrame bb);

        OptionalDouble hardness();

        BUILDER hardness(float hardness);

        OptionalDouble lightEmission();

        BUILDER lightEmission(float lightEmission);

        OptionalInt lightOpacity();

        BUILDER lightOpacity(int lightOpacity);

        OptionalDouble resistance();

        BUILDER resistance(float resistance);

        @Override
        default BLOCK build(String id, String name) {
            return build(id);
        }

        @Override
        BLOCK build(String id);
    }
}
