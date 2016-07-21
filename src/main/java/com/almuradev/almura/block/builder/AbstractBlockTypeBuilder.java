/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Almura;
import com.almuradev.almura.api.CreativeTab;
import com.almuradev.almura.api.block.BuildableBlockType;
import com.almuradev.almura.block.GenericBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

@SuppressWarnings("unchecked")
public abstract class AbstractBlockTypeBuilder<BLOCK extends BuildableBlockType, BUILDER extends AbstractBlockTypeBuilder<BLOCK, BUILDER>> implements
        BuildableBlockType.Builder<BLOCK, BUILDER> {
    public String dictName;
    public CreativeTab tab;
    public Material material;
    public MapColor mapColor;

    public AbstractBlockTypeBuilder() {
        this.reset();
    }

    @Override
    public BUILDER unlocalizedName(String dictName) {
        checkNotNull(dictName);
        this.dictName = dictName;
        return (BUILDER) this;
    }

    @Override
    public BUILDER material(Material material) {
        checkNotNull(material);
        this.material = material;
        return (BUILDER) this;
    }

    @Override
    public BUILDER mapColor(MapColor mapColor) {
        checkNotNull(mapColor);
        this.mapColor = mapColor;
        return (BUILDER) this;
    }

    @Override
    public BUILDER creativeTab(CreativeTab tab) {
        this.tab = tab;
        return (BUILDER) this;
    }

    @Override
    public BUILDER from(BuildableBlockType value) {
        checkNotNull(value);
        final Block block = (Block) value;
        material(block.getMaterial(block.getDefaultState()));
        mapColor(block.getMapColor(block.getDefaultState()));
        return (BUILDER) this;
    }

    @Override
    public BUILDER reset() {
        this.tab = null;
        this.material = Material.GROUND;
        this.mapColor = MapColor.DIRT;
        return (BUILDER) this;
    }

    public static final class BuilderImpl extends AbstractBlockTypeBuilder<BuildableBlockType, BuilderImpl> {

        @Override
        public BuildableBlockType build(String id) {
            checkNotNull(id);

            final GenericBlock block = GameRegistry.register(new GenericBlock(Almura.PLUGIN_ID, id, this));

            final ItemBlock itemBlock = new ItemBlock(block);
            itemBlock.setRegistryName(Almura.PLUGIN_ID, id);

            // TODO Make this configurable and make Almura GenericItemBlock
            GameRegistry.register(itemBlock);

            return (BuildableBlockType) (Object) block;
        }
    }
}
