/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.almuradev.almura.content.def.ContentDefs.BiomeRange;
import com.almuradev.almura.content.def.ContentDefs.Range;
import com.almuradev.almura.content.def.ContentDefs.TreeDef;
import com.almuradev.almura.content.def.ContentDefs.TreeHeightDef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The content's trees ({@code content/tree}), grown from their saplings as 3.5's {@code TreeFeature}, {@code BigTreeFeature} and
 * {@code PalmTreeFeature} grew them. Leaves are placed already counted as near a log (1.12's {@code CHECK_DECAY} false) and then told
 * to work out their distance, so a new tree never starts decaying.
 */
public final class ContentTrees {

    private static final Map<String, TreeDef> TREES = new HashMap<>();

    private ContentTrees() {
    }

    static void register(final List<TreeDef> trees) {
        trees.forEach(tree -> TREES.put(tree.id(), tree));
    }

    public static @Nullable TreeDef tree(final String id) {
        return TREES.get(id);
    }

    /**
     * Grows the tree at the position (the sapling already gone).
     *
     * @return Whether it grew; when not, nothing was changed
     */
    public static boolean grow(final ServerLevel level, final BlockPos origin, final TreeDef tree, final RandomSource random) {
        return grow(level, origin, tree, random, List.of(), Block.UPDATE_ALL);
    }

    /**
     * Grows the tree at the position: from a sapling (every block updating its neighbours, as 1.12's saplings did) or in world generation
     * (only telling clients, as 1.12's world generation did).
     *
     * @param requires the blocks the tree must stand on; empty for a sapling's soil
     */
    public static boolean grow(final WorldGenLevel level, final BlockPos origin, final TreeDef tree, final RandomSource random,
        final List<String> requires, final int flags) {
        final Grower grower = new Grower(level, tree, biome(level, origin), requires, flags);
        final boolean grew = switch (tree.shape()) {
            case "big" -> new BigTree(grower, origin, random).generate();
            case "palm" -> grower.palm(origin, random);
            default -> grower.normal(origin, random);
        };
        if (grew) {
            // Each leaf works out how far it is from a log
            grower.placedLeaves.forEach(pos -> level.scheduleTick(pos, level.getBlockState(pos).getBlock(), 1));
        }
        return grew;
    }

    static String biome(final LevelReader level, final BlockPos pos) {
        return level.getBiome(pos).unwrapKey().map(key -> key.identifier().toString()).orElse("minecraft:plains");
    }

    /**
     * The trunk height for the biome: a height naming the biome (or its namespace), else the default one, else any; its minimum plus a
     * variance drawn from its range.
     */
    static int height(final TreeDef tree, final String biome, final RandomSource random) {
        TreeHeightDef chosen = null;
        for (final TreeHeightDef height : tree.heights()) {
            if (!height.biomes().isEmpty() && BiomeRange.matches(height.biomes(), biome)) {
                chosen = height;
                break;
            }
            if (height.biomes().isEmpty() && chosen == null) {
                chosen = height;
            }
        }
        if (chosen == null) {
            chosen = tree.heights().get(random.nextInt(tree.heights().size()));
        }
        return chosen.min() + chosen.varianceMin() + random.nextInt(Math.max(1, chosen.varianceMax() - chosen.varianceMin() + 1));
    }

    /**
     * Places a tree's blocks and knows what 1.12's trees could grow into.
     */
    private static final class Grower {

        final WorldGenLevel level;
        final TreeDef tree;
        final String biome;
        final List<String> requires;
        final int flags;
        final List<BlockPos> placedLeaves = new ArrayList<>();

        Grower(final WorldGenLevel level, final TreeDef tree, final String biome, final List<String> requires, final int flags) {
            this.level = level;
            this.tree = tree;
            this.biome = biome;
            this.requires = requires;
            this.flags = flags;
        }

        static BlockState state(final String id) {
            return BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).orElse(Blocks.AIR).defaultBlockState();
        }

        BlockState log(final Direction.Axis axis) {
            final BlockState log = state(this.tree.log());
            return log.hasProperty(RotatedPillarBlock.AXIS) ? log.setValue(RotatedPillarBlock.AXIS, axis) : log;
        }

        BlockState leaves() {
            return state(this.tree.leaves());
        }

        /**
         * The leaves, or at the tree's fruit chance its fruit leaves.
         */
        BlockState leavesOrFruit(final RandomSource random) {
            if (this.tree.fruit() != null && !this.tree.fruitChance().isEmpty()) {
                final Range chance = BiomeRange.pick(this.tree.fruitChance(), this.biome);
                if (chance != null && random.nextDouble() <= chance.random(random.nextDouble()) / 100) {
                    return state(this.tree.fruit());
                }
            }
            return this.leaves();
        }

        void set(final BlockPos pos, final BlockState state) {
            BlockState placed = state;
            if (placed.getBlock() instanceof LeavesBlock) {
                placed = placed.setValue(LeavesBlock.DISTANCE, 1);
                this.placedLeaves.add(pos.immutable());
            }
            this.level.setBlock(pos, placed, this.flags);
        }

        /**
         * 1.12's {@code WorldGenAbstractTree.isReplaceable}: air, leaves, logs and what a tree grows through (grass, dirt, saplings, vines,
         * plants).
         */
        boolean replaceable(final BlockPos pos) {
            if (this.level.isOutsideBuildHeight(pos)) {
                return false;
            }
            final BlockState state = this.level.getBlockState(pos);
            return state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(Blocks.GRASS_BLOCK) || state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAPLINGS) || state.is(Blocks.VINE) || state.is(BlockTags.REPLACEABLE_BY_TREES);
        }

        /**
         * Where leaves may go: air, leaves, or a plant (1.12's vine material).
         */
        boolean clearForLeaves(final BlockPos pos) {
            final BlockState state = this.level.getBlockState(pos);
            return state.isAir() || state.is(BlockTags.LEAVES) || state.is(Blocks.VINE) || state.is(BlockTags.REPLACEABLE_BY_TREES);
        }

        /**
         * A sapling's soil, and 1.12's {@code onPlantGrow}: grass or farmland under the trunk becomes dirt.
         */
        boolean soil(final BlockPos origin) {
            final BlockState soil = this.level.getBlockState(origin.below());
            if (!this.requires.isEmpty()) {
                return this.requires.contains(BuiltInRegistries.BLOCK.getKey(soil.getBlock()).toString());
            }
            return soil.is(BlockTags.SUPPORTS_VEGETATION);
        }

        void plantGrow(final BlockPos origin) {
            final BlockPos below = origin.below();
            final BlockState soil = this.level.getBlockState(below);
            if (soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.FARMLAND) || soil.is(Blocks.MYCELIUM)) {
                this.level.setBlock(below, Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }

        int bottom() {
            return this.level.getMinY();
        }

        int top() {
            return this.level.getMaxY() + 1;
        }

        /**
         * 3.5's {@code TreeFeature}, 1.12's small oak: a trunk with a four-layer crown, its corners trimmed at random.
         */
        boolean normal(final BlockPos origin, final RandomSource random) {
            final int height = ContentTrees.height(this.tree, this.biome, random);
            if (origin.getY() < this.bottom() + 1 || origin.getY() + height + 1 > this.top()) {
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
                for (int x = origin.getX() - k; x <= origin.getX() + k; x++) {
                    for (int z = origin.getZ() - k; z <= origin.getZ() + k; z++) {
                        if (!this.replaceable(new BlockPos(x, y, z))) {
                            return false;
                        }
                    }
                }
            }
            if (!this.soil(origin) || origin.getY() >= this.top() - height - 1) {
                return false;
            }
            this.plantGrow(origin);
            for (int y = origin.getY() - 3 + height; y <= origin.getY() + height; y++) {
                final int yd = y - (origin.getY() + height);
                final int os = 1 - yd / 2;
                for (int x = origin.getX() - os; x <= origin.getX() + os; x++) {
                    final int ox = x - origin.getX();
                    for (int z = origin.getZ() - os; z <= origin.getZ() + os; z++) {
                        final int oz = z - origin.getZ();
                        if (Math.abs(ox) != os || Math.abs(oz) != os || random.nextInt(2) != 0 && yd != 0) {
                            final BlockPos pos = new BlockPos(x, y, z);
                            if (this.clearForLeaves(pos)) {
                                this.set(pos, this.leavesOrFruit(random));
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < height; i++) {
                final BlockPos pos = origin.above(i);
                if (this.clearForLeaves(pos)) {
                    this.set(pos, this.log(Direction.Axis.Y));
                }
            }
            return true;
        }

        // The palm's crown, relative to the top of the trunk: a tuft above it, fronds along the four sides drooping at the tips, short
        // ones on the diagonals
        private static final int[][] PALM_CROWN = {
            {0, 1, 0},
            {1, 1, 0}, {-1, 1, 0}, {0, 1, 1}, {0, 1, -1},
            {1, 0, 0}, {2, 0, 0}, {3, 0, 0}, {3, -1, 0},
            {-1, 0, 0}, {-2, 0, 0}, {-3, 0, 0}, {-3, -1, 0},
            {0, 0, 1}, {0, 0, 2}, {0, 0, 3}, {0, -1, 3},
            {0, 0, -1}, {0, 0, -2}, {0, 0, -3}, {0, -1, -3},
            {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}
        };
        // Where coconuts may hang: just under the crown, around the trunk
        private static final int[][] PALM_FRUIT = {{1, -1, 0}, {-1, -1, 0}, {0, -1, 1}, {0, -1, -1}};

        /**
         * 3.5's {@code PalmTreeFeature}: a tall trunk leaning a step partway up, a crown of drooping fronds, and the fruit clustered under
         * the crown against the trunk.
         */
        boolean palm(final BlockPos origin, final RandomSource random) {
            final int height = ContentTrees.height(this.tree, this.biome, random);
            if (origin.getY() < this.bottom() + 1 || origin.getY() + height + 2 > this.top()) {
                return false;
            }
            if (!this.soil(origin)) {
                return false;
            }
            // The trunk leans one step, in a random direction, a little over a third of the way up
            final Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            final int leanAt = Math.max(2, height * 2 / 5);
            final BlockPos top = origin.above(height - 1).relative(lean);
            for (int y = 0; y < height; y++) {
                if (!this.replaceable(trunkAt(origin, lean, leanAt, y))) {
                    return false;
                }
            }
            for (final int[] c : PALM_CROWN) {
                if (!this.replaceable(top.offset(c[0], c[1], c[2]))) {
                    return false;
                }
            }
            this.plantGrow(origin);
            for (int y = 0; y < height; y++) {
                this.set(trunkAt(origin, lean, leanAt, y), this.log(Direction.Axis.Y));
            }
            final BlockState leaves = this.leaves();
            for (final int[] c : PALM_CROWN) {
                this.setIfClear(top.offset(c[0], c[1], c[2]), leaves);
            }
            if (this.tree.fruit() != null && !this.tree.fruitChance().isEmpty()) {
                final Range chanceRange = BiomeRange.pick(this.tree.fruitChance(), this.biome);
                final double chance = chanceRange == null ? 0 : chanceRange.random(random.nextDouble()) / 100;
                final BlockState fruit = state(this.tree.fruit());
                for (final int[] c : PALM_FRUIT) {
                    if (random.nextDouble() <= chance) {
                        this.setIfClear(top.offset(c[0], c[1], c[2]), fruit);
                    }
                }
            }
            return true;
        }

        private static BlockPos trunkAt(final BlockPos origin, final Direction lean, final int leanAt, final int y) {
            final BlockPos pos = origin.above(y);
            return y >= leanAt ? pos.relative(lean) : pos;
        }

        private void setIfClear(final BlockPos pos, final BlockState state) {
            if (this.clearForLeaves(pos)) {
                this.set(pos, state);
            }
        }
    }

    /**
     * 3.5's {@code BigTreeFeature}, 1.12's big oak ({@code WorldGenBigTree}): a trunk with branches out to clusters of foliage.
     */
    private static final class BigTree {

        private final Grower grower;
        private final BlockPos origin;
        private final RandomSource random;
        private int height;
        private int trunkHeight;
        private final double trunkHeightScale = 0.618d;
        private final double branchSlope = 0.381d;
        private final double widthScale = 1d;
        private final double foliageDensity = 1d;
        private final int foliageHeight = 4;
        private final List<FoliagePos> foliageCoords = new ArrayList<>();

        private record FoliagePos(BlockPos pos, int branchBase) {
        }

        BigTree(final Grower grower, final BlockPos origin, final RandomSource random) {
            this.grower = grower;
            this.origin = origin;
            this.random = RandomSource.create(random.nextLong());
            this.height = ContentTrees.height(grower.tree, grower.biome, this.random);
        }

        boolean generate() {
            if (!this.checkLocation()) {
                return false;
            }
            // 3.5's big tree left the soil as it was
            this.prepare();
            this.makeFoliage();
            this.makeTrunk();
            this.makeBranches();
            return true;
        }

        private void prepare() {
            this.trunkHeight = (int) (this.height * this.trunkHeightScale);
            if (this.trunkHeight >= this.height) {
                this.trunkHeight = this.height - 1;
            }
            int clustersPerY = (int) (1.382d + Math.pow(this.foliageDensity * this.height / 13d, 2d));
            if (clustersPerY < 1) {
                clustersPerY = 1;
            }
            final int trunkTop = this.origin.getY() + this.trunkHeight;
            int relativeY = this.height - this.foliageHeight;
            this.foliageCoords.add(new FoliagePos(this.origin.above(relativeY), trunkTop));
            while (relativeY >= 0) {
                final float shapeFac = this.treeShape(relativeY);
                if (shapeFac < 0f) {
                    relativeY--;
                    continue;
                }
                for (int num = 0; num < clustersPerY; num++) {
                    final double radius = this.widthScale * shapeFac * (this.random.nextFloat() + 0.328d);
                    final double angle = this.random.nextFloat() * 2.0f * Math.PI;
                    final double x = radius * Math.sin(angle) + 0.5d;
                    final double z = radius * Math.cos(angle) + 0.5d;
                    final BlockPos checkStart = BlockPos.containing(this.origin.getX() + x, this.origin.getY() + relativeY - 1, this.origin.getZ() + z);
                    final BlockPos checkEnd = checkStart.above(this.foliageHeight);
                    if (this.checkLine(checkStart, checkEnd) == -1) {
                        final int xd = this.origin.getX() - checkStart.getX();
                        final int zd = this.origin.getZ() - checkStart.getZ();
                        final double branchHeight = checkStart.getY() - Math.sqrt(xd * xd + zd * zd) * this.branchSlope;
                        final int y = branchHeight > trunkTop ? trunkTop : (int) branchHeight;
                        final BlockPos checkBranchBase = new BlockPos(this.origin.getX(), y, this.origin.getZ());
                        if (this.checkLine(checkBranchBase, checkStart) == -1) {
                            this.foliageCoords.add(new FoliagePos(checkStart, checkBranchBase.getY()));
                        }
                    }
                }
                relativeY--;
            }
        }

        private void crossection(final BlockPos center, final float radius, final BlockState state) {
            final int rad = (int) (radius + 0.618d);
            for (int x = -rad; x <= rad; x++) {
                for (int z = -rad; z <= rad; z++) {
                    if (Math.pow(Math.abs(x) + 0.5d, 2d) + Math.pow(Math.abs(z) + 0.5d, 2d) <= radius * radius) {
                        final BlockPos pos = center.offset(x, 0, z);
                        final BlockState at = this.grower.level.getBlockState(pos);
                        if (at.isAir() || at.is(BlockTags.LEAVES)) {
                            this.grower.set(pos, state);
                        }
                    }
                }
            }
        }

        private float treeShape(final int y) {
            if (y < this.height * 0.3f) {
                return -1.0f;
            }
            final float radius = this.height / 2.0f;
            final float adjacent = radius - y;
            float distance = Mth.sqrt(radius * radius - adjacent * adjacent);
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
            }
            return y == 0 || y == this.foliageHeight - 1 ? 2f : 3f;
        }

        private void foliageCluster(final BlockPos pos) {
            for (int i = 0; i < this.foliageHeight; i++) {
                this.crossection(pos.above(i), this.foliageShape(i), this.grower.leavesOrFruit(this.random));
            }
        }

        private void limb(final BlockPos start, final BlockPos end) {
            final BlockPos delta = end.subtract(start);
            final int largestDistance = largestDistance(delta);
            final float xf = (float) delta.getX() / largestDistance;
            final float yf = (float) delta.getY() / largestDistance;
            final float zf = (float) delta.getZ() / largestDistance;
            for (int i = 0; i <= largestDistance; i++) {
                final BlockPos pos = BlockPos.containing(start.getX() + 0.5f + i * xf, start.getY() + 0.5f + i * yf, start.getZ() + 0.5f + i * zf);
                this.grower.set(pos, this.grower.log(logAxis(start, pos)));
            }
        }

        private static int largestDistance(final BlockPos pos) {
            final int x = Math.abs(pos.getX()), y = Math.abs(pos.getY()), z = Math.abs(pos.getZ());
            if (z > x && z > y) {
                return z;
            }
            return y > x ? y : x;
        }

        private static Direction.Axis logAxis(final BlockPos start, final BlockPos end) {
            final int xDiff = Math.abs(end.getX() - start.getX());
            final int zDiff = Math.abs(end.getZ() - start.getZ());
            final int maxDiff = Math.max(xDiff, zDiff);
            if (maxDiff > 0) {
                if (xDiff == maxDiff) {
                    return Direction.Axis.X;
                }
                if (zDiff == maxDiff) {
                    return Direction.Axis.Z;
                }
            }
            return Direction.Axis.Y;
        }

        private void makeFoliage() {
            for (final FoliagePos pos : this.foliageCoords) {
                this.foliageCluster(pos.pos());
            }
        }

        private void makeTrunk() {
            this.limb(this.origin, this.origin.above(this.trunkHeight));
        }

        private void makeBranches() {
            for (final FoliagePos foliage : this.foliageCoords) {
                final int yBase = foliage.branchBase();
                final BlockPos base = new BlockPos(this.origin.getX(), yBase, this.origin.getZ());
                if (!base.equals(foliage.pos()) && yBase - this.origin.getY() >= this.height * 0.2d) {
                    this.limb(base, foliage.pos());
                }
            }
        }

        private int checkLine(final BlockPos start, final BlockPos end) {
            final BlockPos delta = end.subtract(start);
            final int largestDistance = largestDistance(delta);
            if (largestDistance == 0) {
                return -1;
            }
            final float xf = (float) delta.getX() / largestDistance;
            final float yf = (float) delta.getY() / largestDistance;
            final float zf = (float) delta.getZ() / largestDistance;
            for (int i = 0; i <= largestDistance; i++) {
                final BlockPos pos = BlockPos.containing(start.getX() + 0.5f + i * xf, start.getY() + 0.5f + i * yf, start.getZ() + 0.5f + i * zf);
                if (!this.grower.replaceable(pos)) {
                    return i;
                }
            }
            return -1;
        }

        private boolean checkLocation() {
            if (!this.grower.soil(this.origin)) {
                return false;
            }
            final int allowedHeight = this.checkLine(this.origin, this.origin.above(this.height - 1));
            if (allowedHeight == -1) {
                return true;
            }
            if (allowedHeight < 6) {
                return false;
            }
            this.height = allowedHeight;
            return true;
        }
    }
}
