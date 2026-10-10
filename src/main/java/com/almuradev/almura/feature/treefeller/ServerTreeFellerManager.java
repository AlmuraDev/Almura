/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.treefeller;

import com.almuradev.almura.asm.mixin.accessors.block.BlockLeavesAccessor;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.tree.Tree;
import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Fells fruit trees: when a player breaks the log of a tree that bears fruit, every connected log at or above the cut is broken and every
 * leaf / fruit leaf that no longer has a log within reach drops its items where it was and disappears.
 *
 * <p>Fruit trees are the {@link Tree}s that configure a fruit block. Only natural (decayable) leaves count, so player placed leaves are
 * never touched, and a cluster of fruit logs that isn't touching natural fruit tree leaves (a build) is left alone.</p>
 */
public final class ServerTreeFellerManager extends Witness.Impl {

    // Vanilla leaf support distance: a leaf stays while a log is within this many leaf steps
    private static final int LEAF_SUPPORT_RANGE = 4;
    // How far (in leaf steps) from the felled logs leaves are collected. Fruit leaves sit next to leaves, so one more than support range.
    private static final int LEAF_SEARCH_RANGE = LEAF_SUPPORT_RANGE + 2;
    // Horizontal distance from the cut that logs are followed, so a felled tree can't chain into a forest
    private static final int LOG_SEARCH_RADIUS = 10;
    private static final int MAX_LOGS = 256;
    private static final int MAX_LEAVES = 2048;
    // Blocks removed per tick, so large trees never cause a lag spike
    private static final int BLOCKS_PER_TICK = 64;

    private final PluginContainer container;
    private final Logger logger;
    private final ClaimGuard claims;
    // Log block -> leaf and fruit leaf blocks of every fruit tree using that log (several trees can share a log, e.g. citrus)
    @Nullable private Map<Block, Set<Block>> fruitLeavesByLog;

    @Inject
    private ServerTreeFellerManager(final PluginContainer container, final Logger logger) {
        this.container = container;
        this.logger = logger;
        this.claims = Sponge.getPluginManager().isLoaded("griefdefender") ? new GriefDefenderClaimGuard() : ClaimGuard.NONE;
    }

