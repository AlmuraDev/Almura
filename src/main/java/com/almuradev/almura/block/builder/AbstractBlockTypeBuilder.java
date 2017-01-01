/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.builder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.AbstractMaterialTypeBuilder;
import com.almuradev.almura.block.BuildableBlockType;
import com.almuradev.almura.block.impl.GenericBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Optional;

@SuppressWarnings("unchecked")
public abstract class AbstractBlockTypeBuilder<BLOCK extends BuildableBlockType, BUILDER extends AbstractBlockTypeBuilder<BLOCK, BUILDER>>
        extends AbstractMaterialTypeBuilder<BLOCK, BUILDER> implements BuildableBlockType.Builder<BLOCK, BUILDER> {

    private Material material;
    private MapColor mapColor;
    private float hardness;
    private float resistance;

    @Override
    public final BUILDER material(Material material) {
        checkNotNull(material);
        this.material = material;
        return (BUILDER) this;
    }

    @Override
    public final Optional<Material> material() {
        return Optional.ofNullable(this.material);
    }

    @Override
    public final BUILDER mapColor(MapColor mapColor) {
        checkNotNull(mapColor);
        this.mapColor = mapColor;
        return (BUILDER) this;
    }

    @Override
    public final Optional<MapColor> mapColor() {
        return Optional.ofNullable(this.mapColor);
    }

    @Override
    public final BUILDER hardness(float hardness) {
        this.hardness = hardness;
        return (BUILDER) this;
    }

    @Override
    public final float hardness() {
        return this.hardness;
    }

    @Override
    public final BUILDER resistance(float resistance) {
        this.resistance = resistance;
        return (BUILDER) this;
    }

    @Override
    public final float resistance() {
        return this.resistance;
    }

    @Override
    public BUILDER from(BuildableBlockType value) {
        checkNotNull(value);
        final Block block = (Block) value;
        this.material(block.getMaterial(block.getDefaultState()));
        this.mapColor(block.getMapColor(block.getDefaultState()));
        this.hardness(block.getBlockHardness(block.getDefaultState(), null, null));
        this.resistance(block.blockResistance);
        return (BUILDER) this;
    }

    @Override
    public BUILDER reset() {
        this.material = Material.GROUND;
        this.mapColor = MapColor.DIRT;
        this.hardness = -1f;
        this.resistance = 6000000.0F;
        return (BUILDER) this;
    }

    public static final class BuilderImpl extends AbstractBlockTypeBuilder<BuildableBlockType, BuilderImpl> {

        @Override
        public BuildableBlockType build(String id) {
            checkNotNull(id);
            checkState(!id.isEmpty(), "Id cannot be empty!");

            final String[] idAndPath = id.split(":");
            final Block block = GameRegistry.register(new GenericBlock(idAndPath[0], idAndPath[1], this).setRegistryName(idAndPath[1]));
            final Item item = GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));

            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(id, "inventory"));

            return (BuildableBlockType) (Object) block;
        }
    }
}
