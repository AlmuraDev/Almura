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
import com.almuradev.content.util.DoubleRanges;
import com.almuradev.content.util.MinimumIntWithVarianceFunctionPredicatePair;
import net.minecraft.block.BlockLeaves;
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

/**
 * A palm ({@code "palm": true}): a tall trunk leaning a step partway up, a crown of fronds radiating from its top that droop at their tips,
 * and the fruit (coconuts) clustered under the crown against the trunk. Every frond leaf is within four steps of the trunk through other
 * leaves, vanilla's reach for leaves not to decay.
 */
public final class PalmTreeFeature extends WorldGenAbstractTree implements AbstractTreeFeature, Tree {
    // The crown, relative to the top of the trunk: a tuft above it, fronds along the four sides drooping at the tips, short ones on the diagonals
    private static final int[][] CROWN = {
            {0, 1, 0},
            {1, 1, 0}, {-1, 1, 0}, {0, 1, 1}, {0, 1, -1},
            {1, 0, 0}, {2, 0, 0}, {3, 0, 0}, {3, -1, 0},
            {-1, 0, 0}, {-2, 0, 0}, {-3, 0, 0}, {-3, -1, 0},
            {0, 0, 1}, {0, 0, 2}, {0, 0, 3}, {0, -1, 3},
            {0, 0, -1}, {0, 0, -2}, {0, 0, -3}, {0, -1, -3},
            {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}
    };
    // Where coconuts may hang: just under the crown, around the trunk
    private static final int[][] FRUIT = {{1, -1, 0}, {-1, -1, 0}, {0, -1, 1}, {0, -1, -1}};

    private final List<MinimumIntWithVarianceFunctionPredicatePair<Biome>> height;
    private final LazyBlockState log;
    private final LazyBlockState leaves;
    @Nullable private final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> fruit;

    public PalmTreeFeature(final boolean notify, final List<MinimumIntWithVarianceFunctionPredicatePair<Biome>> height, final LazyBlockState log,
            final LazyBlockState leaves, @Nullable final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> fruit) {
        super(notify);
        this.height = height;
        this.log = log;
        this.leaves = leaves;
        this.fruit = fruit;
    }

    @Override
    public LazyBlockState logState() {
        return this.log;
    }

    @Override
    public LazyBlockState leavesState() {
        return this.leaves;
    }

    @Nullable
    @Override
    public LazyBlockState fruitState() {
        return this.fruit == null ? null : this.fruit.getKey();
    }

    @Override
    public boolean generate(final World world, final Random random, final BlockPos origin) {
        return this.generate(world, random, origin, Collections.emptyList());
    }

    @Override
    public boolean generate(final World world, final Random random, final BlockPos origin, final List<LazyBlockState> requires) {
        final Biome biome = world.getBiome(origin);
        final int height = AbstractTreeFeature.height(this.height, biome, random);
        if (origin.getY() < 1 || origin.getY() + height + 2 > world.getHeight()) {
            return false;
        }

        final BlockPos below = origin.down();
        final IBlockState soil = world.getBlockState(below);
        if (!AbstractTreeFeature.canSustainPlant(soil, world, below, EnumFacing.UP, (BlockSapling) Blocks.SAPLING, requires)) {
            return false;
        }

        // The trunk leans one step, in a random direction, a little over a third of the way up
        final EnumFacing lean = EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];
        final int leanAt = Math.max(2, height * 2 / 5);
        final BlockPos top = origin.up(height - 1).offset(lean);

        // Room for the trunk and the crown
        for (int y = 0; y < height; y++) {
            if (!this.replaceable(world, this.trunkAt(origin, lean, leanAt, y))) {
                return false;
            }
        }
        for (final int[] c : CROWN) {
            if (!this.replaceable(world, top.add(c[0], c[1], c[2]))) {
                return false;
            }
        }

        soil.getBlock().onPlantGrow(soil, world, below, origin);
        for (int y = 0; y < height; y++) {
            this.setBlockAndNotifyAdequately(world, this.trunkAt(origin, lean, leanAt, y), this.log.get());
        }
        final IBlockState leavesState = noDecayCheck(this.leaves.get());
        for (final int[] c : CROWN) {
            this.setIfClear(world, top.add(c[0], c[1], c[2]), leavesState);
        }
        if (this.fruit != null && !this.fruit.getValue().isEmpty()) {
            final double chance = DoubleRanges.random(AbstractTreeFeature.pickChance(biome, this.fruit.getValue()).range(), random) / 100d;
            final IBlockState fruitState = noDecayCheck(this.fruit.getKey().get());
            for (final int[] c : FRUIT) {
                if (random.nextDouble() <= chance) {
                    this.setIfClear(world, top.add(c[0], c[1], c[2]), fruitState);
                }
            }
        }
        return true;
    }

    /** The trunk's block at a height: straight up to where it leans, then one step over. */
    private BlockPos trunkAt(final BlockPos origin, final EnumFacing lean, final int leanAt, final int y) {
        final BlockPos pos = origin.up(y);
        return y >= leanAt ? pos.offset(lean) : pos;
    }

    private void setIfClear(final World world, final BlockPos pos, final IBlockState state) {
        final IBlockState at = world.getBlockState(pos);
        if (at.getBlock().isAir(at, world, pos) || at.getBlock().isLeaves(at, world, pos) || at.getMaterial() == Material.VINE) {
            this.setBlockAndNotifyAdequately(world, pos, state);
        }
    }

    private static IBlockState noDecayCheck(final IBlockState state) {
        return state.getPropertyKeys().contains(BlockLeaves.CHECK_DECAY) ? state.withProperty(BlockLeaves.CHECK_DECAY, false) : state;
    }

    private boolean replaceable(final World world, final BlockPos pos) {
        return pos.getY() >= 0 && pos.getY() < world.getHeight() && (!world.isChunkGeneratedAt(pos.getX(), pos.getZ()) || this.isReplaceable(world, pos));
    }
}