    @Listener(order = Order.POST)
    public void onBlockBreak(final ChangeBlockEvent.Break event, @Root final Player player) {
        for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (!transaction.isValid()) {
                continue;
            }

            final Block block = ((IBlockState) transaction.getOriginal().getState()).getBlock();
            final Set<Block> leafBlocks = this.fruitLeavesByLog().get(block);
            if (leafBlocks == null) {
                continue;
            }

            final Location<org.spongepowered.api.world.World> location = transaction.getOriginal().getLocation().orElse(null);
            if (location == null) {
                continue;
            }

            final World world = (World) location.getExtent();
            final BlockPos origin = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            // Run next tick, once the player's break has been applied to the world
            Task.builder()
                    .delayTicks(1)
                    .execute(() -> this.fell(world, origin, block, leafBlocks))
                    .submit(this.container);
        }
    }

    private Map<Block, Set<Block>> fruitLeavesByLog() {
        if (this.fruitLeavesByLog == null) {
            final Map<Block, Set<Block>> map = new HashMap<>();
            for (final Tree tree : Sponge.getRegistry().getAllOf(Tree.class)) {
                final LazyBlockState fruit = tree.fruitState();
                if (fruit == null) {
                    continue;
                }

                try {
                    final Set<Block> leafBlocks = map.computeIfAbsent(tree.logState().block(), k -> new HashSet<>());
                    leafBlocks.add(tree.leavesState().block());
                    leafBlocks.add(fruit.block());
                } catch (final RuntimeException e) {
                    this.logger.warn("Unable to resolve the blocks of fruit tree '{}', it will not be felled.", tree.getId(), e);
                }
            }
            this.fruitLeavesByLog = map;
        }
        return this.fruitLeavesByLog;
    }

    private void fell(final World world, final BlockPos origin, final Block log, final Set<Block> leafBlocks) {
        if (!world.isBlockLoaded(origin)) {
            return;
        }

        final Object claim = this.claims.claimAt(world, origin);

        final Set<BlockPos> logs = this.findLogs(world, origin, log, claim);

        // The cut log is gone too, so leaves are collected from it as well as from the logs being felled
        final Set<BlockPos> felled = new HashSet<>(logs);
        felled.add(origin);
        final Set<BlockPos> leaves = this.findLeaves(world, felled, leafBlocks, claim);

        // Not touching any natural fruit tree leaves: this is a build made of logs, not a tree
        if (leaves.isEmpty()) {
            return;
        }

        // Leaves still held up by another log (a neighbouring tree, a log below the cut) stay, the same rule as decay
        leaves.removeAll(this.findSupported(world, leaves, felled));

        final List<BlockPos> toRemove = new ArrayList<>(logs.size() + leaves.size());
        toRemove.addAll(logs);
        toRemove.addAll(leaves);
        if (toRemove.isEmpty()) {
            return;
        }

        final Iterator<BlockPos> iterator = toRemove.iterator();
        Task.builder()
                .intervalTicks(1)
                .execute(task -> {
                    for (int i = 0; i < BLOCKS_PER_TICK && iterator.hasNext(); i++) {
                        this.remove(world, iterator.next(), log, leafBlocks);
                    }
                    if (!iterator.hasNext()) {
                        task.cancel();
                    }
                })
                .submit(this.container);
    }

    /**
     * Logs of the same type connected (including diagonally, for branches) to the cut, at or above it.
     */
    private Set<BlockPos> findLogs(final World world, final BlockPos origin, final Block log, final Object claim) {
        final Set<BlockPos> logs = new LinkedHashSet<>();
        final Set<BlockPos> visited = new HashSet<>();
        final Deque<BlockPos> queue = new ArrayDeque<>();
        visited.add(origin);
        queue.add(origin);

        while (!queue.isEmpty() && logs.size() < MAX_LOGS) {
            final BlockPos pos = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        final BlockPos next = pos.add(dx, dy, dz);
                        if (next.getY() < origin.getY()
                                || Math.abs(next.getX() - origin.getX()) > LOG_SEARCH_RADIUS
                                || Math.abs(next.getZ() - origin.getZ()) > LOG_SEARCH_RADIUS
                                || !visited.add(next)
                                || !world.isBlockLoaded(next)) {
                            continue;
                        }

                        if (world.getBlockState(next).getBlock() == log && claim.equals(this.claims.claimAt(world, next))) {
                            logs.add(next);
                            queue.add(next);
                        }
                    }
                }
            }
        }

        return logs;
    }

    /**
     * Natural leaves and fruit leaves of this tree type reachable from the felled logs through other leaves.
     */
    private Set<BlockPos> findLeaves(final World world, final Set<BlockPos> felled, final Set<Block> leafBlocks, final Object claim) {
        final Set<BlockPos> leaves = new LinkedHashSet<>();
        final Map<BlockPos, Integer> distance = new HashMap<>();
        final Deque<BlockPos> queue = new ArrayDeque<>();
        for (final BlockPos pos : felled) {
            distance.put(pos, 0);
            queue.add(pos);
        }

        while (!queue.isEmpty() && leaves.size() < MAX_LEAVES) {
            final BlockPos pos = queue.poll();
            final int nextDistance = distance.get(pos) + 1;
            if (nextDistance > LEAF_SEARCH_RANGE) {
                continue;
            }

            for (final EnumFacing facing : EnumFacing.VALUES) {
                final BlockPos next = pos.offset(facing);
                if (distance.containsKey(next) || !world.isBlockLoaded(next)) {
                    continue;
                }

                final IBlockState state = world.getBlockState(next);
                if (leafBlocks.contains(state.getBlock()) && isNatural(state) && claim.equals(this.claims.claimAt(world, next))) {
                    distance.put(next, nextDistance);
                    leaves.add(next);
                    queue.add(next);
                }
            }
        }

        return leaves;
    }

    /**
     * The candidate leaves that are still within {@link #LEAF_SUPPORT_RANGE} leaf steps of a block that sustains leaves (a log) which is not
     * being felled. Mirrors vanilla decay: support spreads from logs through any leaves.
     */
    private Set<BlockPos> findSupported(final World world, final Set<BlockPos> candidates, final Set<BlockPos> felled) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (final BlockPos pos : candidates) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        minX -= LEAF_SUPPORT_RANGE;
        minY -= LEAF_SUPPORT_RANGE;
        minZ -= LEAF_SUPPORT_RANGE;
        maxX += LEAF_SUPPORT_RANGE;
        maxY += LEAF_SUPPORT_RANGE;
        maxZ += LEAF_SUPPORT_RANGE;

        final Map<BlockPos, Integer> distance = new HashMap<>();
        final Deque<BlockPos> queue = new ArrayDeque<>();
        final BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = Math.max(0, minY); y <= Math.min(255, maxY); y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    scan.setPos(x, y, z);
                    if (felled.contains(scan) || !world.isBlockLoaded(scan)) {
                        continue;
                    }
                    final IBlockState state = world.getBlockState(scan);
                    if (state.getBlock().canSustainLeaves(state, world, scan)) {
                        final BlockPos source = scan.toImmutable();
                        distance.put(source, 0);
                        queue.add(source);
                    }
                }
            }
        }

        final Set<BlockPos> supported = new HashSet<>();
        while (!queue.isEmpty()) {
            final BlockPos pos = queue.poll();
            final int nextDistance = distance.get(pos) + 1;
            if (nextDistance > LEAF_SUPPORT_RANGE) {
                continue;
            }

            for (final EnumFacing facing : EnumFacing.VALUES) {
                final BlockPos next = pos.offset(facing);
                if (distance.containsKey(next) || felled.contains(next)
                        || next.getX() < minX || next.getX() > maxX || next.getZ() < minZ || next.getZ() > maxZ
                        || next.getY() < minY || next.getY() > maxY || !world.isBlockLoaded(next)) {
                    continue;
                }

                final IBlockState state = world.getBlockState(next);
                if (state.getBlock().isLeaves(state, world, next)) {
                    distance.put(next, nextDistance);
                    queue.add(next);
                    if (candidates.contains(next)) {
                        supported.add(next);
                    }
                }
            }
        }

        return supported;
    }

    private void remove(final World world, final BlockPos pos, final Block log, final Set<Block> leafBlocks) {
        if (!world.isBlockLoaded(pos)) {
            return;
        }

        // The world may have changed since the tree was scanned, only remove what is still there
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block == log) {
            // Drops the log's normal items where it was
            world.destroyBlock(pos, true);
        } else if (leafBlocks.contains(block) && block instanceof BlockLeaves && isNatural(state)) {
            // Same path as natural decay: drops the leaf's configured decay items (fruit, saplings) where it was
            ((BlockLeavesAccessor) (Object) block).invoker$destroy(world, pos);
        }
    }

    private static boolean isNatural(final IBlockState state) {
        // Player placed leaves are not decayable
        return !state.getPropertyKeys().contains(BlockLeaves.DECAYABLE) || state.getValue(BlockLeaves.DECAYABLE);
    }
}
