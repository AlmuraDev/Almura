/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.convert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * The files of Almura's content pack, by their path in the pack ({@code assets/almura/models/...}, {@code data/almura/recipe/...}): generated
 * JSON, kept until it is read, or a file in the content folders served as it is.
 */
public final class ContentPack {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final TreeMap<String, Object> files = new TreeMap<>();

    void putJson(final String path, final JsonElement json) {
        this.files.put(path, json);
    }

    void putFile(final String path, final Path file) {
        this.files.put(path, file);
    }

    /**
     * A file made when it is first read (a texture cut from another), or left out if it can't be made.
     */
    void putGenerated(final String path, final Supplier<byte @Nullable []> contents) {
        this.files.put(path, new Generated(contents));
    }

    public boolean has(final String path) {
        return this.files.containsKey(path);
    }

    public @Nullable JsonElement json(final String path) {
        return this.files.get(path) instanceof final JsonElement json ? json : null;
    }

    public @Nullable Path file(final String path) {
        return this.files.get(path) instanceof final Path file ? file : null;
    }

    public NavigableSet<String> paths() {
        return Collections.unmodifiableNavigableSet(this.files.navigableKeySet());
    }

    /**
     * The files under a folder of the pack ({@code assets/almura/textures/}), with their paths.
     */
    public Map<String, Object> under(final String folder) {
        return Collections.unmodifiableMap(this.files.subMap(folder, true, folder + Character.MAX_VALUE, false));
    }

    /**
     * Opens a file of the pack.
     *
     * @return The file's contents, or null if the pack hasn't the file
     */
    public @Nullable InputStream open(final String path) throws IOException {
        final Object file = this.files.get(path);
        if (file instanceof final JsonElement json) {
            return new ByteArrayInputStream(GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
        }
        if (file instanceof final Path source) {
            return Files.newInputStream(source);
        }
        if (file instanceof final Generated generated) {
            final byte[] bytes = generated.get();
            return bytes == null ? null : new ByteArrayInputStream(bytes);
        }
        return null;
    }

    private static final class Generated {

        private final Supplier<byte @Nullable []> contents;
        private byte @Nullable [] bytes;
        private boolean made;

        Generated(final Supplier<byte @Nullable []> contents) {
            this.contents = contents;
        }

        synchronized byte @Nullable [] get() {
            if (!this.made) {
                this.bytes = this.contents.get();
                this.made = true;
            }
            return this.bytes;
        }
    }
}
