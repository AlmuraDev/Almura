/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

final class JarSearchEntry extends SearchEntry {

    private static final JsonParser PARSER = new JsonParser();
    private final URI uri;
    final AssetStateEntry state;
    private final Set<String> modified = new HashSet<>();
    private final Set<String> removed = new HashSet<>();

    JarSearchEntry(final AssetStateEntry state, final URI uri) {
        super(uri.toString());
        this.state = state;
        this.state.source = this;
        this.uri = uri;
    }

    private void accept(final Logger logger, final Function<FileSystem, Path> path, final Consumer<Path> consumer) {
        try (final FileSystem fs = FileSystems.newFileSystem(this.uri, Collections.emptyMap())) {
            consumer.accept(path.apply(fs));
        } catch (final IOException e) {
            logger.error("Encountered an exception while creating a file system to read content", e);
        }
    }

    void loadJson(final Logger logger) {
        this.accept(logger, fs -> fs.getPath("/assets.json"), path -> {
            final JsonElement element;
            try {
                element = PARSER.parse(Files.newBufferedReader(path));
            } catch (final IOException e) {
                logger.error("Encountered an exception while parsing asset JSON", e);
                return;
            }
            final JsonObject object = element.getAsJsonObject();
            @Nullable final JsonElement modified = object.get("modified");
            if (modified != null) {
                for (final JsonElement entry : (JsonArray) modified) {
                    this.modified.add(entry.getAsString());
                }
            }
            @Nullable final JsonElement removed = object.get("removed");
            if (removed != null) {
                for (final JsonElement entry : (JsonArray) removed) {
                    this.removed.add(entry.getAsString());
                }
            }
        });
    }

    void filter(final JarSearchEntry that) {
        this.modified.removeAll(that.modified);
        this.removed.removeAll(that.modified);
        this.removed.addAll(that.removed);
        this.modified.removeAll(that.removed);
    }

    boolean canRevert(final Logger logger) {
        if (this.state.is(AssetStateEntry.State.ROLLED_BACK)) {
            logger.debug("Skipping rollback on asset JAR '{}' - already performed", this.state.id);
            return false;
        }
        return !this.state.enabled && this.state.previous != null;
    }

    boolean canCopy(final Logger logger) {
        if (!this.state.enabled && this.state.is(AssetStateEntry.State.ROLLED_BACK)) {
            logger.debug("Skipping asset JAR '{}' - rollback performed", this.state.id);
            return false;
        }
        if (this.state.is(AssetStateEntry.State.EXTRACTED)) {
            logger.debug("Skipping asset JAR '{}' - already extracted", this.state.id);
            return false;
        }
        return true;
    }

    void copy(final Logger logger, final Path target) {
        this.accept(logger, fs -> fs.getPath("/"), path -> {
            logger.debug("Copying assets from '{}' in '{}' to '{}'", path, this.description, target.toAbsolutePath());
            try {
                Files.walkFileTree(path, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
                        if (file.getFileName().toString().equals("assets.json")) {
                            return FileVisitResult.CONTINUE;
                        }
                        final String name = string(file.toString());
                        if (JarSearchEntry.this.removed.contains(name)) {
                            logger.debug("    Skipping '{}' - marked as removed", file);
                            return FileVisitResult.CONTINUE;
                        }
                        if (!JarSearchEntry.this.modified.contains(name)) {
                            logger.debug("    Skipping '{}' - will be provided by another source", file);
                            return FileVisitResult.CONTINUE;
                        }
                        final Path to = target.resolve(name).toAbsolutePath(); // must use toString to avoid PME and ADE
                        logger.debug("    Copying '{}'", file);
                        if (Files.notExists(to.getParent())) {
                            Files.createDirectories(to.getParent());
                        }
                        Files.copy(file, to, StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(final Path file, final IOException exception) throws IOException {
                        if (exception != null && !(exception instanceof FileNotFoundException)) {
                            logger.error("Encountered an exception while extracting file '{}'", file, exception);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(final Path directory, final IOException exception) throws IOException {
                        if (exception != null && !(exception instanceof FileNotFoundException)) {
                            logger.error("Encountered an exception while extracting file '{}'", directory, exception);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                for (final String removed : this.removed) {
                    final Path to = target.resolve(string(removed)).toAbsolutePath();
                    if (Files.exists(to)) {
                        logger.debug("    Removing '{}' - requested by '{}'", to, this);
                        Files.delete(to);
                    } else {
                        logger.debug("    Could not remove '{}' - does not exist", to);
                    }
                }
            } catch (final IOException e) {
                logger.error("Encountered an exception while visiting '{}' to copy assets", path, e);
            }
        });
        this.state.to(AssetStateEntry.State.EXTRACTED);
    }

    void revert(final Logger logger, final Path target) {
        @Nullable final AssetStateEntry revertPoint = this.state.previous;
        if (revertPoint == null) {
            return;
        }
        this.state.to(AssetStateEntry.State.ROLLED_BACK);
        revertPoint.source.accept(logger, fs -> fs.getPath("/"), parent -> {
            this.accept(logger, fs -> fs.getPath("/"), path -> {
                logger.debug("    Rolling '{}' back to '{}'", this.state.id, revertPoint.id);
                try {
                    Files.walkFileTree(path, new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
                            if (file.getFileName().toString().equals("assets.json")) {
                                return FileVisitResult.CONTINUE;
                            }
                            final String name = string(file.toString());
                            if (!JarSearchEntry.this.modified.contains(name)) {
                                return FileVisitResult.CONTINUE;
                            }
                            logger.debug("        Reverting '{}' to version in rollback target '{}'", file, revertPoint.id);
                            final Path source = parent.resolve(string(file.toString()));
                            final Path to = target.resolve(name).toAbsolutePath(); // must use toString to avoid PME and ADE
                            if (Files.notExists(to.getParent())) {
                                Files.createDirectories(to.getParent());
                            }
                            Files.copy(source, to, StandardCopyOption.REPLACE_EXISTING);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(final Path file, final IOException exception) throws IOException {
                            if (exception != null && !(exception instanceof FileNotFoundException)) {
                                logger.error("Encountered an exception while extracting file '{}'", file, exception);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(final Path directory, final IOException exception) throws IOException {
                            if (exception != null && !(exception instanceof FileNotFoundException)) {
                                logger.error("Encountered an exception while extracting file '{}'", directory, exception);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });

                    for (final String removed : this.removed) {
                        final Path source = parent.resolve(string(removed));
                        final Path to = target.resolve(string(removed)).toAbsolutePath();
                        if (!Files.exists(to)) {
                            logger.debug("        Restoring '{}' to '{}'", source, to);
                            Files.copy(source, to);
                        } else {
                            logger.debug("        Could not restore '{}' - was not removed", to);
                        }
                    }
                } catch (final IOException e) {
                    logger.error("Encountered an exception while rolling back '{}'", this.state.id, e);
                }
            });
        });
    }

    void remove(final Logger logger, final Path target) {
        for (final String remove : this.removed) {
            final Path path = target.resolve(remove);
            if (Files.exists(path)) {
                try {
                    Files.delete(path);
                } catch (final IOException e) {
                    logger.error("Encountered an exception while removing '{}'", path);
                }
            }
        }
    }

    private static String string(final String string) {
        if (string.charAt(0) == '/') {
            return string.substring(1);
        }
        return string;
    }
}
