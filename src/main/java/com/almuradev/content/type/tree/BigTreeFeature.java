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
import com.google.common.collect.Lists;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

public final class BigTreeFeature extends WorldGenAbstractTree implements AbstractTreeFeature, Tree {
    private final List<MinimumIntWithVarianceFunctionPredicatePair<Biome>> heights;
    private final LazyBlockState log;
    private final LazyBlockState leaves;
    @Nullable private final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> fruit;
    @Nullable private final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> hanging;
    private Random random;
    private World world;
    private Biome biome;
    private BlockPos origin = BlockPos.ORIGIN;
    private List<LazyBlockState> requires;
    private int height;
    private int trunkHeight;
    private double trunkHeightScale = 0.618d;
    private double branchSlope = 0.381d;
    private double widthScale = 1d;
    private double foliageDensity = 1d;
    private int trunkWidth = 1;
    private int foliageHeight = 4;
    private List<FoliagePos> foliageCoords;

    public BigTreeFeature(final boolean notify, final List<MinimumIntWithVarianceFunctionPredicatePair<Biome>> height, final LazyBlockState log, final LazyBlockState leaves, @Nullable final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> fruit, @Nullable final Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> hanging) {
        super(notify);
        this.heights = height;
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
        this.world = world;
        this.biome = world.getBiome(origin);
        this.origin = origin;
        this.requires = requires;
        this.random = new Random(random.nextLong());
        this.height = AbstractTreeFeature.height(this.heights, this.biome, this.random);

        if (!this.checkLocation()) {
            this.clean();
            return false;
        }

        this.prepare();
        this.makeFoliage();
        this.makeTrunk();
        this.makeBranches();
        this.clean();
        return true;
    }

    private void clean() {
        this.world = null;
        this.biome = null;
        this.requires = null;
    }

    private void prepare() {
        this.trunkHeight = (int) ((double) this.height * this.trunkHeightScale);

        if (this.trunkHeight >= this.height) {
            this.trunkHeight = this.height - 1;
        }

        int clustersPerY = (int) (1.382d + Math.pow(this.foliageDensity * (double) this.height / 13d, 2d));

        if (clustersPerY < 1) {
            clustersPerY = 1;
        }

        final int trunkTop = this.origin.getY() + this.trunkHeight;
        int relativeY = this.height - this.foliageHeight;
        this.foliageCoords = Lists.newArrayList();
        this.foliageCoords.add(new FoliagePos(this.origin.up(relativeY), trunkTop));

        while (relativeY >= 0) {
            final float shapeFac = this.treeShape(relativeY);
            if (shapeFac < 0f) {
                relativeY--;
                continue;
            }

            int num = 0;
            while (num < clustersPerY) {
                final double radius = this.widthScale * (double) shapeFac * ((double) this.random.nextFloat() + 0.328d);
                final double angle = (double) (this.random.nextFloat() * 2.0f) * Math.PI;
                final double x = radius * Math.sin(angle) + 0.5d;
                final double z = radius * Math.cos(angle) + 0.5d;
                final BlockPos checkStart = this.origin.add(x, (double) (relativeY - 1), z);
                final BlockPos checkEnd = checkStart.up(this.foliageHeight);

                if (this.checkLine(checkStart, checkEnd) == -1) {
                    final int xd = this.origin.getX() - checkStart.getX();
                    final int zd = this.origin.getZ() - checkStart.getZ();
                    final double branchHeight = (double) checkStart.getY() - Math.sqrt((double) (xd * xd + zd * zd)) * this.branchSlope;
                    final int y = branchHeight > (double) trunkTop ? trunkTop : (int) branchHeight;
                    final BlockPos checkBranchBase = new BlockPos(this.origin.getX(), y, this.origin.getZ());

                    if (this.checkLine(checkBranchBase, checkStart) == -1) {
                        this.foliageCoords.add(new FoliagePos(checkStart, checkBranchBase.getY()));
                    }
                }
                num++;
            }
            relativeY--;
        }
    }

    private void crossection(final BlockPos origin, final float radius, final IBlockState targetState) {
        final int rad = (int) ((double) radius + 0.618d);
        for (int x = -rad; x <= rad; x++) {
            for (int z = -rad; z <= rad; z++) {
                if (Math.pow((double) Math.abs(x) + 0.5d, 2d) + Math.pow((double) Math.abs(z) + 0.5d, 2d) <= (double) (radius * radius)) {
                    BlockPos pos = origin.add(x, 0, z);
                    final IBlockState state = this.world.getBlockState(pos);

                    if (state.getBlock().isAir(state, this.world, pos) || state.getBlock().isLeaves(state, this.world, pos)) {
                        this.setBlockAndNotifyAdequately(this.world, pos, targetState);

                        // Enforce hanging only hanging from a leaves or fruit
                        if (this.world.getBlockState(pos).equals(targetState) && AbstractTreeFeature.shouldPlaceHanging(this.hanging, this.biome, this.random)) {
                            pos = pos.down();

                            final IBlockState underLeafOrFruitState = this.world.getBlockState(pos);
                            if (underLeafOrFruitState.getBlock().isAir(underLeafOrFruitState, this.world, pos)) {
                                this.setBlockAndNotifyAdequately(this.world, pos, AbstractTreeFeature.hangingBlock(this.hanging));
                            }
                        }


                        this.setBlockAndNotifyAdequately(this.world, pos, targetState);
                    }
                }
            }
        }
    }

