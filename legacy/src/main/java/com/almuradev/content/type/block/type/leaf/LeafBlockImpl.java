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
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.block.type.leaf.processor.preventdecay.PreventDecay;
import com.almuradev.content.type.block.type.leaf.processor.spread.Spread;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinition;
import com.almuradev.content.util.DoubleRanges;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.kyori.violet.Lazy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.block.BlockState;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class LeafBlockImpl extends BlockLeaves implements LeafBlock, StateMappedBlock {
    @Deprecated private static final int LEGACY_DECAYABLE = 1;
    @Deprecated private static final int LEGACY_CHECK_DECAY = 2;
    private final LeafBlockStateDefinition definition;

    LeafBlockImpl(final LeafBlockBuilder builder) {
        ((BlockAccessor) (Object) this).accessor$setDisplayOnCreativeTab(null);
        builder.fill(this);
        this.definition = builder.singleState();
        this.definition.fill(this);

        this.setSoundType(SoundType.PLANT);
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
        // Always return false here because our images for leaves are expecting them to be transparent.
        return false;
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
    public BlockRenderLayer getRenderLayer() {
        return this.leavesFancy ? BlockRenderLayer.CUTOUT_MIPPED : BlockRenderLayer.SOLID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return (((BlockLeavesAccessor) (Object) this).accessor$getIsLeavesFancy() || blockAccess.getBlockState(pos.offset(side)).getBlock() != this) && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote)
        {
            if (((Boolean)state.getValue(CHECK_DECAY)).booleanValue() && ((Boolean)state.getValue(DECAYABLE)).booleanValue())
            {
                int i = 4;
                int j = 5;
                int k = pos.getX();
                int l = pos.getY();
                int i1 = pos.getZ();
                int j1 = 32;
                int k1 = 1024;
                int l1 = 16;

                if (((BlockLeavesAccessor) (Object) this).accessor$getSurroundings() == null)
                {
                    ((BlockLeavesAccessor) (Object) this).accessor$setSurroundings(new int[32768]);
                }

                final int[] surroundings = ((BlockLeavesAccessor) (Object) this).accessor$getSurroundings();

                if (!worldIn.isAreaLoaded(pos, 1)) return; // Forge: prevent decaying leaves from updating neighbors and loading unloaded chunks
                if (worldIn.isAreaLoaded(pos, 6)) // Forge: extend range from 5 to 6 to account for neighbor checks in world.markAndNotifyBlock -> world.updateObservingBlocksAt
                {
                    BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                    // Almura Start - PreventDecay: roll the biome chance once per decay check. When it passes, the configured block acts like a log
                    // for this leaf for the scan below. Chance is the chance to prevent decay (roll <= chance), matching Spread and Growth.
                    @Nullable LazyBlockState preventDecayState = null;
                    @Nullable Block preventDecayBlock = null;
                    final PreventDecay preventDecayConfig = this.definition.preventDecay;
                    if (preventDecayConfig != null) {
                        final DoubleRange chanceRoll = preventDecayConfig.getOrLoadChanceRangeForBiome(worldIn.getBiome(pos));
                        if (chanceRoll != null && rand.nextDouble() <= (DoubleRanges.random(chanceRoll, rand) / 100)) {
                            preventDecayState = preventDecayConfig.getBlock();
                            preventDecayBlock = preventDecayState.block();
                        }
                    }
                    // Almura End

                    for (int i2 = -4; i2 <= 4; ++i2)
                    {
                        for (int j2 = -4; j2 <= 4; ++j2)
                        {
                            for (int k2 = -4; k2 <= 4; ++k2)
                            {
                                IBlockState iblockstate = worldIn.getBlockState(blockpos$mutableblockpos.setPos(k + i2, l + j2, i1 + k2));
                                Block block = iblockstate.getBlock();

                                // Almura Start - Ask the leaf for a block that can keep it from decaying. Allow Vanilla logs to have first say
                                // (canSustainLeaves), then the PreventDecay block if its chance passed above. Block identity is checked first so the
                                // state test only runs on candidate blocks.
                                final boolean preventDecay = block.canSustainLeaves(iblockstate, worldIn, blockpos$mutableblockpos)
                                        || (block == preventDecayBlock && preventDecayState.partialTest(iblockstate));
                                if (!preventDecay)
                                {
                                    // Almura End
                                    if (block.isLeaves(iblockstate, worldIn, blockpos$mutableblockpos))
                                    {
                                        surroundings[(i2 + 16) * 1024 + (j2 + 16) * 32 + k2 + 16] = -2;
                                    }
                                    else
                                    {
                                        surroundings[(i2 + 16) * 1024 + (j2 + 16) * 32 + k2 + 16] = -1;
                                    }
                                }
                                else
                                {
                                    surroundings[(i2 + 16) * 1024 + (j2 + 16) * 32 + k2 + 16] = 0;
                                }
                            }
                        }
                    }

                    for (int i3 = 1; i3 <= 4; ++i3)
                    {
                        for (int j3 = -4; j3 <= 4; ++j3)
                        {
                            for (int k3 = -4; k3 <= 4; ++k3)
                            {
                                for (int l3 = -4; l3 <= 4; ++l3)
                                {
                                    if (surroundings[(j3 + 16) * 1024 + (k3 + 16) * 32 + l3 + 16] == i3 - 1)
                                    {
                                        if (surroundings[(j3 + 16 - 1) * 1024 + (k3 + 16) * 32 + l3 + 16] == -2)
                                        {
                                            surroundings[(j3 + 16 - 1) * 1024 + (k3 + 16) * 32 + l3 + 16] = i3;
                                        }

                                        if (surroundings[(j3 + 16 + 1) * 1024 + (k3 + 16) * 32 + l3 + 16] == -2)
                                        {
                                            surroundings[(j3 + 16 + 1) * 1024 + (k3 + 16) * 32 + l3 + 16] = i3;
                                        }

                                        if (surroundings[(j3 + 16) * 1024 + (k3 + 16 - 1) * 32 + l3 + 16] == -2)
                                        {
                                            surroundings[(j3 + 16) * 1024 + (k3 + 16 - 1) * 32 + l3 + 16] = i3;
                                        }

                                        if (surroundings[(j3 + 16) * 1024 + (k3 + 16 + 1) * 32 + l3 + 16] == -2)
                                        {
                                            surroundings[(j3 + 16) * 1024 + (k3 + 16 + 1) * 32 + l3 + 16] = i3;
                                        }

                                        if (surroundings[(j3 + 16) * 1024 + (k3 + 16) * 32 + (l3 + 16 - 1)] == -2)
                                        {
                                            surroundings[(j3 + 16) * 1024 + (k3 + 16) * 32 + (l3 + 16 - 1)] = i3;
                                        }

                                        if (surroundings[(j3 + 16) * 1024 + (k3 + 16) * 32 + l3 + 16 + 1] == -2)
                                        {
                                            surroundings[(j3 + 16) * 1024 + (k3 + 16) * 32 + l3 + 16 + 1] = i3;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    int l2 = surroundings[16912];

                    if (l2 >= 0)
                    {
                        worldIn.setBlockState(pos, state.withProperty(CHECK_DECAY, Boolean.valueOf(false)), 4);
                    }
                    else
                    {
                        ((BlockLeavesAccessor) (Object) this).invoker$destroy(worldIn, pos);
                    }
                }
                // Almura - When the scan area isn't loaded, leave CHECK_DECAY set and decide on a later tick. Vanilla falls through and decides
                // from the shared surroundings array, which still holds the previous leaf's scan and can wrongly decay this leaf.
            }
        }

        this.updateSpreadTick(worldIn, pos, state, rand);

    }

    private void updateSpreadTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote) {
            return;
        }

        // Runs on every random tick of every leaf, so bail out on the cheap check first
        final Spread spread = this.definition.spread;
        if (spread == null) {
            return;
        }

        // The decay check above may have just destroyed this leaf
        if (worldIn.getBlockState(pos).getBlock() != this) {
            return;
        }

        final Biome biome = worldIn.getBiome(pos);
        final DoubleRange chanceRoll = spread.getOrLoadChanceRangeForBiome(biome);
        if (chanceRoll == null) {
            return;
        }
        if (!(rand.nextDouble() <= (DoubleRanges.random(chanceRoll, rand) / 100))) {
            return;
        }

        for (final EnumFacing facing : EnumFacing.VALUES) {
            final BlockPos offset = pos.offset(facing);
            // Leaves on a chunk edge must not load the neighbouring chunk just to look for air
            if (!worldIn.isBlockLoaded(offset)) {
                continue;
            }
            final IBlockState offsetBlock = worldIn.getBlockState(offset);
            if (offsetBlock.getBlock() == Blocks.AIR) {
                final IBlockState spreadState = spread.getBlock().get();
                if (spreadState != null) {
                    worldIn.setBlockState(offset, spreadState);
                    worldIn.playEvent(2005, pos, 0);
                }
                break;
            }
        }
    }
}
