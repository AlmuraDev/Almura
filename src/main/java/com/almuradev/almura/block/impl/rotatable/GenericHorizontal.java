/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.impl.rotatable;

import com.almuradev.almura.block.builder.rotatable.HorizontalTypeBuilderImpl;
import com.almuradev.almura.block.rotatable.HorizontalType;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.CatalogType;

public final class GenericHorizontal extends BlockHorizontal {

    private final HorizontalType apiType = (HorizontalType) (Object) this;

    public GenericHorizontal(String modid, String id, HorizontalTypeBuilderImpl builder) {
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
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", ((CatalogType) (Object) this).getId())
                .add("unlocalizedName", this.getUnlocalizedName())
                .add("material", this.blockMaterial)
                .add("mapColor", this.blockMapColor)
                .add("creativeTab", this.apiType.getCreativeTab().orElse(null))
                .toString();
    }
}
