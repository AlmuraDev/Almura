/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.state;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.content.type.block.component.aabb.CollisionBox;
import com.almuradev.almura.content.type.block.component.aabb.WireFrame;
import com.almuradev.almura.content.type.block.component.action.breaks.BlockBreak;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.material.MapColor;
import com.almuradev.almura.content.type.material.Material;
import com.almuradev.shared.registry.catalog.CatalogDelegate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.annotation.Nullable;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
public abstract class AbstractBlockStateDefinitionBuilder<B extends AbstractBlockStateDefinitionBuilder<B>> implements BlockStateDefinitionBuilder<B> {

    @Nullable protected Material material;
    @Nullable protected MapColor mapColor;
    @Nullable protected String asset;
    @Nullable protected String id;
    protected CollisionBox collisionAABB = CollisionBox.VANILLA;
    protected WireFrame wireFrameAABB = WireFrame.VANILLA;
    protected OptionalDouble slipperiness = OptionalDouble.empty();
    protected OptionalDouble hardness = OptionalDouble.empty();
    protected OptionalDouble lightEmission = OptionalDouble.empty();
    protected OptionalInt lightOpacity = OptionalInt.empty();
    protected OptionalDouble resistance = OptionalDouble.empty();
    protected Optional<CatalogDelegate<BlockSoundGroup>> soundGroup = Optional.empty();
    protected List<BlockBreak> breaks = Collections.emptyList();

    @Override
    public String asset() {
        return checkNotNull(this.asset, "asset");
    }

    @Override
    public String id() {
        return checkNotNull(this.id, "id");
    }

    @Override
    public B id(final String asset, final String id) {
        this.asset = asset;
        this.id = id;
        return (B) this;
    }

    @Override
    public Material material() {
        return checkNotNull(this.material, "material");
    }

    @Override
    public B material(final Material material) {
        this.material = material;
        return (B) this;
    }

    @Override
    public MapColor mapColor() {
        return checkNotNull(this.mapColor, "map color");
    }

    @Override
    public B mapColor(final MapColor color) {
        this.mapColor = color;
        return (B) this;
    }

    @Override
    public CollisionBox collisionAABB() {
        return this.collisionAABB;
    }

    @Override
    public B collisionAABB(final CollisionBox bb) {
        this.collisionAABB = bb;
        return (B) this;
    }

    @Override
    public WireFrame wireFrameAABB() {
        return this.wireFrameAABB;
    }

    @Override
    public B wireFrameAABB(final WireFrame bb) {
        this.wireFrameAABB = bb;
        return (B) this;
    }

    @Override
    public OptionalDouble slipperiness() {
        return this.slipperiness;
    }

    @Override
    public B slipperiness(final double slipperiness) {
        this.slipperiness = OptionalDouble.of(slipperiness);
        return (B) this;
    }

    @Override
    public OptionalDouble hardness() {
        return this.hardness;
    }

    @Override
    public B hardness(final float hardness) {
        this.hardness = OptionalDouble.of(hardness);
        return (B) this;
    }

    @Override
    public OptionalDouble lightEmission() {
        return this.lightEmission;
    }

    @Override
    public B lightEmission(final float emission) {
        this.lightEmission = OptionalDouble.of(emission);
        return (B) this;
    }

    @Override
    public OptionalInt lightOpacity() {
        return this.lightOpacity;
    }

    @Override
    public B lightOpacity(final int opacity) {
        this.lightOpacity = OptionalInt.of(opacity);
        return (B) this;
    }

    @Override
    public OptionalDouble resistance() {
        return this.resistance;
    }

    @Override
    public B resistance(final float resistance) {
        this.resistance = OptionalDouble.of(resistance);
        return (B) this;
    }

    @Override
    public Optional<CatalogDelegate<BlockSoundGroup>> soundGroup() {
        return this.soundGroup;
    }

    @Override
    public B soundGroup(final CatalogDelegate<BlockSoundGroup> group) {
        this.soundGroup = Optional.of(group);
        return (B) this;
    }

    @Override
    public List<BlockBreak> breaks() {
        return this.breaks;
    }

    @Override
    public B breaks(final List<BlockBreak> breaks) {
        this.breaks = breaks;
        return (B) this;
    }

    @Override
    public B from(final BlockStateDefinitionBuilder<?> definition) {
        return this.from((AbstractBlockStateDefinitionBuilder<B>) definition);
    }

    private B from(final AbstractBlockStateDefinitionBuilder<B> definition) {
        this.material = definition.material;
        this.mapColor = definition.mapColor;
        this.collisionAABB = definition.collisionAABB;
        this.wireFrameAABB = definition.wireFrameAABB;
        this.slipperiness = definition.slipperiness;
        this.hardness = definition.hardness;
        this.lightEmission = definition.lightEmission;
        this.lightOpacity = definition.lightOpacity;
        this.resistance = definition.resistance;
        this.soundGroup = definition.soundGroup;
        this.breaks = definition.breaks;
        return (B) this;
    }

}
