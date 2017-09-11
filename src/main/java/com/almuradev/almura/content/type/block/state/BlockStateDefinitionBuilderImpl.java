/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.state;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.content.type.block.component.aabb.CollisionBox;
import com.almuradev.almura.content.type.block.component.aabb.WireFrame;
import com.almuradev.almura.content.type.block.component.action.blockbreak.BlockBreak;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.material.MapColor;
import com.almuradev.almura.content.type.material.Material;
import com.almuradev.almura.registry.CatalogDelegate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.annotation.Nullable;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
final class BlockStateDefinitionBuilderImpl implements BlockStateDefinitionBuilder<BlockStateDefinitionBuilderImpl> {

    @Nullable private Material material;
    @Nullable private MapColor mapColor;
    @Nullable private String asset;
    @Nullable private String id;
    private CollisionBox collisionAABB = CollisionBox.VANILLA;
    private WireFrame wireFrameAABB = WireFrame.VANILLA;
    private OptionalDouble slipperiness = OptionalDouble.empty();
    private OptionalDouble hardness = OptionalDouble.empty();
    private OptionalDouble lightEmission = OptionalDouble.empty();
    private OptionalInt lightOpacity = OptionalInt.empty();
    private OptionalDouble resistance = OptionalDouble.empty();
    private Optional<CatalogDelegate<BlockSoundGroup>> soundGroup = Optional.empty();
    private List<BlockBreak> breaks = Collections.emptyList();

    @Override
    public String asset() {
        return checkNotNull(this.asset, "asset");
    }

    @Override
    public String id() {
        return checkNotNull(this.id, "id");
    }

    @Override
    public BlockStateDefinitionBuilderImpl id(final String asset, final String id) {
        this.asset = asset;
        this.id = id;
        return this;
    }

    @Override
    public Material material() {
        return checkNotNull(this.material, "material");
    }

    @Override
    public BlockStateDefinitionBuilderImpl material(final Material material) {
        this.material = material;
        return this;
    }

    @Override
    public MapColor mapColor() {
        return checkNotNull(this.mapColor, "map color");
    }

    @Override
    public BlockStateDefinitionBuilderImpl mapColor(final MapColor color) {
        this.mapColor = color;
        return this;
    }

    @Override
    public CollisionBox collisionAABB() {
        return this.collisionAABB;
    }

    @Override
    public BlockStateDefinitionBuilderImpl collisionAABB(final CollisionBox bb) {
        this.collisionAABB = bb;
        return this;
    }

    @Override
    public WireFrame wireFrameAABB() {
        return this.wireFrameAABB;
    }

    @Override
    public BlockStateDefinitionBuilderImpl wireFrameAABB(final WireFrame bb) {
        this.wireFrameAABB = bb;
        return this;
    }

    @Override
    public OptionalDouble slipperiness() {
        return this.slipperiness;
    }

    @Override
    public BlockStateDefinitionBuilderImpl slipperiness(final double slipperiness) {
        this.slipperiness = OptionalDouble.of(slipperiness);
        return this;
    }

    @Override
    public OptionalDouble hardness() {
        return this.hardness;
    }

    @Override
    public BlockStateDefinitionBuilderImpl hardness(final float hardness) {
        this.hardness = OptionalDouble.of(hardness);
        return this;
    }

    @Override
    public OptionalDouble lightEmission() {
        return this.lightEmission;
    }

    @Override
    public BlockStateDefinitionBuilderImpl lightEmission(final float emission) {
        this.lightEmission = OptionalDouble.of(emission);
        return this;
    }

    @Override
    public OptionalInt lightOpacity() {
        return this.lightOpacity;
    }

    @Override
    public BlockStateDefinitionBuilderImpl lightOpacity(final int opacity) {
        this.lightOpacity = OptionalInt.of(opacity);
        return this;
    }

    @Override
    public OptionalDouble resistance() {
        return this.resistance;
    }

    @Override
    public BlockStateDefinitionBuilderImpl resistance(final float resistance) {
        this.resistance = OptionalDouble.of(resistance);
        return this;
    }

    @Override
    public Optional<CatalogDelegate<BlockSoundGroup>> soundGroup() {
        return this.soundGroup;
    }

    @Override
    public BlockStateDefinitionBuilderImpl soundGroup(final CatalogDelegate<BlockSoundGroup> group) {
        this.soundGroup = Optional.of(group);
        return this;
    }

    @Override
    public List<BlockBreak> breaks() {
        return this.breaks;
    }

    @Override
    public BlockStateDefinitionBuilderImpl breaks(final List<BlockBreak> breaks) {
        this.breaks = breaks;
        return this;
    }

    @Override
    public BlockStateDefinition build() {
        checkState(this.material != null, "material not set");
        checkState(this.mapColor != null, "map color not set");
        return new BlockStateDefinition(this);
    }
}
