/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block;

import com.almuradev.almura.content.block.component.aabb.CollisionBox;
import com.almuradev.almura.content.block.component.aabb.WireFrame;
import com.almuradev.almura.content.block.component.action.blockbreak.BlockBreak;
import com.almuradev.almura.content.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.loader.CatalogDelegate;
import com.almuradev.almura.content.material.MapColor;
import com.almuradev.almura.content.material.Material;
import com.almuradev.almura.content.material.MaterialType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;

import java.util.List;
import java.util.Optional;
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

        CollisionBox collisionAABB();

        BUILDER collisionAABB(final CollisionBox bb);

        WireFrame wireFrameAABB();

        BUILDER wireFrameAABB(final WireFrame bb);

        OptionalDouble slipperiness();

        BUILDER slipperiness(double slipperiness);

        OptionalDouble hardness();

        BUILDER hardness(float hardness);

        OptionalDouble lightEmission();

        BUILDER lightEmission(float lightEmission);

        OptionalInt lightOpacity();

        BUILDER lightOpacity(int lightOpacity);

        OptionalDouble resistance();

        BUILDER resistance(float resistance);

        Optional<CatalogDelegate<BlockSoundGroup>> soundGroup();

        BUILDER soundGroup(CatalogDelegate<BlockSoundGroup> group);

        List<BlockBreak> breaks();

        BUILDER breaks(final List<BlockBreak> breaks);

        @Override
        BLOCK build(String id);
    }
}
