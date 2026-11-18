/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.treefeller;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.def.ContentDefs.TreeDef;
import com.almuradev.almura.content.registry.ContentTrees;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Fells fruit trees, as 3.5's {@code ServerTreeFellerManager}: when a player breaks the log of a tree that bears fruit, every connected log
 * at or above the cut is broken and every leaf and fruit leaf left with no log within reach drops what breaking it by hand would (its
 * loot table) where it was and disappears.
 * <p>
 * Fruit trees are the content's trees that name a fruit block. Only natural leaves count (not {@code persistent}: player placed ones are),
 * so a player's leaves are never touched, and logs of a fruit tree's kind that touch no natural fruit tree leaves (a build) are left alone.
 * Large trees are taken down over several ticks. 3.5 also kept a felled tree inside the claim of the log cut (GriefDefender); that comes
 * back with GriefDefender on 4.0.
 */
public final class ServerTreeFeller {

    // A leaf stays while a log is within this many leaf steps: 26.3's leaves (1.12's reached 4)
    private static final int LEAF_SUPPORT_RANGE = LeavesBlock.DECAY_DISTANCE - 1;
    // How far (in leaf steps) from the felled logs leaves are collected; fruit leaves sit beside leaves, so a little further
    private static final int LEAF_SEARCH_RANGE = LEAF_SUPPORT_RANGE + 2;
    // Horizontal distance from the cut that logs are followed, so a felled tree can't chain into a forest
    private static final int LOG_SEARCH_RADIUS = 10;
    private static final int MAX_LOGS = 256, MAX_LEAVES = 4096;
    // Blocks removed a tick, so a large tree never makes a lag spike
    private static final int BLOCKS_PER_TICK = 64;

    // Log -> the leaf and fruit leaf blocks of every fruit tree using it (several trees can share a log, as the citrus trees do)
    private @Nullable Map<Block, Set<Block>> fruitLeavesByLog;
    // Cuts to look at on the next tick, once the break is in the world, and trees being taken down
    private final List<Cut> cuts = new ArrayList<>();
    private final List<Felling> fellings = new ArrayList<>();

    private record Cut(ServerLevel level, BlockPos origin, Block log, Set<Block> leaves) {
    }

    private record Felling(ServerLevel level, Iterator<BlockPos> blocks, Block log, Set<Block> leaves) {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBreak(final BreakBlockEvent event) {
        // A player's own break only (survival or creative), not a machine's
        if (!(event.getLevel() instanceof final ServerLevel level) || event.getPlayer() instanceof FakePlayer || event.isCanceled()) {
            return;
        }
        final Block block = event.getState().getBlock();
        final Set<Block> leaves = this.fruitLeavesByLog().get(block);
        if (leaves != null) {
            this.cuts.add(new Cut(level, event.getPos().immutable(), block, leaves));
        }
    }

    @SubscribeEvent
    public void onServerTick(final ServerTickEvent.Post event) {
        if (!this.cuts.isEmpty()) {
            final List<Cut> cuts = List.copyOf(this.cuts);
            this.cuts.clear();
            cuts.forEach(this::fell);
        }
        for (final Iterator<Felling> iterator = this.fellings.iterator(); iterator.hasNext(); ) {
            final Felling felling = iterator.next();
            for (int i = 0; i < BLOCKS_PER_TICK && felling.blocks.hasNext(); i++) {
                remove(felling.level, felling.blocks.next(), felling.log, felling.leaves);
            }
            if (!felling.blocks.hasNext()) {
                iterator.remove();
            }
        }
    }

    private Map<Block, Set<Block>> fruitLeavesByLog() {
        if (this.fruitLeavesByLog == null) {
            final Map<Block, Set<Block>> map = new HashMap<>();
            for (final TreeDef tree : ContentTrees.all()) {
                if (tree.fruit() == null) {
                    continue;
                }
                final Block log = block(tree.log()), leaves = block(tree.leaves()), fruit = block(tree.fruit());
                if (log == Blocks.AIR || leaves == Blocks.AIR || fruit == Blocks.AIR) {
                    Almura.LOGGER.warn("Fruit tree {}'s blocks aren't all there; it won't be felled", tree.id());
                    continue;
                }
                final Set<Block> leafBlocks = map.computeIfAbsent(log, key -> new HashSet<>());
                leafBlocks.add(leaves);
                leafBlocks.add(fruit);
            }
            this.fruitLeavesByLog = map;
        }
        return this.fruitLeavesByLog;
    }

    private static Block block(final String id) {
        final Identifier parsed = Identifier.tryParse(id);
        return parsed == null ? Blocks.AIR : BuiltInRegistries.BLOCK.getOptional(parsed).orElse(Blocks.AIR);
    }

