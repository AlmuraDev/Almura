/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling;

import com.almuradev.almura.asm.mixin.accessors.block.BlockAccessor;
import com.almuradev.content.type.block.BlockUpdateFlag;
import com.almuradev.content.type.block.StateMappedBlock;
import com.almuradev.content.type.block.type.sapling.state.SaplingBlockStateDefinition;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public final class SaplingBlockImpl extends AbstractSaplingBlock implements SaplingBlock, StateMappedBlock {
    private final SaplingBlockStateDefinition definition;

    SaplingBlockImpl(final SaplingBlockBuilder builder) {
        ((BlockAccessor) (Object) this).accessor$setDisplayOnCreativeTab(null);
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

        final WorldGenAbstractTree feature = this.tree(world.getBiome(pos), random);
        if (!feature.generate(world, random, pos)) {
            world.setBlockState(pos, state, BlockUpdateFlag.PREVENT_RENDER);
        }
    }

    private WorldGenAbstractTree tree(final Biome biome, final Random random) {
        if(this.definition.bigTree != null && this.definition.bigTreeChances != null && random.nextDouble() <= (DoubleRangeFunctionPredicatePair.rangeOrRandom(this.definition.bigTreeChances, biome).random(random) / 100d)) {
            return (WorldGenAbstractTree) this.definition.bigTree.require();
        }
        return (WorldGenAbstractTree) this.definition.tree.require();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IStateMapper createStateMapper() {
        return new StateMap.Builder()
                .ignore(TYPE)
                .build();
    }
}
