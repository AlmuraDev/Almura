/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.state;

import com.almuradev.almura.content.type.block.component.aabb.CollisionBox;
import com.almuradev.almura.content.type.block.component.aabb.WireFrame;
import com.almuradev.almura.content.type.block.component.action.breaks.BlockBreak;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.block.type.crop.state.CropBlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.block.type.normal.state.NormalBlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.material.MapColor;
import com.almuradev.almura.content.type.material.Material;
import com.almuradev.shared.registry.catalog.CatalogDelegate;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

@SuppressWarnings("UnusedReturnValue")
public interface BlockStateDefinitionBuilder<B extends BlockStateDefinitionBuilder<B>> {

    static CropBlockStateDefinitionBuilder crop() {
        return new CropBlockStateDefinitionBuilder();
    }

    static NormalBlockStateDefinitionBuilder normal() {
        return new NormalBlockStateDefinitionBuilder();
    }

    String asset();

    String id();

    B id(final String asset, final String id);

    Material material();

    B material(final Material material);

    MapColor mapColor();

    B mapColor(final MapColor color);

    CollisionBox collisionAABB();

    B collisionAABB(final CollisionBox bb);

    WireFrame wireFrameAABB();

    B wireFrameAABB(final WireFrame bb);

    OptionalDouble slipperiness();

    B slipperiness(final double slipperiness);

    OptionalDouble hardness();

    B hardness(final float hardness);

    OptionalDouble lightEmission();

    B lightEmission(final float emission);

    OptionalInt lightOpacity();

    B lightOpacity(final int opacity);

    OptionalDouble resistance();

    B resistance(final float resistance);

    Optional<CatalogDelegate<BlockSoundGroup>> soundGroup();

    B soundGroup(final CatalogDelegate<BlockSoundGroup> group);

    List<BlockBreak> breaks();

    B breaks(final List<BlockBreak> breaks);

    B from(final BlockStateDefinitionBuilder<?> definition);

    BlockStateDefinition build();
}