    private void fell(final Cut cut) {
        final ServerLevel level = cut.level;
        // Still there: something stopped the break after the event (a protection plugin)
        if (!level.isLoaded(cut.origin) || level.getBlockState(cut.origin).is(cut.log)) {
            return;
        }
        final Set<BlockPos> logs = findLogs(level, cut.origin, cut.log);
        // The cut log is gone too, so leaves are collected from it as well as from the logs being felled
        final Set<BlockPos> felled = new HashSet<>(logs);
        felled.add(cut.origin);
        final Set<BlockPos> leaves = findLeaves(level, felled, cut.leaves);
        // Touching no natural fruit tree leaves: a build made of logs, not a tree
        if (leaves.isEmpty()) {
            return;
        }
        // Leaves still held up by another log (a neighbouring tree, a log below the cut) stay, the same rule as decay
        leaves.removeAll(findSupported(level, leaves, felled));
        final List<BlockPos> blocks = new ArrayList<>(logs.size() + leaves.size());
        blocks.addAll(logs);
        blocks.addAll(leaves);
        if (!blocks.isEmpty()) {
            this.fellings.add(new Felling(level, blocks.iterator(), cut.log, cut.leaves));
        }
    }

    /**
     * Logs of the same kind connected (diagonally too, for branches) to the cut, at or above it.
     */
    private static Set<BlockPos> findLogs(final ServerLevel level, final BlockPos origin, final Block log) {
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
                        final BlockPos next = pos.offset(dx, dy, dz);
                        if (next.getY() < origin.getY() || Math.abs(next.getX() - origin.getX()) > LOG_SEARCH_RADIUS
                            || Math.abs(next.getZ() - origin.getZ()) > LOG_SEARCH_RADIUS || !visited.add(next) || !level.isLoaded(next)) {
                            continue;
                        }
                        if (level.getBlockState(next).is(log)) {
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
     * Natural leaves and fruit leaves of the tree's kinds reachable from the felled logs through other leaves.
     */
    private static Set<BlockPos> findLeaves(final ServerLevel level, final Set<BlockPos> felled, final Set<Block> leafBlocks) {
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
            for (final Direction direction : Direction.values()) {
                final BlockPos next = pos.relative(direction);
                if (distance.containsKey(next) || !level.isLoaded(next)) {
                    continue;
                }
                final BlockState state = level.getBlockState(next);
                if (leafBlocks.contains(state.getBlock()) && natural(state)) {
                    distance.put(next, nextDistance);
                    leaves.add(next);
                    queue.add(next);
                }
            }
        }
        return leaves;
    }

    /**
     * The candidate leaves still within {@link #LEAF_SUPPORT_RANGE} leaf steps of a log that isn't being felled: support spreads from logs
     * through any leaves, as 26.3's decay does.
     */
    private static Set<BlockPos> findSupported(final ServerLevel level, final Set<BlockPos> candidates, final Set<BlockPos> felled) {
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
        minY = Math.max(level.getMinY(), minY - LEAF_SUPPORT_RANGE);
        minZ -= LEAF_SUPPORT_RANGE;
        maxX += LEAF_SUPPORT_RANGE;
        maxY = Math.min(level.getMaxY(), maxY + LEAF_SUPPORT_RANGE);
        maxZ += LEAF_SUPPORT_RANGE;

        final Map<BlockPos, Integer> distance = new HashMap<>();
        final Deque<BlockPos> queue = new ArrayDeque<>();
        final BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    scan.set(x, y, z);
                    if (felled.contains(scan) || !level.isLoaded(scan)) {
                        continue;
                    }
                    // What holds leaves up: a log (26.3's leaves count distance 0 from it)
                    final OptionalInt holds = LeavesBlock.getOptionalDistanceAt(level.getBlockState(scan));
                    if (holds.isPresent() && holds.getAsInt() == 0) {
                        final BlockPos source = scan.immutable();
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
            for (final Direction direction : Direction.values()) {
                final BlockPos next = pos.relative(direction);
                if (distance.containsKey(next) || felled.contains(next) || next.getX() < minX || next.getX() > maxX || next.getZ() < minZ
                    || next.getZ() > maxZ || next.getY() < minY || next.getY() > maxY || !level.isLoaded(next)) {
                    continue;
                }
                if (level.getBlockState(next).getBlock() instanceof LeavesBlock) {
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

    private static void remove(final ServerLevel level, final BlockPos pos, final Block log, final Set<Block> leafBlocks) {
        if (!level.isLoaded(pos)) {
            return;
        }
        // The world may have changed since the tree was looked at: only what's still there goes
        final BlockState state = level.getBlockState(pos);
        if (state.is(log)) {
            // The log's own drops, where it was
            level.destroyBlock(pos, true);
        } else if (leafBlocks.contains(state.getBlock()) && natural(state)) {
            // What breaking it by hand drops (its loot table: a fruit leaf's fruit), not decay's chance; no particles or sound, as decay
            Block.dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    private static boolean natural(final BlockState state) {
        // Player placed leaves are persistent
        return !state.hasProperty(LeavesBlock.PERSISTENT) || !state.getValue(LeavesBlock.PERSISTENT);
    }
}