    private float treeShape(final int y) {
        if ((float) y < (float) this.height * 0.3f) {
            return -1.0f;
        }

        final float radius = (float) this.height / 2.0f;
        final float adjacent = radius - (float) y;
        float distance = MathHelper.sqrt(radius * radius - adjacent * adjacent);

        if (adjacent == 0.0f) {
            distance = radius;
        } else if (Math.abs(adjacent) >= radius) {
            return 0.0f;
        }

        return distance * 0.5f;
    }

    private float foliageShape(final int y) {
        if (y < 0 || y >= this.foliageHeight) {
            return -1f;
        } else if (y == 0 || y == this.foliageHeight - 1) {
            return 2f;
        } else {
            return 3f;
        }
    }

    private void foliageCluster(final BlockPos pos) {
        for (int i = 0; i < this.foliageHeight; i++) {
            this.crossection(pos.up(i), this.foliageShape(i), AbstractTreeFeature.leavesOrFruitBlock(this.leaves, this.fruit, this.biome, this.random));
        }
    }

    private void limb(final BlockPos start, final BlockPos end, final IBlockState state) {
        final BlockPos delta = end.add(-start.getX(), -start.getY(), -start.getZ());
        final int largestDistance = this.getLargestDistance(delta);
        final float xf = (float) delta.getX() / (float) largestDistance;
        final float yf = (float) delta.getY() / (float) largestDistance;
        final float zf = (float) delta.getZ() / (float) largestDistance;

        for (int i = 0; i <= largestDistance; i++) {
            final BlockPos pos = start.add((double) (0.5f + (float) i * xf), (double) (0.5f + (float) i * yf), (double) (0.5f + (float) i * zf));
            final BlockLog.EnumAxis axis = this.getLogAxis(start, pos);
            this.setBlockAndNotifyAdequately(this.world, pos, AbstractTreeFeature.log(state, axis));
        }
    }

    private int getLargestDistance(final BlockPos pos) {
        final int x = MathHelper.abs(pos.getX());
        final int y = MathHelper.abs(pos.getY());
        final int z = MathHelper.abs(pos.getZ());
        if (z > x && z > y) {
            return z;
        } else if (y > x) {
            return y;
        } else {
            return x;
        }
    }

    private BlockLog.EnumAxis getLogAxis(final BlockPos start, final BlockPos end) {
        BlockLog.EnumAxis axis = BlockLog.EnumAxis.Y;
        final int xDiff = Math.abs(end.getX() - start.getX());
        final int zDiff = Math.abs(end.getZ() - start.getZ());
        final int maxDiff = Math.max(xDiff, zDiff);

        if (maxDiff > 0) {
            if (xDiff == maxDiff) {
                axis = BlockLog.EnumAxis.X;
            } else if (zDiff == maxDiff) {
                axis = BlockLog.EnumAxis.Z;
            }
        }

        return axis;
    }

    private void makeFoliage() {
        for (final FoliagePos pos : this.foliageCoords) {
            this.foliageCluster(pos);
        }
    }

    private boolean trimBranches(final int y) {
        return (double) y >= (double) this.height * 0.2D;
    }

    private void makeTrunk() {
        final BlockPos start = this.origin;
        final BlockPos end = this.origin.up(this.trunkHeight);
        final IBlockState state = this.log.get();
        this.limb(start, end, state);

        if (this.trunkWidth == 2) {
            this.limb(start.east(), end.east(), state);
            this.limb(start.east().south(), end.east().south(), state);
            this.limb(start.south(), end.south(), state);
        }
    }

    private void makeBranches() {
        for (final FoliagePos fPos : this.foliageCoords) {
            final int yBase = fPos.getBranchBase();

            final BlockPos bPos = new BlockPos(this.origin.getX(), yBase, this.origin.getZ());
            if (!bPos.equals(fPos) && this.trimBranches(yBase - this.origin.getY())) {
                this.limb(bPos, fPos, this.log.get());
            }
        }
    }

    private int checkLine(final BlockPos start, final BlockPos end) {
        final BlockPos delta = end.add(-start.getX(), -start.getY(), -start.getZ());
        final int largestDistance = this.getLargestDistance(delta);
        final float xf = (float) delta.getX() / (float) largestDistance;
        final float yf = (float) delta.getY() / (float) largestDistance;
        final float zf = (float) delta.getZ() / (float) largestDistance;

        if (largestDistance == 0) {
            return -1;
        }

        for (int i = 0; i <= largestDistance; i++) {
            final BlockPos pos = start.add((double) (0.5f + (float) i * xf), (double) (0.5f + (float) i * yf), (double) (0.5f + (float) i * zf));
            if (!this.isReplaceable(this.world, pos)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void setDecorationDefaults() {
        this.foliageHeight = 5;
    }

    private boolean checkLocation() {
        final BlockPos belowOrigin = this.origin.down();
        final IBlockState state = this.world.getBlockState(belowOrigin);

        final boolean isSoil = AbstractTreeFeature.canSustainPlant(state, this.world, belowOrigin, EnumFacing.UP, (BlockSapling) Blocks.SAPLING, this.requires);
        if (!isSoil) {
            return false;
        }

        final int allowedHeight = this.checkLine(this.origin, this.origin.up(this.height - 1));
        if (allowedHeight == -1) {
            return true;
        } else if (allowedHeight < 6) {
            return false;
        } else {
            this.height = allowedHeight;
            return true;
        }
    }

    static class FoliagePos extends BlockPos {
        private final int branchBase;

        FoliagePos(final BlockPos pos, final int branchBase) {
            super(pos.getX(), pos.getY(), pos.getZ());
            this.branchBase = branchBase;
        }

        int getBranchBase() {
            return this.branchBase;
        }
    }
}
