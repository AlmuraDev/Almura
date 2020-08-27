/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf;

import com.almuradev.almura.asm.mixin.accessors.block.BlockAccessor;
import com.almuradev.almura.asm.mixin.accessors.block.BlockLeavesAccessor;
import com.almuradev.content.type.block.StateMappedBlock;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinition;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

public final class LeafBlockImpl extends BlockLeaves implements LeafBlock, StateMappedBlock {
    @Deprecated private static final int LEGACY_DECAYABLE = 1;
    @Deprecated private static final int LEGACY_CHECK_DECAY = 2;
    private final LeafBlockStateDefinition definition;
    private int[] metaCache;

    LeafBlockImpl(final LeafBlockBuilder builder) {
        ((BlockAccessor) (Object) this).accessor$setDisplayOnCreativeTab(null);
        builder.fill(this);
        this.definition = builder.singleState();
        this.definition.fill(this);
        this.setDefaultState(
                this.blockState.getBaseState()
                        .withProperty(CHECK_DECAY, true)
                        .withProperty(DECAYABLE, true)
        );
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE);
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(final int data) {
        return this.getDefaultState()
                .withProperty(DECAYABLE, (data & LEGACY_DECAYABLE) > 0)
                .withProperty(CHECK_DECAY, (data & LEGACY_CHECK_DECAY) > 0);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return !((BlockLeavesAccessor) (Object) this).accessor$getIsLeavesFancy();
    }

    @Deprecated
    @Override
    public int getMetaFromState(final IBlockState state) {
        int data = 0;
        if (state.getValue(DECAYABLE)) {
            data |= LEGACY_DECAYABLE;
        }
        if (state.getValue(CHECK_DECAY)) {
            data |= LEGACY_CHECK_DECAY;
        }
        return data;
    }

    @Override
    public LeafBlockStateDefinition definition(final IBlockState state) {
        return this.definition;
    }

    @Override
    public BlockPlanks.EnumType getWoodType(final int meta) {
        return BlockPlanks.EnumType.DARK_OAK; // I'm not even related to dark_oak, but this goes away with 1.13
    }

    @Override
    public List<ItemStack> onSheared(@Nonnull final ItemStack item, final IBlockAccess world, final BlockPos pos, final int fortune) {
        return NonNullList.withSize(1, new ItemStack(this));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IStateMapper createStateMapper() {
        return new StateMap.Builder()
                .ignore(CHECK_DECAY, DECAYABLE)
                .build();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return (((BlockLeavesAccessor) (Object) this).accessor$getIsLeavesFancy() || blockAccess.getBlockState(pos.offset(side)).getBlock() != this) && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        //Todo: the following needs to be updated from 1.7.10 to make work with 1.12.2 implementation.
        // Pulled from : https://github.com/AlmuraDev/Almura/blob/1.7.10/src/main/java/com/almuradev/almura/pack/tree/PackLeaves.java
        /*
        if (!worldIn.isRemote && decayNode != null && decayNode.isEnabled()) {
            byte b0 = 4;
            int i1 = b0 + 1;
            byte b1 = 32;
            int j1 = b1 * b1;
            int k1 = b1 / 2;

            int l = this.getMetaFromState(state);

            if ((l & 8) != 0 && (l & 4) == 0) {
                if (this.metaCache == null) {
                    this.metaCache = new int[b1 * b1 * b1];
                }

                int l1;
                if (((WorldServer) worldIn).getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4)) {
                    int i2;
                    int j2;

                    for (l1 = -b0; l1 <= b0; ++l1) {
                        for (i2 = -b0; i2 <= b0; ++i2) {
                            for (j2 = -b0; j2 <= b0; ++j2) {
                                Block block = worldIn.getBlockState(new BlockPos(pos.getX() + l1, pos.getY() + i2, pos.getZ() + j2)).getBlock();
                                GameObject found = null;
                                // Check if we have something that prevents decaying nearby
                                for (GameObjectProperty prop : decayNode.getPreventDecayProperties()) {
                                    final Object minecraftObject =
                                        prop.getSource().minecraftObject instanceof ItemBlock ? ((ItemBlock) prop.getSource().minecraftObject).blockInstance : prop.getSource().minecraftObject;
                                    if (minecraftObject == block) {
                                        found = prop.getSource();
                                        break;
                                    }
                                }

                                if (found == null) {
                                    
                                    if (state.getBlock().isLeaves(state, worldIn, new BlockPos(pos.getX() + l1, pos.getY() + i2, pos.getZ() + j2))) {
                                        this.metaCache[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -2;
                                    } else {
                                        this.metaCache[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -1;
                                    }
                                } else {
                                    this.metaCache[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = 0;
                                }
                            }
                        }
                    }

                    for (l1 = 1; l1 <= 4; ++l1) {
                        for (i2 = -b0; i2 <= b0; ++i2) {
                            for (j2 = -b0; j2 <= b0; ++j2) {
                                for (int k2 = -b0; k2 <= b0; ++k2) {
                                    if (this.metaCache[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1] == l1 - 1) {
                                        if (this.metaCache[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2) {
                                            this.metaCache[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                        }

                                        if (this.metaCache[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2) {
                                            this.metaCache[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                        }

                                        if (this.metaCache[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] == -2) {
                                            this.metaCache[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] = l1;
                                        }

                                        if (this.metaCache[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] == -2) {
                                            this.metaCache[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] = l1;
                                        }

                                        if (this.metaCache[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] == -2) {
                                            this.metaCache[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] = l1;
                                        }

                                        if (this.metaCache[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] == -2) {
                                            this.metaCache[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] = l1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                l1 = this.metaCache[k1 * j1 + k1 * b1 + k1];

                if (l1 >= 0) {
                    worldIn.setBlockMetadataWithNotify(x, y, z, l & -9, 4); //old
                } else {
                    //Todo: drop leaf as item here?
                    worldIn.setBlockToAir(pos);
                }
            }
        }

        if (spreadNode.isEnabled() && !spreadNode.getValue().isEmpty()) {
            final Block currentBlock = worldIn.getBlockState(pos).getBlock();
            if (currentBlock == this) {
                for (ForgeDirection dir : ForgeDirection.values()) {
                    final BlockPos toPos = pos.offset(dir);
                    final Block dirBlock = worldIn.getBlockState(pos).getBlock();
                    final int blockMetadata = dirBlock.getMetaFromState(state);

                    ReplacementProperty matched = null;

                    for (ReplacementProperty property : spreadNode.getValue()) {
                        Block toCheck;

                        if (property.getReplacementObj().minecraftObject instanceof ItemBlock) {
                            toCheck = ((ItemBlock) property.getReplacementObj().minecraftObject).blockInstance;
                        } else {
                            toCheck = (Block) property.getReplacementObj().minecraftObject;
                        }

                        if (toCheck == dirBlock && property.getReplacementObj().data == blockMetadata) {
                            matched = property;
                            break;
                        }
                    }

                    if (matched != null) {
                        final double chance = matched.getSource().getValueWithinRange();
                        if (rand.nextDouble() <= (chance / 100)) {
                            Block toReplace;

                            if (matched.getWithObj().minecraftObject instanceof ItemBlock) {
                                toReplace = ((ItemBlock) matched.getWithObj().minecraftObject).blockInstance;
                            } else {
                                toReplace = (Block) matched.getWithObj().minecraftObject;
                            }
                            worldIn.setBlock(toPos.getX(), toPos.getY(), toPos.getZ(), toReplace, matched.getWithObj().data, 3);
                        }
                    }
                }
            }
        } */
    }
}
