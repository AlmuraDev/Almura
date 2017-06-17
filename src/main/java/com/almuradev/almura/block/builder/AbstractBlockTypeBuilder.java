/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.builder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.AbstractMaterialTypeBuilder;
import com.almuradev.almura.Constants;
import com.almuradev.almura.block.BuildableBlockType;
import com.almuradev.almura.block.impl.GenericBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

@SuppressWarnings("unchecked")
public abstract class AbstractBlockTypeBuilder<BLOCK extends BuildableBlockType, BUILDER extends AbstractBlockTypeBuilder<BLOCK, BUILDER>>
        extends AbstractMaterialTypeBuilder<BLOCK, BUILDER> implements BuildableBlockType.Builder<BLOCK, BUILDER> {

    private Material material;
    private MapColor mapColor;
    private OptionalDouble hardness = OptionalDouble.empty();
    private OptionalDouble lightEmission = OptionalDouble.empty();
    private OptionalInt lightOpacity = OptionalInt.empty();
    private OptionalDouble resistance = OptionalDouble.empty();

    @Override
    public final Optional<Material> material() {
        return Optional.ofNullable(this.material);
    }

    @Override
    public final BUILDER material(Material material) {
        this.material = checkNotNull(material);
        return (BUILDER) this;
    }

    @Override
    public final Optional<MapColor> mapColor() {
        return Optional.ofNullable(this.mapColor);
    }

    @Override
    public final BUILDER mapColor(MapColor mapColor) {
        this.mapColor = checkNotNull(mapColor);
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
    public BUILDER from(BuildableBlockType value) {
        super.from(value);

        final Block block = (Block) value;
        final IBlockState defaultState = block.getDefaultState();

        this.material(block.getMaterial(defaultState));
        // TODO Figure out how to do blockMapColor now
        this.hardness(block.getBlockHardness(defaultState, null, null));
        this.lightEmission(block.getLightValue(defaultState, null, null));
        this.lightOpacity(block.getLightOpacity(defaultState, null, null));
        this.resistance(block.blockResistance);

        return (BUILDER) this;
    }

    @Override
    public BUILDER reset() {
        super.reset();

        this.material = Material.GROUND;
        this.mapColor = MapColor.DIRT;
        this.hardness = OptionalDouble.empty();
        this.lightEmission = OptionalDouble.empty();
        this.lightOpacity = OptionalInt.empty();
        this.resistance = OptionalDouble.empty();

        return (BUILDER) this;
    }

    @Override
    public BLOCK build(String id) {
        checkNotNull(id);
        checkState(!id.isEmpty(), "Id cannot be empty!");

        final String[] idAndPath = id.split(":");
        final String modid = idAndPath[0];
        final String name = idAndPath[1];

        final Block block = GameRegistry.register(this.createBlock((BUILDER) this).setRegistryName(name));
        block.setUnlocalizedName(modid + "." + id.replace(Constants.Plugin.ID.concat(":"), "").replace("/", "."));
        block.setCreativeTab((CreativeTabs) this.creativeTab().orElse(null));
        this.hardness().ifPresent(hardness -> block.setHardness((float) hardness));
        this.resistance().ifPresent(resistance -> block.setResistance((float) resistance));
        this.lightEmission().ifPresent(emission -> block.setLightLevel((float) emission));
        this.lightOpacity().ifPresent(block::setLightOpacity);

        final Item item = GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));

        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(id, "inventory"));

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
