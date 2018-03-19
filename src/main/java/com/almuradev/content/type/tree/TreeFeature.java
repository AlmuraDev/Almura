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
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

public final class TreeFeature extends WorldGenAbstractTree implements AbstractTreeFeature, Tree {
    private final List<MinimumIntWithVarianceFunctionPredicatePair<Biome>> height;
    private final LazyBlockState log;
    private final LazyBlockState leaves;
    @Nullable private final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> fruit;
    @Nullable private final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> hanging;

    public TreeFeature(final boolean notify, final List<MinimumIntWithVarianceFunctionPredicatePair<Biome>> height, final LazyBlockState log, final LazyBlockState leaves, @Nullable final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> fruit, @Nullable final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> hanging) {
        super(notify);
        this.height = height;
        this.log = log;
        this.leaves = leaves;
        this.fruit = fruit;
        this.hanging = hanging;
    }

    @Override
    public boolean generate(final World world, final Random random, final BlockPos origin) {
        return this.generate(world, random, origin, Collections.emptyList());
    }

    @Override
    public boolean generate(final World world, final Random random, final BlockPos origin, final List<LazyBlockState> requires) {
        final Biome biome = world.getBiome(origin);
        final int height = AbstractTreeFeature.height(this.height, biome, random);
        boolean canPlace = true;

        if (origin.getY() < 1 || origin.getY() + height + 1 > world.getHeight()) {
            return false;
        }

        for (int y = origin.getY(); y <= origin.getY() + 1 + height; y++) {
            int k = 1;

            if (y == origin.getY()) {
                k = 0;
            }

            if (y >= origin.getY() + 1 + height - 2) {
                k = 2;
            }

            final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            for (int x = origin.getX() - k; x <= origin.getX() + k && canPlace; x++) {
                for (int z = origin.getZ() - k; z <= origin.getZ() + k && canPlace; z++) {
                    if (y >= 0 && y < world.getHeight()) {
                        if (!this.replaceable(world, pos.setPos(x, y, z))) {
                            canPlace = false;
                        }
                    } else {
                        canPlace = false;
                    }
                }
            }
        }

        if (!canPlace) {
            return false;
        }

        final BlockPos belowOrigin = origin.down();

        IBlockState state = world.getBlockState(belowOrigin);

        if (AbstractTreeFeature.canSustainPlant(state, world, belowOrigin, EnumFacing.UP, (BlockSapling) Blocks.SAPLING, requires) && origin.getY() < world.getHeight() - height - 1) {
            state.getBlock().onPlantGrow(state, world, belowOrigin, origin);

            for (int y = origin.getY() - 3 + height; y <= origin.getY() + height; y++) {
                final int yd = y - (origin.getY() + height);
                final int os = 1 - yd / 2;

                for (int x = origin.getX() - os; x <= origin.getX() + os; x++) {
                    final int ox = x - origin.getX();

                    for (int z = origin.getZ() - os; z <= origin.getZ() + os; z++) {
                        final int oz = z - origin.getZ();

                        if (Math.abs(ox) != os || Math.abs(oz) != os || random.nextInt(2) != 0 && yd != 0) {
                            final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
                            state = world.getBlockState(pos);

                            if (state.getBlock().isAir(state, world, pos) || state.getBlock().isLeaves(state, world, pos) || state.getMaterial() == Material.VINE) {
                                final IBlockState leavesOrFruitState = AbstractTreeFeature.leavesOrFruitBlock(this.leaves, this.fruit, biome, random);
                                this.setBlockAndNotifyAdequately(world, pos, leavesOrFruitState);
                                state = world.getBlockState(pos);

                                // Enforce hanging only hanging from a leaves or fruit
                                if (state.equals(leavesOrFruitState) && AbstractTreeFeature.shouldPlaceHanging(this.hanging, biome, random)) {
                                    pos.setPos(x, y - 1, z);

                                    final IBlockState underLeafOrFruitState = world.getBlockState(pos);
                                    if (underLeafOrFruitState.getBlock().isAir(underLeafOrFruitState, world, pos)) {
                                        this.setBlockAndNotifyAdequately(world, pos, AbstractTreeFeature.hangingBlock(this.hanging));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            final BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos(origin);
            for (int i = 0; i < height; i++) {
                final BlockPos pos = mutPos.setPos(mutPos.getX(), origin.getY() + i, mutPos.getZ());
                state = world.getBlockState(pos);

                if (state.getBlock().isAir(state, world, pos) || state.getBlock().isLeaves(state, world, pos) || AbstractTreeFeature.hangingBlock(this.hanging).equals(state) || state.getMaterial() == Material.VINE) {
                    this.setBlockAndNotifyAdequately(world, pos, this.log.get());
                }
            }

            return true;
        }

        return false;
    }

    private boolean replaceable(final World world, final BlockPos pos) {
        return !world.isChunkGeneratedAt(pos.getX(), pos.getZ()) || this.isReplaceable(world, pos);
    }
}
