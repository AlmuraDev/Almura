/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.history;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;

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
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Keeps a local log of the notifications this client received, so players can review them later.
 *
 * <p>Stored in {@code config/almura/notifications.log}, one JSON object per line, oldest first.</p>
 */
@SideOnly(Side.CLIENT)
@Singleton
public final class ClientNotificationHistory {

    private static final String FILE_NAME = "notifications.log";
    /** How many notifications are kept. */
    private static final int MAX_RECORDS = 500;
    /** Lines the file may grow past MAX_RECORDS before it's rewritten without the oldest ones. */
    private static final int COMPACT_SLACK = 100;

    private final Logger logger;
    private final Path file;
    private final Deque<NotificationRecord> records = new ArrayDeque<>();
    private int linesInFile;
    private boolean loaded;

    @Inject
    private ClientNotificationHistory(final Logger logger, @ConfigDir(sharedRoot = false) final Path configRoot) {
        this.logger = logger;
        this.file = configRoot.resolve(FILE_NAME);
    }

    /**
     * Records a notification as received now, on the current server.
     */
    public void record(final String type, final String title, final String message) {
        this.load();

        final String server = getServerName();
        if (this.isRepeatedWelcome(server, title, message)) {
            return;
        }

        final NotificationRecord record = new NotificationRecord(Instant.now(), type, server, title, message);
        this.records.addLast(record);
        while (this.records.size() > MAX_RECORDS) {
            this.records.removeFirst();
        }

        if (this.linesInFile >= MAX_RECORDS + COMPACT_SLACK) {
            this.rewrite();
        } else {
            this.append(record);
        }
    }

    /**
     * The welcome popup is sent on every join. It is only kept when it differs (title or message) from the last welcome kept for the
     * server.
     */
    private boolean isRepeatedWelcome(final String server, final String title, final String message) {
        if (!isWelcome(title)) {
            return false;
        }
        final Iterator<NotificationRecord> iterator = this.records.descendingIterator();
        while (iterator.hasNext()) {
            final NotificationRecord record = iterator.next();
            if (record.getServer().equals(server) && isWelcome(record.getTitle())) {
                return record.getTitle().equals(title) && record.getMessage().equals(message);
            }
        }
        return false;
    }

    private static boolean isWelcome(final String title) {
        final String plain = TextFormatting.getTextWithoutFormattingCodes(title);
        return plain != null && plain.trim().startsWith("Welcome");
    }

    /**
     * @return the kept notifications, newest first
     */
    public List<NotificationRecord> getRecords() {
        this.load();

        final ImmutableList.Builder<NotificationRecord> builder = ImmutableList.builder();
        final Iterator<NotificationRecord> iterator = this.records.descendingIterator();
        while (iterator.hasNext()) {
            builder.add(iterator.next());
        }
        return builder.build();
    }

    public void clear() {
        this.load();

        this.records.clear();
        this.rewrite();
    }

    private void load() {
        if (this.loaded) {
            return;
        }
        this.loaded = true;

        if (!Files.exists(this.file)) {
            return;
        }

        try {
            final List<String> lines = Files.readAllLines(this.file, StandardCharsets.UTF_8);
            this.linesInFile = lines.size();
            final JsonParser parser = new JsonParser();
            for (final String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    final NotificationRecord record = NotificationRecord.fromJson(parser.parse(line).getAsJsonObject());
                    if (record != null) {
                        this.records.addLast(record);
                    }
                } catch (final JsonParseException | IllegalStateException | DateTimeException e) {
                    // Skip a damaged line rather than losing the rest of the history
                }
            }
            while (this.records.size() > MAX_RECORDS) {
                this.records.removeFirst();
            }
        } catch (final IOException e) {
            this.logger.error("Unable to read the notification history from '{}'", this.file, e);
        }
    }

    private void append(final NotificationRecord record) {
        try {
            Files.createDirectories(this.file.getParent());
            try (final BufferedWriter writer = Files.newBufferedWriter(this.file, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                writer.write(record.toJson().toString());
                writer.newLine();
            }
            this.linesInFile++;
        } catch (final IOException e) {
            this.logger.error("Unable to write to the notification history '{}'", this.file, e);
        }
    }

    private void rewrite() {
        try {
            Files.createDirectories(this.file.getParent());
            // Write a temp file and swap it in, so a crash mid-write can't wipe the history
            final Path temp = this.file.resolveSibling(FILE_NAME + ".tmp");
            try (final BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                for (final NotificationRecord record : this.records) {
                    final JsonObject json = record.toJson();
                    writer.write(json.toString());
                    writer.newLine();
                }
            }
            Files.move(temp, this.file, StandardCopyOption.REPLACE_EXISTING);
            this.linesInFile = this.records.size();
        } catch (final IOException e) {
            this.logger.error("Unable to rewrite the notification history '{}'", this.file, e);
        }
    }

    private static String getServerName() {
        final Minecraft client = Minecraft.getMinecraft();
        if (client.isSingleplayer()) {
            return "Singleplayer";
        }
        final ServerData server = client.getCurrentServerData();
        return server == null ? "" : server.serverName;
    }
}
