/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.builder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateMaterialAttributes;
import com.almuradev.almura.content.block.impl.BlockAABB;
import com.almuradev.almura.content.block.data.blockbreak.BlockBreak;
import com.almuradev.almura.content.block.sound.BlockSoundGroup;
import com.almuradev.almura.content.block.BuildableBlockType;
import com.almuradev.almura.content.block.impl.GenericBlock;
import com.almuradev.almura.content.material.AbstractMaterialTypeBuilder;
import com.almuradev.almura.content.material.MapColor;
import com.almuradev.almura.content.material.Material;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.annotation.Nullable;

@SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
public abstract class AbstractBlockTypeBuilder<BLOCK extends BuildableBlockType, BUILDER extends AbstractBlockTypeBuilder<BLOCK, BUILDER>>
        extends AbstractMaterialTypeBuilder<BLOCK, BUILDER> implements BuildableBlockType.Builder<BLOCK, BUILDER> {

    @Nullable private Material material;
    @Nullable private MapColor mapColor;
    private BlockAABB.Collision collisionAABB = BlockAABB.Collision.VANILLA;
    private BlockAABB.WireFrame wireFrameAABB = BlockAABB.WireFrame.VANILLA;
    private OptionalDouble slipperiness = OptionalDouble.empty();
    private OptionalDouble hardness = OptionalDouble.empty();
    private OptionalDouble lightEmission = OptionalDouble.empty();
    private OptionalInt lightOpacity = OptionalInt.empty();
    private OptionalDouble resistance = OptionalDouble.empty();
    @Nullable private BlockSoundGroup soundGroup;
    private List<BlockBreak> breaks = Collections.emptyList();

    @Override
    public final Material material() {
        return checkNotNull(this.material);
    }

    @Override
    public final BUILDER material(Material material) {
        this.material = checkNotNull(material);
        return (BUILDER) this;
    }

    @Override
    public final MapColor mapColor() {
        return checkNotNull(this.mapColor);
    }

    @Override
    public final BUILDER mapColor(MapColor mapColor) {
        this.mapColor = checkNotNull(mapColor);
        return (BUILDER) this;
    }

    @Override
    public final BlockAABB.Collision collisionAABB() {
        return this.collisionAABB;
    }

    @Override
    public final BUILDER collisionAABB(final BlockAABB.Collision bb) {
        this.collisionAABB = bb;
        return (BUILDER) this;
    }

    @Override
    public final BlockAABB.WireFrame wireFrameAABB() {
        return this.wireFrameAABB;
    }

    @Override
    public final BUILDER wireFrameAABB(final BlockAABB.WireFrame bb) {
        this.wireFrameAABB = bb;
        return (BUILDER) this;
    }

    @Override
    public OptionalDouble slipperiness() {
        return this.slipperiness;
    }

    @Override
    public BUILDER slipperiness(double slipperiness) {
        this.slipperiness = OptionalDouble.of(slipperiness);
        return (BUILDER) this;
    }

    @Override
    public final OptionalDouble hardness() {
        return this.hardness;
    }

    @Override
    public final BUILDER hardness(float hardness) {
        this.hardness = OptionalDouble.of(hardness);
        return (BUILDER) this;
    }

    @Override
    public final OptionalDouble lightEmission() {
        return this.lightEmission;
    }

    @Override
    public BUILDER lightEmission(float lightEmission) {
        this.lightEmission = OptionalDouble.of(lightEmission);
        return (BUILDER) this;
    }

    @Override
    public final OptionalInt lightOpacity() {
        return this.lightOpacity;
    }

    @Override
    public BUILDER lightOpacity(int lightOpacity) {
        this.lightOpacity = OptionalInt.of(lightOpacity);
        return (BUILDER) this;
    }

    @Override
    public final OptionalDouble resistance() {
        return this.resistance;
    }

    @Override
    public final BUILDER resistance(float resistance) {
        this.resistance = OptionalDouble.of(resistance);
        return (BUILDER) this;
    }

    @Override
    public Optional<BlockSoundGroup> soundGroup() {
        return Optional.ofNullable(this.soundGroup);
    }

    @Override
    public BUILDER soundGroup(BlockSoundGroup group) {
        this.soundGroup = group;
        return (BUILDER) this;
    }

    @Override
    public List<BlockBreak> breaks() {
        return this.breaks;
    }

    @Override
    public BUILDER breaks(List<BlockBreak> breaks) {
        this.breaks = breaks;
        return (BUILDER) this;
    }

    @Override
    public BUILDER from(BuildableBlockType value) {
        super.from((BLOCK) value);

        final Block block = (Block) value;
        final IBlockState defaultState = block.getDefaultState();

        this.material((Material) block.getMaterial(defaultState));
        // TODO Figure out how to do blockMapColor now
        this.hardness(block.getBlockHardness(defaultState, null, null));
        this.lightEmission(block.getLightValue(defaultState, null, null));
        this.lightOpacity(block.getLightOpacity(defaultState, null, null));
        this.resistance(block.blockResistance);
        this.soundGroup((BlockSoundGroup) block.getSoundType());

        return (BUILDER) this;
    }

    @Override
    public BUILDER reset() {
        super.reset();

        this.material = (Material) net.minecraft.block.material.Material.GROUND;
        this.mapColor = (MapColor) net.minecraft.block.material.MapColor.DIRT;
        this.collisionAABB = BlockAABB.Collision.VANILLA;
        this.wireFrameAABB = BlockAABB.WireFrame.VANILLA;
        this.hardness = OptionalDouble.empty();
        this.lightEmission = OptionalDouble.empty();
        this.lightOpacity = OptionalInt.empty();
        this.resistance = OptionalDouble.empty();
        this.soundGroup = null;

        return (BUILDER) this;
    }

    @Override
    public BLOCK build(String id) {
        checkNotNull(id);
        checkState(!id.isEmpty(), "Id cannot be empty!");

        final String registryName = id.split(":")[1];

        final Block block = this.createBlock((BUILDER) this).setRegistryName(registryName);
        block.setUnlocalizedName(id.replace(":", ".").replace("/", "."));

        ((IMixinDelegateMaterialAttributes) block).setItemGroupDelegate(this.itemGroup());
        this.slipperiness().ifPresent(slipperiness -> block.slipperiness = (float) slipperiness);
        this.soundGroup().ifPresent(sound -> block.setSoundType((SoundType) sound));
        this.hardness().ifPresent(hardness -> block.setHardness((float) hardness));
        this.resistance().ifPresent(resistance -> block.setResistance((float) resistance));
        this.lightEmission().ifPresent(emission -> block.setLightLevel((float) emission));
        this.lightOpacity().ifPresent(block::setLightOpacity);

        return (BLOCK) (Object) block;
    }

    protected abstract Block createBlock(BUILDER builder);

    public static final class BuilderImpl extends AbstractBlockTypeBuilder<BuildableBlockType, BuilderImpl> {

        @Override
        protected Block createBlock(BuilderImpl builder) {
            return new GenericBlock(builder);
        }
    }
}
