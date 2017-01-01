/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.impl;

import com.almuradev.almura.block.BuildableBlockType;
import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.api.CatalogType;

public final class GenericBlock extends Block {

    private final BuildableBlockType apiType = (BuildableBlockType) (Object) this;

    public GenericBlock(String modid, String id, BuildableBlockType.Builder<?, ?> builder) {
        super(builder.material().orElse(null), builder.mapColor().orElse(null));
        this.setUnlocalizedName(modid + "." + id.replace("/", "."));
        this.setCreativeTab((CreativeTabs) (Object) builder.creativeTab().orElse(null));
        this.setHardness(builder.hardness());
        this.setResistance(builder.resistance());
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", ((CatalogType) (Object) this).getId())
                .add("unlocalizedName", this.getUnlocalizedName())
                .add("material", this.blockMaterial)
                .add("mapColor", this.blockMapColor)
                .add("creativeTab", this.apiType.getCreativeTab().orElse(null))
                .toString();
    }
}
