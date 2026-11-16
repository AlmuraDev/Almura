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
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts a 3.5 world's regions to 26.3 with {@link LegacyWorldConverter}; only when asked, as it reads and writes worlds:
 * <pre>./gradlew test --tests com.almuradev.almura.legacy.LegacyWorldConversionTest -Palmura.convert.level=&lt;3.5 level.dat&gt;
 *   -Palmura.convert.regions=&lt;its region folder&gt; -Palmura.convert.out=&lt;folder for the new region files&gt;
 *   -Palmura.convert.dimension=terraincontrol:orilla -Palmura.convert.tcWorld=&lt;3.5 TerrainControl WorldConfig.ini&gt;
 *   [-Palmura.convert.only=r.0.0.mca,r.-1.0.mca]</pre>
 * Read from copies: the region files are opened as the game opens them.
 */
final class LegacyWorldConversionTest {

    private static final Pattern CUSTOM_BIOME = Pattern.compile("([^:,]+):(\\d+)");

    @Test
    void convert() throws IOException {
        final String levelFile = System.getProperty("almura.convert.level");
        assumeTrue(levelFile != null, "No world to convert");
        final Path regions = Path.of(System.getProperty("almura.convert.regions"));
        final Path out = Path.of(System.getProperty("almura.convert.out"));
        final Identifier dimension = Identifier.parse(System.getProperty("almura.convert.dimension"));
        final String only = System.getProperty("almura.convert.only", "");

        final LegacyWorldConverter converter = new LegacyWorldConverter(blockNames(Path.of(levelFile)),
            customBiomes(Path.of(System.getProperty("almura.convert.tcWorld")), dimension), dimension);
        Files.createDirectories(out);
        final RegionStorageInfo info = new RegionStorageInfo("convert", ResourceKey.create(Registries.DIMENSION, dimension), "chunk");
        try (var files = Files.list(regions)) {
            for (final Path file : files.filter(f -> f.getFileName().toString().endsWith(".mca")).sorted().toList()) {
                if (!only.isEmpty() && !List.of(only.split(",")).contains(file.getFileName().toString())) {
                    continue;
                }
                final String[] parts = file.getFileName().toString().split("\\.");
                final int regionX = Integer.parseInt(parts[1]), regionZ = Integer.parseInt(parts[2]);
                try (RegionFile input = new RegionFile(info, file, regions, false);
                     RegionFile output = new RegionFile(info, out.resolve(file.getFileName()), out, false)) {
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
                System.out.println("Converted " + file.getFileName());
            }
        }
        System.out.println(converter.report);
        Files.writeString(out.resolve("conversion-report.txt"), converter.report.toString());
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
