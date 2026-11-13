/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.history;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * The notifications this player has received, kept in {@code config/almura/notifications.log} (one JSON line each, as on 1.12, so a 1.12
 * history carries over): the newest 500, the file rewritten without the oldest once it grows 100 past that.
 */
public final class NotificationHistory {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_NAME = "notifications.log";
    private static final int MAX_RECORDS = 500, COMPACT_SLACK = 100;

    private static final Deque<NotificationRecord> records = new ArrayDeque<>();
    private static int linesInFile;
    private static boolean loaded;

    private NotificationHistory() {
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("almura").resolve(FILE_NAME);
    }

    /**
     * Keeps a notification as received now, on the server playing. The welcome sent on every join is kept only when it differs from the last
     * one kept for that server.
     */
    public static void record(final String type, final String title, final String message) {
        load();
        final String server = serverName();
        if (isRepeatedWelcome(server, title, message)) {
            return;
        }
        final NotificationRecord record = new NotificationRecord(Instant.now(), type, server, title, message);
        records.addLast(record);
        while (records.size() > MAX_RECORDS) {
            records.removeFirst();
        }
        if (linesInFile >= MAX_RECORDS + COMPACT_SLACK) {
            rewrite();
        } else {
            append(record);
        }
    }

    /**
     * @return The notifications kept, newest first
     */
    public static List<NotificationRecord> records() {
        load();
        final List<NotificationRecord> newestFirst = new ArrayList<>(records.size());
        records.descendingIterator().forEachRemaining(newestFirst::add);
        return newestFirst;
    }

    public static void clear() {
        load();
        records.clear();
        rewrite();
    }

    private static boolean isRepeatedWelcome(final String server, final String title, final String message) {
        if (!isWelcome(title)) {
            return false;
        }
        final Iterator<NotificationRecord> iterator = records.descendingIterator();
        while (iterator.hasNext()) {
            final NotificationRecord record = iterator.next();
            if (record.server().equals(server) && isWelcome(record.title())) {
                return record.title().equals(title) && record.message().equals(message);
            }
        }
        return false;
    }

    private static boolean isWelcome(final String title) {
        final String plain = ChatFormatting.stripFormatting(title);
        return plain != null && plain.trim().startsWith("Welcome");
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        final Path file = file();
        if (!Files.exists(file)) {
            return;
        }
        try {
            final List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            linesInFile = lines.size();
            for (final String line : lines) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    final NotificationRecord record = NotificationRecord.fromJson(JsonParser.parseString(line).getAsJsonObject());
                    if (record != null) {
                        records.addLast(record);
                    }
                } catch (final JsonParseException | IllegalStateException | DateTimeException e) {
                    // A damaged line is skipped rather than losing the rest
                }
            }
            while (records.size() > MAX_RECORDS) {
                records.removeFirst();
            }
        } catch (final IOException e) {
            LOGGER.error("Unable to read the notification history from '{}'", file, e);
        }
    }

    private static void append(final NotificationRecord record) {
        final Path file = file();
        try {
            Files.createDirectories(file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(record.toJson().toString());
                writer.newLine();
            }
            linesInFile++;
        } catch (final IOException e) {
            LOGGER.error("Unable to write to the notification history '{}'", file, e);
        }
    }

    private static void rewrite() {
        final Path file = file();
        try {
            Files.createDirectories(file.getParent());
            // A temporary file swapped in, so a crash part way can't lose the history
            final Path temp = file.resolveSibling(FILE_NAME + ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                for (final NotificationRecord record : records) {
                    writer.write(record.toJson().toString());
                    writer.newLine();
                }
            }
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            linesInFile = records.size();
        } catch (final IOException e) {
            LOGGER.error("Unable to rewrite the notification history '{}'", file, e);
        }
    }

    private static String serverName() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isLocalServer()) {
            return "Singleplayer";
        }
        final ServerData server = minecraft.getCurrentServer();
        return server == null ? "" : server.name;
    }
}
