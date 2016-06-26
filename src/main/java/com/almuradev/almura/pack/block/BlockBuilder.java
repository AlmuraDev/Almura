/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.block;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Almura;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class BlockBuilder implements PackBlockObject.Builder<PackBlockObject, BlockBuilder> {
    protected CreativeTabs tabs;
    protected Material material;
    protected MapColor mapColor;

    public BlockBuilder() {
        this.reset();
    }

    @Override
    public PackBlockObject.Builder<PackBlockObject, BlockBuilder> material(Material material) {
        checkNotNull(material);
        this.material = material;
        return this;
    }

    @Override
    public PackBlockObject.Builder<PackBlockObject, BlockBuilder> mapColor(MapColor mapColor) {
        checkNotNull(mapColor);
        this.mapColor = mapColor;
        return this;
    }

    @Override
    public PackBlockObject.Builder<PackBlockObject, BlockBuilder> creativeTab(CreativeTabs tab) {
        this.tabs = tab;
        return this;
    }

    @Override
    public BlockBuilder from(PackBlockObject value) {
        checkNotNull(value);
        final Block block = (Block) value;
        material(block.getMaterial(block.getDefaultState()));
        mapColor(block.getMapColor(block.getDefaultState()));
        return this;
    }

    @Override
    public BlockBuilder reset() {
        this.tabs = null;
        this.material = Material.GROUND;
        this.mapColor = MapColor.DIRT;
        return this;
    }

    @Override
    public PackBlockObject build(String id, String name) {
        final Block block = new Block(this.material, this.mapColor);
        block.setRegistryName(Almura.PLUGIN_ID, id);

        if (this.tabs != null) {
            block.setCreativeTab(this.tabs);
        }

        return (PackBlockObject) GameRegistry.register(block);
    }
}
