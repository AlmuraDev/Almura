/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.legacy;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

/**
 * Converts a 3.5 world to 26.3 with {@link LegacyWorldConverter}, straight into a 4.0 world; only when asked, as it rewrites worlds:
 * <pre>./gradlew convertWorld -Palmura.convert.from=C:/spongesrv/Dakara -Palmura.convert.world=Orilla
 *   -Palmura.convert.to="C:/NeoSpongeSrv/Almura Test" [-Palmura.convert.only=r.0.0.mca,r.-1.0.mca]</pre>
 * <ul>
 *   <li>{@code from}: the 3.5 world folder holding level.dat (its block numbers); {@code world}: the world's folder in it.</li>
 *   <li>{@code to}: the 4.0 world folder. Each region converted replaces 4.0's, whose region, entities and poi files are first moved to
 *   {@code <to>/../almura-convert-backups/<time>/<world>/}; its entities and poi go, so the converted chunks' mobs come once. Nothing is
 *   written while the 4.0 world is open (its session.lock held by a running server).</li>
 *   <li>{@code out} instead of {@code to}: a trial, the region files written there alone; SGCraft's world files (gates.dat, zpm_links.dat,
 *   the dimension's naquadah_ore_chunks.dat) are written there too, merged from those of the 4.0 world {@code sgcraftFrom} names (only
 *   read). With {@code to}, they are merged into the 4.0 world's own, each first copied to the backups.</li>
 *   <li>{@code dimension}: the 4.0 dimension, by default {@code terraincontrol:<world>}; {@code tcWorld}: the 3.5 TerrainControl world
 *   config (its custom biomes' numbers), by default {@code <from>/../config/TerrainControl/worlds/<world>/WorldConfig.ini}.</li>
 * </ul>
 * The 3.5 regions are read from copies, as the game opens region files for writing; 3.5's world is never changed.
 * <p>
 * {@code -Palmura.convert.players=true} (with or without {@code world}) converts 3.5's players too: see {@link #convertPlayers}; with
 * {@code -Palmura.convert.offlineCopies=true} each player whose name 3.5 knew is also written under their offline-mode UUID.
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
        final String world = System.getProperty("almura.convert.world");
        final boolean players = Boolean.parseBoolean(System.getProperty("almura.convert.players", "false"));
        final boolean markets = Boolean.parseBoolean(System.getProperty("almura.convert.markets", "false"));
        if (world == null && !players && !markets) {
            throw new IOException("-Palmura.convert.world (a world's regions), -Palmura.convert.players=true (3.5's players) or "
                + "-Palmura.convert.markets=true (3.5's stores and exchanges) is needed");
        }
        if (world != null) {
            convertWorld(legacy, world);
        }
        if (players) {
            convertPlayers(legacy);
        }
        if (markets) {
            convertMarkets(legacy);
        }
    }

    private static void convertWorld(final Path legacy, final String world) throws IOException {
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
        // The 4.0 world whose SGCraft files (gates.dat and the rest) the converted gates are merged into: the target, or for a trial the
        // world named by -Palmura.convert.sgcraftFrom (only read); they are written under the target, or the trial folder
        final @Nullable Path sgcraftSource;
        final Path sgcraftOut;
        final String trial = System.getProperty("almura.convert.out");
        if (trial != null) {
            regionOut = Path.of(trial);
            dimensionFolder = null;
            backup = regionOut;
            final String from4 = System.getProperty("almura.convert.sgcraftFrom");
            sgcraftSource = from4 == null ? null : Path.of(from4).toAbsolutePath();
            sgcraftOut = regionOut;
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
            sgcraftSource = target;
            sgcraftOut = target;
        }
        Files.createDirectories(regionOut);
        Files.createDirectories(backup);

        final LegacyWorldConverter converter = new LegacyWorldConverter(blockNames(legacy.resolve("level.dat")), customBiomes(tcWorld, dimension),
            dimension);
        final RegionStorageInfo info = new RegionStorageInfo("convert", ResourceKey.create(Registries.DIMENSION, dimension), "chunk");
        final String sgcraftSetup = sgcraftSetup(converter, legacy, world, dimension, sgcraftSource);
        if (converter.sgcraft.active()) {
            sgcraftPrescan(converter, regions, only);
        }
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
        String sgcraftReport = "";
        if (converter.sgcraft.active()) {
            converter.sgcraft.finish(sgcraftSource, sgcraftOut, trial == null ? backup : null, dimension);
            sgcraftReport = "\n" + sgcraftSetup + "\n" + converter.sgcraft.report + "\nSGCraft gates:" + converter.sgcraft.gates()
                + "\nSGCraft files: " + sgcraftOut + " (merged from " + sgcraftSource + ")";
        }
        final String report = "World: " + world + " -> " + dimension + " in " + regionOut + "\nRegions: " + regionsDone + "\n" + converter.report
            + "\nFurniture stand-ins (no 26.3 match): " + converter.cfm.substituted + sgcraftReport;
        System.out.println(report);
        Files.writeString(backup.resolve("conversion-report.txt"), report);
        System.out.println("Report and 4.0's replaced files: " + backup);
    }

    /**
     * 3.5's players ({@code <from>/playerdata}, by their Mojang UUIDs) into the 4.0 world's {@code players/data}. A player in a 3.5 world
     * already converted into 4.0 (its {@code terraincontrol:<world>} dimension has regions) stays where they were; everyone else starts at
     * the 4.0 world's spawn. A player's 4.0 file is first moved to {@code <to>/../almura-convert-backups/<time>/players/}. With {@code out}
     * (a trial) the players are written to {@code <out>/players/data}, the spawn and converted worlds read from {@code to} if given.
     */
    /**
     * 3.5's stores and exchanges ({@code <from>/../config/almura/h2.mv.db}) into the 4.0 server's database
     * ({@code <to>/../config/almura/database/almura.mv.db}), backed up first; with {@code -Palmura.convert.out} into a copy of it there
     * instead. H2 1.4 is read from {@code -Palmura.convert.h2Legacy}, by default Gradle's cached h2-1.4.196.jar. See
     * {@link LegacyMarketConversion}.
     */
    private static void convertMarkets(final Path legacy) throws IOException {
        final Path legacyDatabase = legacy.getParent().resolve("config").resolve("almura").resolve("h2.mv.db");
        if (!Files.isRegularFile(legacyDatabase)) {
            throw new IOException("No 3.5 database at " + legacyDatabase);
        }
        final Path target = Path.of(Objects.requireNonNull(System.getProperty("almura.convert.to"),
            "-Palmura.convert.to (the 4.0 world, beside the server's config) is needed")).toAbsolutePath();
        final Path currentDatabase = target.getParent().resolve("config").resolve("almura").resolve("database").resolve("almura.mv.db");
        if (!Files.isRegularFile(currentDatabase)) {
            throw new IOException("No 4.0 database at " + currentDatabase + " (start the server once first)");
        }
        final String trial = System.getProperty("almura.convert.out");
        final Path writeTo;
        final Path reportFolder;
        if (trial != null) {
            reportFolder = Path.of(trial).resolve("markets");
            Files.createDirectories(reportFolder);
            writeTo = reportFolder.resolve("almura.mv.db");
            Files.copy(currentDatabase, writeTo, StandardCopyOption.REPLACE_EXISTING);
        } else {
            requireClosed(target);
            reportFolder = target.getParent().resolve("almura-convert-backups").resolve(LocalDateTime.now().format(STAMP)).resolve("markets");
            LegacyMarketConversion.backup(currentDatabase, reportFolder);
            writeTo = currentDatabase;
        }
        final String jar = System.getProperty("almura.convert.h2Legacy");
        final Path h2Jar = jar != null ? Path.of(jar) : legacyH2Jar();
        final Path scratch = Files.createTempDirectory("almura-markets");
        final Path copy = scratch.resolve("h2.mv.db");
        Files.copy(legacyDatabase, copy);
        final LegacyMarketConversion markets = new LegacyMarketConversion(
            new LegacyWorldConverter(Map.of(), Map.of(), Identifier.withDefaultNamespace("overworld")));
        try (Connection from = LegacyMarketConversion.openLegacy(h2Jar, copy); Connection to = LegacyMarketConversion.openCurrent(writeTo)) {
            markets.convert(from, to);
        } catch (final SQLException e) {
            throw new IOException("Couldn't convert the stores and exchanges", e);
        }
        final String report = "Markets: " + legacyDatabase + " -> " + writeTo + "\n" + markets.report();
        System.out.println(report);
        Files.writeString(reportFolder.resolve("conversion-report.txt"), report);
    }

    // Gradle's cached H2 1.4, which opens 3.5's database
    private static Path legacyH2Jar() throws IOException {
        final Path cache = Path.of(System.getProperty("user.home"), ".gradle", "caches", "modules-2", "files-2.1", "com.h2database", "h2");
        try (var found = Files.walk(cache)) {
            return found.filter(path -> path.getFileName().toString().matches("h2-1\\.4\\.\\d+\\.jar")).findFirst()
                .orElseThrow(() -> new IOException("No H2 1.4 jar under " + cache + "; give one with -Palmura.convert.h2Legacy"));
        }
    }

    private static void convertPlayers(final Path legacy) throws IOException {
        final Path playerData = legacy.resolve("playerdata");
        if (!Files.isDirectory(playerData)) {
            throw new IOException("No 3.5 players at " + playerData);
        }
        final String to = System.getProperty("almura.convert.to");
        final @Nullable Path target = to == null ? null : Path.of(to).toAbsolutePath();
        final String trial = System.getProperty("almura.convert.out");
        if (target == null && trial == null) {
            throw new IOException("-Palmura.convert.to (the 4.0 world) or -Palmura.convert.out (a trial folder) is needed");
        }
        if (target != null && !Files.isRegularFile(target.resolve("level.dat"))) {
            throw new IOException(target + " isn't a world folder (no level.dat)");
        }
        final Path dataOut;
        final Path backup;
        if (trial != null) {
            dataOut = Path.of(trial).resolve("players").resolve("data");
            backup = Path.of(trial).resolve("players");
        } else {
            requireClosed(target);
            dataOut = target.resolve("players").resolve("data");
            backup = target.getParent().resolve("almura-convert-backups").resolve(LocalDateTime.now().format(STAMP)).resolve("players");
        }
        Files.createDirectories(dataOut);
        Files.createDirectories(backup);

        // The 4.0 world's spawn
        String spawnDimension = "minecraft:overworld";
        int[] spawn = {0, 100, 0};
        if (target != null) {
            final CompoundTag spawnTag = NbtIo.readCompressed(target.resolve("level.dat"), NbtAccounter.unlimitedHeap())
                .getCompoundOrEmpty("Data").getCompoundOrEmpty("spawn");
            spawnDimension = spawnTag.getStringOr("dimension", spawnDimension);
            spawn = spawnTag.getIntArray("pos").filter(pos -> pos.length == 3).orElse(spawn);
        }

        // 3.5's worlds by the dimension number Sponge gave them (in each world's level_sponge.dat), as 4.0 dimensions already converted
        final Map<Integer, String> converted = new HashMap<>();
        try (var folders = Files.list(legacy)) {
            for (final Path folder : folders.filter(Files::isDirectory).toList()) {
                final Path sponge = folder.resolve("level_sponge.dat");
                if (!Files.isRegularFile(sponge) || target == null) {
                    continue;
                }
                final int id = NbtIo.readCompressed(sponge, NbtAccounter.unlimitedHeap()).getCompoundOrEmpty("SpongeData").getIntOr("dimensionId",
                    Integer.MIN_VALUE);
                final String name = folder.getFileName().toString().toLowerCase(Locale.ROOT);
                final Path regions = target.resolve("dimensions").resolve("terraincontrol").resolve(name).resolve("region");
                if (id != Integer.MIN_VALUE && Files.isDirectory(regions)) {
                    try (var files = Files.list(regions)) {
                        if (files.anyMatch(f -> f.getFileName().toString().endsWith(".mca"))) {
                            converted.put(id, "terraincontrol:" + name);
                        }
                    }
                }
            }
        }

        // For an offline-mode server: 3.5's player names (its usercache.json), to write each player again under their offline UUID
        final boolean offlineCopies = Boolean.parseBoolean(System.getProperty("almura.convert.offlineCopies", "false"));
        final Map<String, String> names = offlineCopies ? playerNames(legacy.getParent().resolve("usercache.json")) : Map.of();

        final LegacyWorldConverter converter = new LegacyWorldConverter(Map.of(), Map.of(), Identifier.withDefaultNamespace("overworld"));
        // SGCraft's PDDs: their addresses made for the 4.0 server (its gates.dat's world symbols)
        final String sgcraftFrom = System.getProperty("almura.convert.sgcraftFrom");
        System.out.println(sgcraftSetup(converter, legacy, null, Identifier.withDefaultNamespace("overworld"),
            target != null ? target : sgcraftFrom == null ? null : Path.of(sgcraftFrom).toAbsolutePath()));
        int done = 0, copies = 0;
        try (var files = Files.list(playerData)) {
            for (final Path file : files.filter(f -> f.getFileName().toString().endsWith(".dat")).sorted().toList()) {
                final String name = file.getFileName().toString();
                final CompoundTag player = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
                final String dimension = converted.get(player.getIntOr("Dimension", 0));
                final CompoundTag upgraded = converter.convertPlayer(player, dimension, spawnDimension, spawn);
                writePlayer(upgraded, dataOut, name, trial == null ? backup : null);
                done++;

                final String playerName = names.get(name.substring(0, name.length() - 4));
                if (playerName != null) {
                    final UUID offline = UUIDUtil.createOfflinePlayerUUID(playerName);
                    final CompoundTag copy = upgraded.copy();
                    copy.putIntArray("UUID", UUIDUtil.uuidToIntArray(offline));
                    writePlayer(copy, dataOut, offline + ".dat", trial == null ? backup : null);
                    copies++;
                }
            }
        }
        final String report = "Players: " + done + " from " + playerData + " into " + dataOut
            + (offlineCopies ? "\nOffline-mode copies (by 3.5's names): " + copies : "") + "\nWorlds kept: " + converted + "\nSpawn: "
            + spawnDimension + " " + spawn[0] + " " + spawn[1] + " " + spawn[2] + "\n" + converter.report.players()
            + (converter.sgcraft.active() ? "\n" + converter.sgcraft.report : "");
        System.out.println(report);
        Files.writeString(backup.resolve("conversion-report.txt"), report);
        System.out.println("Report and 4.0's replaced player files: " + backup);
    }

    /**
     * Writes a converted player as {@code <data>/<file>}; the 4.0 file it replaces (and its {@code _old}) is first moved to the backup, if
     * given.
     */
    private static void writePlayer(final CompoundTag player, final Path data, final String file, final @Nullable Path backup) throws IOException {
        if (backup != null) {
            for (final String existing : List.of(file, file + "_old")) {
                final Path old = data.resolve(existing);
                if (Files.isRegularFile(old)) {
                    Files.move(old, backup.resolve(existing), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        NbtIo.writeCompressed(player, data.resolve(file));
    }

    /**
     * 3.5's player names by UUID, from its usercache.json (the players who joined in its last months).
     */
    private static Map<String, String> playerNames(final Path userCache) throws IOException {
        final Map<String, String> names = new HashMap<>();
        if (!Files.isRegularFile(userCache)) {
            throw new IOException("No 3.5 usercache.json at " + userCache + " for the offline-mode copies");
        }
        for (final JsonElement entry : JsonParser.parseString(Files.readString(userCache)).getAsJsonArray()) {
            final JsonObject user = entry.getAsJsonObject();
            names.put(user.get("uuid").getAsString(), user.get("name").getAsString());
        }
        return names;
    }

    // ---- SGCraft

    /**
     * Tells the SGCraft conversion what it needs: 3.5's worlds by dimension number (as 4.0's: {@code terraincontrol:<world>}, with the
     * overworld, nether and end vanilla's), 1.12's address dimension map, 3.5's players by name (its usercache.json, by the Mojang UUIDs the
     * players are converted under), and how the 4.0 server lays out addresses: its {@code config/sgcraft-server.json} (beside the world's
     * folder) and the symbols its gates.dat records. The converted world's cells are one chunk across unless that file gives it a size.
     *
     * @param world the 3.5 world being converted, or null (players)
     * @param source the 4.0 world, or null when there is none to read
     * @return what was found, for the report
     */
    private static String sgcraftSetup(final LegacyWorldConverter converter, final Path legacy, final @Nullable String world,
        final Identifier dimension, final @Nullable Path source) throws IOException {
        if (!converter.sgcraft.active()) {
            return "SGCraft: not loaded";
        }
        final StringBuilder out = new StringBuilder("SGCraft setup:");
        final Map<Integer, String> worlds = new HashMap<>();
        worlds.put(0, "minecraft:overworld");
        worlds.put(-1, "minecraft:the_nether");
        worlds.put(1, "minecraft:the_end");
        int legacyDimension = 0;
        try (var folders = Files.list(legacy)) {
            for (final Path folder : folders.filter(Files::isDirectory).toList()) {
                final Path sponge = folder.resolve("level_sponge.dat");
                final String name = folder.getFileName().toString();
                if (!Files.isRegularFile(sponge) || name.matches("DIM-?\\d+")) {
                    continue;
                }
                final int id = NbtIo.readCompressed(sponge, NbtAccounter.unlimitedHeap()).getCompoundOrEmpty("SpongeData").getIntOr("dimensionId",
                    Integer.MIN_VALUE);
                if (id == Integer.MIN_VALUE) {
                    continue;
                }
                if (name.equals(world)) {
                    legacyDimension = id;
                    worlds.put(id, dimension.toString());
                } else {
                    worlds.putIfAbsent(id, "terraincontrol:" + name.toLowerCase(Locale.ROOT));
                }
            }
        }
        out.append("\n  3.5 dimensions as 4.0 worlds: ").append(new TreeMap<>(worlds)).append("; converting dimension ").append(legacyDimension);
        final List<Integer> dimensionMap = new ArrayList<>();
        final Path mapFile = legacy.resolve("data").resolve("sgcraft-dimension_map.dat");
        if (Files.isRegularFile(mapFile)) {
            for (final int id : NbtIo.readCompressed(mapFile, NbtAccounter.unlimitedHeap()).getCompoundOrEmpty("data").getIntArray("dimensions")
                .orElse(new int[0])) {
                dimensionMap.add(id);
            }
        }
        out.append("\n  1.12 address dimension map: ").append(dimensionMap);

        // 3.5's players by name; a name cached twice (once under an offline-mode UUID) by its Mojang UUID, else the latest
        final Map<String, UUID> players = new HashMap<>();
        final Map<String, String> properNames = new HashMap<>();
        final Map<String, String> expires = new HashMap<>();
        final Path userCache = legacy.getParent().resolve("usercache.json");
        if (Files.isRegularFile(userCache)) {
            for (final JsonElement entry : JsonParser.parseString(Files.readString(userCache)).getAsJsonArray()) {
                final JsonObject user = entry.getAsJsonObject();
                final String name = user.get("name").getAsString();
                final String key = name.toLowerCase(Locale.ROOT);
                final UUID uuid = UUID.fromString(user.get("uuid").getAsString());
                final String expiry = user.has("expiresOn") ? user.get("expiresOn").getAsString() : "";
                final UUID known = players.get(key);
                final boolean better = known == null || known.version() != 4 && uuid.version() == 4
                    || known.version() == uuid.version() && expiry.compareTo(expires.get(key)) > 0;
                if (better) {
                    players.put(key, uuid);
                    properNames.put(key, name);
                    expires.put(key, expiry);
                }
            }
        }
        out.append("\n  3.5 players by name (").append(userCache).append("): ").append(players.size());
        final boolean offlineCopies = Boolean.parseBoolean(System.getProperty("almura.convert.offlineCopies", "false"));
        if (offlineCopies) {
            out.append(", each also given rights under their offline-mode UUID");
        }

        // The 4.0 server's address layout
        final Map<String, SgcraftAddresses.Layout> layouts = new HashMap<>();
        @Nullable SgcraftAddresses addresses = null;
        JsonObject settings = new JsonObject();
        final @Nullable Path config = source == null ? null : source.getParent().resolve("config").resolve("sgcraft-server.json");
        if (config != null && Files.isRegularFile(config)) {
            final JsonElement parsed = JsonParser.parseString(Files.readString(config));
            if (parsed.isJsonObject() && parsed.getAsJsonObject().has("addresses")) {
                settings = parsed.getAsJsonObject().getAsJsonObject("addresses");
            }
        }
        final Path gatesFile = source == null ? Path.of("") : source.resolve("data/sgcraft/gates.dat");
        final CompoundTag gates = source != null && Files.isRegularFile(gatesFile)
            ? NbtIo.readCompressed(gatesFile, NbtAccounter.unlimitedHeap()).getCompoundOrEmpty("data") : new CompoundTag();
        final CompoundTag worldSymbols = gates.getCompoundOrEmpty("world_symbols");
        final CompoundTag worldGroups = gates.getCompoundOrEmpty("world_groups");
        final CompoundTag groupSymbols = gates.getCompoundOrEmpty("group_symbols");
        final JsonObject worldSettings = settings.has("worlds") ? settings.getAsJsonObject("worlds") : new JsonObject();
        final int cellDefault = settings.has("cell_chunks") ? settings.get("cell_chunks").getAsInt() : 8;
        final String originDefault = settings.has("point_of_origin") ? settings.get("point_of_origin").getAsString() : "@";
        final Set<String> known = new TreeSet<>(worlds.values());
        known.addAll(worldSymbols.keySet());
        for (final String id : known) {
            final JsonObject own = worldSettings.has(id) ? worldSettings.getAsJsonObject(id) : new JsonObject();
            int cells = own.has("cell_chunks") ? own.get("cell_chunks").getAsInt() : cellDefault;
            if (id.equals(dimension.toString()) && world != null && !own.has("cell_chunks")) {
                // As decided for the converted worlds: cells one chunk across, as 1.12's addresses were, so no two gates share one
                cells = 1;
                out.append("\n  ").append(id).append(": cell_chunks 1 assumed - set addresses.worlds.\"").append(id)
                    .append("\".cell_chunks = 1 in ").append(config).append(" before the server next starts");
            }
            final String origin = own.has("point_of_origin") ? own.get("point_of_origin").getAsString() : originDefault;
            final String group = worldGroups.getStringOr(id, own.has("group") ? own.get("group").getAsString() : "default");
            final String groupSymbol = groupSymbols.getStringOr(group, group.equals("default") ? "@" : "");
            final String worldSymbol = worldSymbols.getStringOr(id, "");
            layouts.put(id, new SgcraftAddresses.Layout(cells, origin.isEmpty() ? null : origin.charAt(0),
                worldSymbol.isEmpty() ? null : worldSymbol.charAt(0), groupSymbol.isEmpty() ? null : groupSymbol.charAt(0)));
            out.append("\n  ").append(id).append(": cells of ").append(cells).append(" chunks, origin ").append(origin.isEmpty() ? "none" : origin)
                .append(", world symbol ").append(worldSymbol.isEmpty() ? "none yet (no address to it can be made)" : worldSymbol)
                .append(", group ").append(group);
        }
        if (settings.has("secret_layout") && settings.get("secret_layout").getAsBoolean()) {
            out.append("\n  The server's layout is secret: no address can be made here, so PDDs and address rules keep 1.12's");
        } else {
            try {
                final ClassLoader loader = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("sgcraft", "stargate_base")).getClass()
                    .getClassLoader();
                final boolean grouped = groupSymbols.size() > 1 || settings.has("groups") && !settings.getAsJsonObject("groups").isEmpty();
                addresses = new SgcraftAddresses(loader, layouts, grouped);
                out.append("\n  Addresses made by SGCraft's own GridReach; lasting addresses ")
                    .append(grouped ? "name the group (9 symbols)" : "of 8 symbols");
            } catch (final ReflectiveOperationException e) {
                out.append("\n  SGCraft's address code couldn't be found (").append(e).append("): addresses kept as 1.12's");
            }
        }
        out.append("\n  Read from: ").append(source == null ? "nothing (no 4.0 world given)" : gatesFile + ", " + config);
        converter.sgcraft.setup(dimension.toString(), legacyDimension, worlds, dimensionMap, players, offlineCopies, addresses);
        converter.sgcraft.properNames(properNames);
        if (addresses != null && world != null) {
            // Checked over the converted world: every 37th chunk of a 2000-chunk square
            final List<ChunkPos> sample = new ArrayList<>();
            for (int x = -1000; x <= 1000; x += 37) {
                for (int z = -1000; z <= 1000; z += 37) {
                    sample.add(new ChunkPos(x, z));
                }
            }
            for (final String line : addresses.verify(dimension.toString(), sample, Math.max(0, dimensionMap.indexOf(legacyDimension)))) {
                out.append("\n  Address check: ").append(line);
            }
        }
        return out.toString();
    }

    /**
     * Reads the regions' gate bases before any chunk is converted (for the kind of DHD each gate takes), straight from the region files;
     * only the chunks holding one are parsed.
     */
    private static void sgcraftPrescan(final LegacyWorldConverter converter, final Path regions, final String only) throws IOException {
        final byte[] needle = "gcewing.sg.sgbasete".getBytes(StandardCharsets.US_ASCII);
        try (var files = Files.list(regions)) {
            for (final Path file : files.filter(f -> f.getFileName().toString().endsWith(".mca")).sorted().toList()) {
                if (!only.isEmpty() && !List.of(only.split(",")).contains(file.getFileName().toString())) {
                    continue;
                }
                final byte[] data = Files.readAllBytes(file);
                if (data.length < 8192) {
                    continue;
                }
                for (int i = 0; i < 1024; i++) {
                    final int offset = ((data[i * 4] & 0xFF) << 16 | (data[i * 4 + 1] & 0xFF) << 8 | data[i * 4 + 2] & 0xFF) * 4096;
                    if (offset == 0 || offset + 5 > data.length) {
                        continue;
                    }
                    final int length = (data[offset] & 0xFF) << 24 | (data[offset + 1] & 0xFF) << 16 | (data[offset + 2] & 0xFF) << 8
                        | data[offset + 3] & 0xFF;
                    if (data[offset + 4] != 2 || length < 1 || offset + 4 + length > data.length) {
                        continue;
                    }
                    final byte[] raw;
                    try (var stream = new InflaterInputStream(new ByteArrayInputStream(data, offset + 5, length - 1))) {
                        raw = stream.readAllBytes();
                    }
                    if (indexOf(raw, needle) >= 0) {
                        try (var stream = new DataInputStream(new ByteArrayInputStream(raw))) {
                            converter.sgcraft.prescan(NbtIo.read(stream));
                        }
                    }
                }
            }
        }
    }

    private static int indexOf(final byte[] haystack, final byte[] needle) {
        outer:
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
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
