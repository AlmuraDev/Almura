/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.tree;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import com.almuradev.content.util.MinimumIntWithVarianceFunctionPredicatePair;
import net.kyori.lunar.collection.MoreIterables;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.IPlantable;

import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

public interface AbstractTreeFeature {
    boolean generate(final World world, final Random random, final BlockPos origin, final List<LazyBlockState> requires);

    static int height(final List<MinimumIntWithVarianceFunctionPredicatePair<Biome>> heights, final Biome biome, final Random random) {
        for (final MinimumIntWithVarianceFunctionPredicatePair<Biome> height : heights) {
            if (height.test(biome)) {
                return height.get(random);
            }
        }
        return MoreIterables.random(heights).get(random);
    }

    static IBlockState log(IBlockState state, final BlockLog.EnumAxis axis) {
        if (state.getPropertyKeys().contains(BlockLog.LOG_AXIS)) {
            state = state.withProperty(BlockLog.LOG_AXIS, axis);
        }
        return state;
    }

    static IBlockState leavesOrFruitBlock(final LazyBlockState leaves, @Nullable final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> fruit, final Biome biome, final Random random) {
        IBlockState state;

        if (fruit != null && !fruit.getValue().isEmpty() && random.nextDouble() <= (pickChance(biome, fruit.getValue()).range().random(random) / 100d)) {
            // use fruit leaves block
            state = fruit.getKey().get();
        } else {
            state = leaves.get();
        }

        if (state.getPropertyKeys().contains(BlockLeaves.CHECK_DECAY)) {
            state = state.withProperty(BlockLeaves.CHECK_DECAY, false);
        }

        return state;
    }

    static boolean shouldPlaceHanging(@Nullable final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> hanging, final Biome biome, final Random random) {
        return hanging != null && !hanging.getValue().isEmpty() && random.nextDouble() <= (pickChance(biome, hanging.getValue()).range().random(random) / 100d);
    }

    static IBlockState hangingBlock(@Nullable final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> hanging) {
        IBlockState state = Blocks.AIR.getDefaultState();
        if (hanging != null) {
            state = hanging.getKey().get();

            if (state.getPropertyKeys().contains(BlockLeaves.CHECK_DECAY)) {
                state = state.withProperty(BlockLeaves.CHECK_DECAY, false);
            }
        }

        return state;
    }

    static DoubleRangeFunctionPredicatePair<Biome> pickChance(final Biome biome, final List<DoubleRangeFunctionPredicatePair<Biome>> chances) {
        for (final DoubleRangeFunctionPredicatePair<Biome> chance : chances) {
            if (chance.test(biome)) {
                return chance;
            }
        }
        return MoreIterables.random(chances);
    }

    static boolean canSustainPlant(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing direction, final IPlantable plantable, final List<LazyBlockState> requires) {
        if (!requires.isEmpty()) {
            for (final LazyBlockState lbs : requires) {
                if (lbs.partialTest(state)) {
                    return true;
                }
            }
            return false;
        }
        return state.getBlock().canSustainPlant(state, world, pos, direction, plantable);
    }
}
