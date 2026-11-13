/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

final class AssetState {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(AssetState.class, new AssetStateSerializer())
            .registerTypeAdapter(AssetStateEntry.class, new AssetStateEntrySerializer())
            .setPrettyPrinting()
            .create();
    private static final JsonParser PARSER = new JsonParser();
    private static final String INDEX_FILE = "index.json";
    private static final String STATE_FILE = "state.json";
    final Map<String, AssetStateEntry> entries;

    private AssetState() {
        this(new LinkedHashMap<>());
    }

    AssetState(final Map<String, AssetStateEntry> entries) {
        this.entries = entries;
    }

    AssetStateEntry entry(final Path path) {
        final String id = com.google.common.io.Files.getNameWithoutExtension(path.getFileName().toString());
        return this.entries.computeIfAbsent(id, key -> {
            @Nullable final AssetStateEntry parent = this.entries.isEmpty() ? null : Iterables.getLast(this.entries.values());
            final AssetStateEntry entry = new AssetStateEntry(id, true);
            if (parent != null) {
                entry.previous = parent;
                parent.next = entry;
            }
            this.entries.put(id, entry);
            return entry;
        });
    }

    void write(final Logger logger, final Path path) {
        try {
            Files.write(path.resolve(INDEX_FILE), GSON.toJson(this).getBytes(StandardCharsets.UTF_8));
            final JsonObject states = new JsonObject();
            for (final Map.Entry<String, AssetStateEntry> entry : this.entries.entrySet()) {
                states.addProperty(entry.getKey(), entry.getValue().state.name());
            }
            Files.createDirectories(path.resolve(RootContentLoader.MANAGED_ASSETS));
            Files.write(path.resolve(RootContentLoader.MANAGED_ASSETS).resolve(STATE_FILE), GSON.toJson(states).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (final IOException e) {
            logger.error("Encountered an exception while saving content loader state", e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("entries", this.entries)
                .toString();
    }

    static AssetState resolve(final Logger logger, final Path path) {
        final AssetState state = create(logger, path.resolve(INDEX_FILE));
        final Path statePath = path.resolve(RootContentLoader.MANAGED_ASSETS).resolve(STATE_FILE);
        if (Files.exists(statePath)) {
            final JsonObject states = states(logger, path.resolve(RootContentLoader.MANAGED_ASSETS).resolve(STATE_FILE));
            for (final Map.Entry<String, JsonElement> element : states.entrySet()) {
                @Nullable final AssetStateEntry entry = state.entries.get(element.getKey());
                if (entry != null) {
                    entry.state = AssetStateEntry.State.STATES.get(element.getValue().getAsString());
                }
            }
        }
        return state;
    }

    private static AssetState create(final Logger logger, final Path path) {
        if (Files.exists(path)) {
            try {
                logger.debug("Loading content loader index from '{}'", path.toAbsolutePath());
                return GSON.fromJson(Files.newBufferedReader(path), AssetState.class);
            } catch (final IOException ignored) {
            }
        }
        return new AssetState();
    }

    private static JsonObject states(final Logger logger, final Path path) {
        if (Files.exists(path)) {
            try {
                logger.debug("Loading content loader state from '{}'", path.toAbsolutePath());
                return PARSER.parse(Files.newBufferedReader(path)).getAsJsonObject();
            } catch (final IOException ignored) {
            }
        }
        return new JsonObject();
    }
}
