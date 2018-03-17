/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling;

import com.almuradev.content.type.block.BlockUpdateFlag;
import com.almuradev.content.type.block.StateMappedBlock;
import com.almuradev.content.type.block.type.sapling.state.SaplingBlockStateDefinition;
import com.almuradev.content.type.tree.TreeFeature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public final class SaplingBlockImpl extends AbstractSaplingBlock implements SaplingBlock, StateMappedBlock {
    private final SaplingBlockStateDefinition definition;

    SaplingBlockImpl(final SaplingBlockBuilder builder) {
        this.displayOnCreativeTab = null;
        builder.fill(this);
        this.definition = builder.singleState();
        this.definition.fill(this);
    }

    @Override
    public SaplingBlockStateDefinition definition(final IBlockState state) {
        return this.definition;
    }

    @Override
    public void generateTree(final World world, final BlockPos pos, final IBlockState state, final Random random) {
        if (!TerrainGen.saplingGrowTree(world, random, pos)) {
            return;
        }

        world.setBlockState(pos, Blocks.AIR.getDefaultState(), BlockUpdateFlag.PREVENT_RENDER);

        final TreeFeature feature = (TreeFeature) this.definition.tree.get();
        if (!feature.generate(world, random, pos)) {
            world.setBlockState(pos, state, BlockUpdateFlag.PREVENT_RENDER);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IStateMapper createStateMapper() {
        return new StateMap.Builder()
                .ignore(TYPE)
                .build();
    }
}
