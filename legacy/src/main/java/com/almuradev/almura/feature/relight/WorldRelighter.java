/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.relight;

import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Relights every saved chunk of a world, a few milliseconds each tick: chunks are taken in 8x8 blocks, each loaded with a two chunk
 * border (light spreads up to 17 blocks, so a light check needs its neighbours), relit like /almura relight, then left to unload
 * and save again. Only chunks on disk are loaded, nothing is generated; chunks players have loaded stay and are resent to them.
 */
final class WorldRelighter {

    private static final Pattern REGION = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");
    private static final int BLOCK = 8;
    private static final int BORDER = 2;
    private static final long BUDGET_NANOS = 15_000_000L;

    @Nullable private static WorldRelighter running;

    private final WorldServer world;
    private final CommandSource source;
    private final Logger logger;
    private final Set<Long> saved;
    private final List<List<ChunkPos>> blocks;
    private final Task task;

    private int block;
    private int step; // index into the current block's load list, then its relight list
    private boolean relighting;
    private List<ChunkPos> load = new ArrayList<>();
    private int chunksDone;
    private int relit;
    private int lastReported;

    private WorldRelighter(final WorldServer world, final CommandSource source, final Logger logger, final PluginContainer container,
            final Set<Long> saved) {
        this.world = world;
        this.source = source;
        this.logger = logger;
        this.saved = saved;
        // group the saved chunks by 8x8 block, in order
        final Map<Long, List<ChunkPos>> grouped = new LinkedHashMap<>();
        saved.stream().map(WorldRelighter::pos).sorted((a, b) -> a.x != b.x ? Integer.compare(a.x, b.x) : Integer.compare(a.z, b.z))
                .forEach(pos -> grouped.computeIfAbsent(ChunkPos.asLong(Math.floorDiv(pos.x, BLOCK), Math.floorDiv(pos.z, BLOCK)),
                        k -> new ArrayList<>()).add(pos));
        this.blocks = new ArrayList<>(grouped.values());
        this.task = Task.builder().name("almura-relight-" + world.getWorldInfo().getWorldName()).intervalTicks(1).execute(this::tick)
                .submit(container);
    }

    static boolean isRunning() {
        return running != null;
    }

    /** Starts relighting the world, returning how many saved chunks it will go through. */
    static int start(final WorldServer world, final CommandSource source, final Logger logger, final PluginContainer container) throws IOException {
        final Set<Long> saved = savedChunks(new File(world.getChunkSaveLocation(), "region"));
        running = new WorldRelighter(world, source, logger, container, saved);
        return saved.size();
    }

    static boolean stop() {
        if (running == null) {
            return false;
        }
        running.finish("Relighting stopped");
        return true;
    }

    private void tick(final Task task) {
        final long end = System.nanoTime() + BUDGET_NANOS;
        final ChunkProviderServer provider = this.world.getChunkProvider();
        while (System.nanoTime() < end) {
            if (this.block >= this.blocks.size()) {
                this.finish("Relighting finished");
                return;
            }
            final List<ChunkPos> inner = this.blocks.get(this.block);
            if (!this.relighting) {
                if (this.step == 0) {
                    this.load = this.window(inner);
                }
                if (this.step < this.load.size()) {
                    final ChunkPos pos = this.load.get(this.step++);
                    provider.loadChunk(pos.x, pos.z);
                    continue;
                }
                this.relighting = true;
                this.step = 0;
            }
            if (this.step < inner.size()) {
                final ChunkPos pos = inner.get(this.step++);
                final Chunk chunk = provider.getLoadedChunk(pos.x, pos.z);
                if (chunk != null) {
                    this.relit += RelightCommand.relight(this.world, chunk);
                    final PlayerChunkMapEntry entry = this.world.getPlayerChunkMap().getEntry(pos.x, pos.z);
                    if (entry != null && entry.isSentToPlayers()) {
                        entry.sendPacket(new SPacketChunkData(chunk, 65535));
                    }
                }
                this.chunksDone++;
                continue;
            }
            // block done: let the chunks no player needs unload (and save)
            for (final ChunkPos pos : this.load) {
                final Chunk chunk = provider.getLoadedChunk(pos.x, pos.z);
                if (chunk != null && this.world.getPlayerChunkMap().getEntry(pos.x, pos.z) == null) {
                    provider.queueUnload(chunk);
                }
            }
            this.block++;
            this.step = 0;
            this.relighting = false;
            this.report();
        }
    }

    /** The block's chunks with their border, those that are saved. */
    private List<ChunkPos> window(final List<ChunkPos> inner) {
        final ChunkPos first = inner.get(0);
        final int bx = Math.floorDiv(first.x, BLOCK) * BLOCK;
        final int bz = Math.floorDiv(first.z, BLOCK) * BLOCK;
        final List<ChunkPos> window = new ArrayList<>();
        for (int x = bx - BORDER; x < bx + BLOCK + BORDER; x++) {
            for (int z = bz - BORDER; z < bz + BLOCK + BORDER; z++) {
                if (this.saved.contains(ChunkPos.asLong(x, z))) {
                    window.add(new ChunkPos(x, z));
                }
            }
        }
        return window;
    }

    private void report() {
        final int percent = this.saved.isEmpty() ? 100 : this.chunksDone * 100 / this.saved.size();
        if (percent / 10 > this.lastReported / 10) {
            this.lastReported = percent;
            this.message(TextColors.GRAY, "Relighting " + this.name() + ": " + percent + "% (" + this.chunksDone + " chunks, " + this.relit + " light sources)");
        }
    }

    private void finish(final String what) {
        this.task.cancel();
        running = null;
        this.message(TextColors.GREEN, what + " for " + this.name() + ": " + this.chunksDone + " of " + this.saved.size() + " chunks, "
                + this.relit + " light sources relit.");
    }

    private void message(final TextColor color, final String message) {
        this.logger.info(message);
        this.source.sendMessage(Text.of(color, message));
    }

    private String name() {
        return this.world.getWorldInfo().getWorldName();
    }

    private static ChunkPos pos(final long key) {
        return new ChunkPos((int) key, (int) (key >> 32));
    }

    /** Every chunk present in the region files, as {@link ChunkPos#asLong} keys. */
    private static Set<Long> savedChunks(final File regionDir) throws IOException {
        final Set<Long> chunks = new HashSet<>();
        final File[] files = regionDir.listFiles();
        if (files == null) {
            return chunks;
        }
        final byte[] header = new byte[4096];
        for (final File file : files) {
            final Matcher matcher = REGION.matcher(file.getName());
            if (!matcher.matches() || file.length() < 8192) {
                continue;
            }
            final int rx = Integer.parseInt(matcher.group(1));
            final int rz = Integer.parseInt(matcher.group(2));
            try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
                in.readFully(header);
            }
            for (int i = 0; i < 1024; i++) {
                if (header[i * 4] != 0 || header[i * 4 + 1] != 0 || header[i * 4 + 2] != 0) {
                    chunks.add(ChunkPos.asLong(rx * 32 + (i & 31), rz * 32 + (i >> 5)));
                }
            }
        }
        return chunks;
    }
}
