/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.block;

import static net.minecraft.block.BlockVine.getPropertyFor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@SuppressWarnings("deprecation")
@Mixin(value = BlockVine.class, priority = 999)
public abstract class MixinBlockVine extends Block {

    @Shadow static final PropertyBool UP = PropertyBool.create("up");
    @Shadow static final PropertyBool NORTH = PropertyBool.create("north");
    @Shadow static final PropertyBool EAST = PropertyBool.create("east");
    @Shadow static final PropertyBool SOUTH = PropertyBool.create("south");
    @Shadow static final PropertyBool WEST = PropertyBool.create("west");
    @Shadow public abstract boolean canAttachTo(World p_193395_1_, BlockPos p_193395_2_, EnumFacing p_193395_3_);

    public MixinBlockVine(final Material material) {
        super(material);
    }

    //TODO:  There must be a better way to do this.
    @Overwrite
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            if (worldIn.rand.nextInt(4) == 0 && !worldIn.getWorldInfo().getWorldName().equalsIgnoreCase("orilla"))  //Specifically for Orilla only at the moment.
            {
                int i = 4;
                int j = 5;
                boolean flag = false;
                label181:

                for (int k = -4; k <= 4; ++k)
                {
                    for (int l = -4; l <= 4; ++l)
                    {
                        for (int i1 = -1; i1 <= 1; ++i1)
                        {
                            if (worldIn.getBlockState(pos.add(k, i1, l)).getBlock() == this)
                            {
                                --j;

                                if (j <= 0)
                                {
                                    flag = true;
                                    break label181;
                                }
                            }
                        }
                    }
                }

                EnumFacing enumfacing1 = EnumFacing.random(rand);
                BlockPos blockpos2 = pos.up();

                if (enumfacing1 == EnumFacing.UP && pos.getY() < 255 && worldIn.isAirBlock(blockpos2))
                {
                    IBlockState iblockstate2 = state;

                    for (EnumFacing enumfacing2 : EnumFacing.Plane.HORIZONTAL)
                    {
                        if (rand.nextBoolean() && this.canAttachTo(worldIn, blockpos2, enumfacing2.getOpposite()))
                        {
                            iblockstate2 = iblockstate2.withProperty(getPropertyFor(enumfacing2), Boolean.valueOf(true));
                        }
                        else
                        {
                            iblockstate2 = iblockstate2.withProperty(getPropertyFor(enumfacing2), Boolean.valueOf(false));
                        }
                    }

                    if (((Boolean)iblockstate2.getValue(NORTH)).booleanValue() || ((Boolean)iblockstate2.getValue(EAST)).booleanValue() || ((Boolean)iblockstate2.getValue(SOUTH)).booleanValue() || ((Boolean)iblockstate2.getValue(WEST)).booleanValue())
                    {
                        worldIn.setBlockState(blockpos2, iblockstate2, 2);
                    }
                }
                else if (enumfacing1.getAxis().isHorizontal() && !((Boolean)state.getValue(getPropertyFor(enumfacing1))).booleanValue())
                {
                    if (!flag)
                    {
                        BlockPos blockpos4 = pos.offset(enumfacing1);
                        IBlockState iblockstate3 = worldIn.getBlockState(blockpos4);
                        Block block1 = iblockstate3.getBlock();

                        if (block1.getMaterial(iblockstate3) == Material.AIR)
                        {
                            EnumFacing enumfacing3 = enumfacing1.rotateY();
                            EnumFacing enumfacing4 = enumfacing1.rotateYCCW();
                            boolean flag1 = ((Boolean)state.getValue(getPropertyFor(enumfacing3))).booleanValue();
                            boolean flag2 = ((Boolean)state.getValue(getPropertyFor(enumfacing4))).booleanValue();
                            BlockPos blockpos = blockpos4.offset(enumfacing3);
                            BlockPos blockpos1 = blockpos4.offset(enumfacing4);

                            if (flag1 && this.canAttachTo(worldIn, blockpos.offset(enumfacing3), enumfacing3))
                            {
                                worldIn.setBlockState(blockpos4, this.getDefaultState().withProperty(getPropertyFor(enumfacing3), Boolean.valueOf(true)), 2);
                            }
                            else if (flag2 && this.canAttachTo(worldIn, blockpos1.offset(enumfacing4), enumfacing4))
                            {
                                worldIn.setBlockState(blockpos4, this.getDefaultState().withProperty(getPropertyFor(enumfacing4), Boolean.valueOf(true)), 2);
                            }
                            else if (flag1 && worldIn.isAirBlock(blockpos) && this.canAttachTo(worldIn, blockpos, enumfacing1))
                            {
                                worldIn.setBlockState(blockpos, this.getDefaultState().withProperty(getPropertyFor(enumfacing1.getOpposite()), Boolean.valueOf(true)), 2);
                            }
                            else if (flag2 && worldIn.isAirBlock(blockpos1) && this.canAttachTo(worldIn, blockpos1, enumfacing1))
                            {
                                worldIn.setBlockState(blockpos1, this.getDefaultState().withProperty(getPropertyFor(enumfacing1.getOpposite()), Boolean.valueOf(true)), 2);
                            }
                        }
                        else if (iblockstate3.getBlockFaceShape(worldIn, blockpos4, enumfacing1) == BlockFaceShape.SOLID)
                        {
                            worldIn.setBlockState(pos, state.withProperty(getPropertyFor(enumfacing1), Boolean.valueOf(true)), 2);
                        }
                    }
                }
                else
                {
                    if (pos.getY() > 1)
                    {
                        BlockPos blockpos3 = pos.down();
                        IBlockState iblockstate = worldIn.getBlockState(blockpos3);
                        Block block = iblockstate.getBlock();

                        if (block.getMaterial(iblockstate) == Material.AIR)
                        {
                            IBlockState iblockstate1 = state;

                            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
                            {
                                if (rand.nextBoolean())
                                {
                                    iblockstate1 = iblockstate1.withProperty(getPropertyFor(enumfacing), Boolean.valueOf(false));
                                }
                            }

                            if (((Boolean)iblockstate1.getValue(NORTH)).booleanValue() || ((Boolean)iblockstate1.getValue(EAST)).booleanValue() || ((Boolean)iblockstate1.getValue(SOUTH)).booleanValue() || ((Boolean)iblockstate1.getValue(WEST)).booleanValue())
                            {
                                worldIn.setBlockState(blockpos3, iblockstate1, 2);
                            }
                        }
                        else if (block == this)
                        {
                            IBlockState iblockstate4 = iblockstate;

                            for (EnumFacing enumfacing5 : EnumFacing.Plane.HORIZONTAL)
                            {
                                PropertyBool propertybool = getPropertyFor(enumfacing5);

                                if (rand.nextBoolean() && ((Boolean)state.getValue(propertybool)).booleanValue())
                                {
                                    iblockstate4 = iblockstate4.withProperty(propertybool, Boolean.valueOf(true));
                                }
                            }

                            if (((Boolean)iblockstate4.getValue(NORTH)).booleanValue() || ((Boolean)iblockstate4.getValue(EAST)).booleanValue() || ((Boolean)iblockstate4.getValue(SOUTH)).booleanValue() || ((Boolean)iblockstate4.getValue(WEST)).booleanValue())
                            {
                                worldIn.setBlockState(blockpos3, iblockstate4, 2);
                            }
                        }
                    }
                }
            }
        }
    }


}
