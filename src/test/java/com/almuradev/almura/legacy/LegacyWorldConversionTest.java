/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.legacy;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts a 3.5 world to 26.3 with {@link LegacyWorldConverter}, straight into a 4.0 world; only when asked, as it rewrites worlds:
 * <pre>./gradlew convertWorld -Palmura.convert.from=C:/spongesrv/Dakara -Palmura.convert.world=Orilla
 *   -Palmura.convert.to="C:/NeoSpongeSrv/Almura Test" [-Palmura.convert.only=r.0.0.mca,r.-1.0.mca]</pre>
 * <ul>
 *   <li>{@code from}: the 3.5 world folder holding level.dat (its block numbers); {@code world}: the world's folder in it.</li>
 *   <li>{@code to}: the 4.0 world folder. Each region converted replaces 4.0's, whose region, entities and poi files are first moved to
 *   {@code <to>/../almura-convert-backups/<time>/<world>/}; its entities and poi go, so the converted chunks' mobs come once. Nothing is
 *   written while the 4.0 world is open (its session.lock held by a running server).</li>
 *   <li>{@code out} instead of {@code to}: a trial, the region files written there alone.</li>
 *   <li>{@code dimension}: the 4.0 dimension, by default {@code terraincontrol:<world>}; {@code tcWorld}: the 3.5 TerrainControl world
 *   config (its custom biomes' numbers), by default {@code <from>/../config/TerrainControl/worlds/<world>/WorldConfig.ini}.</li>
 * </ul>
 * The 3.5 regions are read from copies, as the game opens region files for writing; 3.5's world is never changed.
 */
final class LegacyWorldConversionTest {

    private static final Pattern CUSTOM_BIOME = Pattern.compile("([^:,]+):(\\d+)");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final String OPEN = " is open in a running server; stop it first";

    @Test
    void convert() throws IOException {
        final String from = System.getProperty("almura.convert.from");
        assumeTrue(from != null, "No world to convert");
        final Path legacy = Path.of(from).toAbsolutePath();
        final String world = Objects.requireNonNull(System.getProperty("almura.convert.world"), "-Palmura.convert.world is needed");
        final Path regions = legacy.resolve(world).resolve("region");
        final Identifier dimension = Identifier.parse(System.getProperty("almura.convert.dimension",
            "terraincontrol:" + world.toLowerCase(Locale.ROOT)));
        final Path tcWorld = Path.of(System.getProperty("almura.convert.tcWorld",
            legacy.getParent().resolve("config/TerrainControl/worlds").resolve(world).resolve("WorldConfig.ini").toString()));
        final String only = System.getProperty("almura.convert.only", "");
        if (!Files.isDirectory(regions)) {
            throw new IOException("No 3.5 regions at " + regions);
        }

        // Where the converted regions go: a trial folder, or the 4.0 world's dimension, with what it had backed up
        final Path regionOut;
        final Path backup;
        final @Nullable Path dimensionFolder;
        final String trial = System.getProperty("almura.convert.out");
        if (trial != null) {
            regionOut = Path.of(trial);
            dimensionFolder = null;
            backup = regionOut;
        } else {
            final Path target = Path.of(Objects.requireNonNull(System.getProperty("almura.convert.to"),
                "-Palmura.convert.to (the 4.0 world) or -Palmura.convert.out (a trial folder) is needed")).toAbsolutePath();
            if (!Files.isRegularFile(target.resolve("level.dat"))) {
                throw new IOException(target + " isn't a world folder (no level.dat)");
            }
            requireClosed(target);
            dimensionFolder = target.resolve("dimensions").resolve(dimension.getNamespace()).resolve(dimension.getPath());
            regionOut = dimensionFolder.resolve("region");
            backup = target.getParent().resolve("almura-convert-backups").resolve(LocalDateTime.now().format(STAMP)).resolve(world);
        }
        Files.createDirectories(regionOut);
        Files.createDirectories(backup);

        final LegacyWorldConverter converter = new LegacyWorldConverter(blockNames(legacy.resolve("level.dat")), customBiomes(tcWorld, dimension),
            dimension);
        final RegionStorageInfo info = new RegionStorageInfo("convert", ResourceKey.create(Registries.DIMENSION, dimension), "chunk");
        final Path scratch = Files.createTempDirectory("almura-convert");
        int regionsDone = 0;
        try (var files = Files.list(regions)) {
            for (final Path file : files.filter(f -> f.getFileName().toString().endsWith(".mca")).sorted().toList()) {
                final String name = file.getFileName().toString();
                if (!only.isEmpty() && !List.of(only.split(",")).contains(name)) {
                    continue;
                }
                if (dimensionFolder != null) {
                    // 4.0's own files for this region set aside: its terrain, and the entities and poi that would come back twice
                    for (final String folder : List.of("region", "entities", "poi")) {
                        final Path existing = dimensionFolder.resolve(folder).resolve(name);
                        if (Files.isRegularFile(existing)) {
                            Files.createDirectories(backup.resolve(folder));
                            Files.move(existing, backup.resolve(folder).resolve(name), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
                final Path copy = scratch.resolve(name);
                Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
                final String[] parts = name.split("\\.");
                final int regionX = Integer.parseInt(parts[1]), regionZ = Integer.parseInt(parts[2]);
                try (RegionFile input = new RegionFile(info, copy, scratch, false);
                     RegionFile output = new RegionFile(info, regionOut.resolve(name), regionOut, false)) {
                    for (int x = 0; x < 32; x++) {
                        for (int z = 0; z < 32; z++) {
                            final ChunkPos pos = new ChunkPos(regionX * 32 + x, regionZ * 32 + z);
                            final CompoundTag chunk;
                            try (DataInputStream stream = input.getChunkDataInputStream(pos)) {
                                if (stream == null) {
                                    continue;
                                }
                                chunk = NbtIo.read(stream);
                            }
                            final CompoundTag converted = converter.convert(chunk);
                            try (DataOutputStream stream = output.getChunkDataOutputStream(pos)) {
                                NbtIo.write(converted, stream);
                            }
                        }
                    }
                }
                Files.delete(copy);
                regionsDone++;
                System.out.println("Converted " + name + " (" + regionsDone + ")");
            }
        } finally {
            try (var left = Files.list(scratch)) {
                for (final Path path : left.toList()) {
                    Files.deleteIfExists(path);
                }
            }
            Files.deleteIfExists(scratch);
        }
        final String report = "World: " + world + " -> " + dimension + " in " + regionOut + "\nRegions: " + regionsDone + "\n" + converter.report;
        System.out.println(report);
        Files.writeString(backup.resolve("conversion-report.txt"), report);
        System.out.println("Report and 4.0's replaced files: " + backup);
    }

    /**
     * Fails when the 4.0 world is open: a running server holds its session.lock.
     */
    private static void requireClosed(final Path world) throws IOException {
        final Path lock = world.resolve("session.lock");
        if (!Files.exists(lock)) {
            return;
        }
        final FileLock held;
        try (FileChannel channel = FileChannel.open(lock, StandardOpenOption.WRITE)) {
            try {
                held = channel.tryLock();
            } catch (final OverlappingFileLockException | IOException e) {
                throw new IOException(world + OPEN, e);
            }
            if (held == null) {
                throw new IOException(world + OPEN);
            }
            held.release();
        }
    }

    /**
     * 3.5's block numbers, from the FML registry in its level.dat.
     */
    private static Map<Integer, String> blockNames(final Path levelFile) throws IOException {
        final CompoundTag level = NbtIo.readCompressed(levelFile, NbtAccounter.unlimitedHeap());
        final Map<Integer, String> names = new HashMap<>();
        for (final Tag tag : level.getCompoundOrEmpty("FML").getCompoundOrEmpty("Registries").getCompoundOrEmpty("minecraft:blocks")
            .getListOrEmpty("ids")) {
            if (tag instanceof final CompoundTag entry) {
                names.put(entry.getIntOr("V", -1), entry.getStringOr("K", ""));
            }
        }
        return names;
    }

    /**
     * TerrainControl's custom biomes of the world by the number 3.5 saved them with (their id's last three digits, as in
     * {@code CustomBiomes: Lakes:8045}), as their 26.3 biome: {@code terraincontrol:<world>/<biome>}.
     */
    private static Map<Integer, String> customBiomes(final Path worldConfig, final Identifier dimension) throws IOException {
        final Map<Integer, String> biomes = new HashMap<>();
        for (final String line : Files.readAllLines(worldConfig)) {
            if (line.startsWith("CustomBiomes:")) {
                final Matcher matcher = CUSTOM_BIOME.matcher(line.substring("CustomBiomes:".length()));
                while (matcher.find()) {
                    final String name = matcher.group(1).trim().toLowerCase(Locale.ROOT).replace("+", "plus").replaceAll("[^a-z0-9._-]", "_");
                    biomes.put(Integer.parseInt(matcher.group(2)) % 1000, "terraincontrol:" + dimension.getPath() + "/" + name);
                }
            }
        }
        return biomes;
    }
}
