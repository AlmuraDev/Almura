/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.source;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * The content folders, as 1.12 laid them out: {@code <root>/assets/<namespace>/content/<type>/...} beside the namespace's
 * {@code blockstates}, {@code models} and {@code textures}. Read only; nothing here uses Minecraft's classes, so it can be tested against
 * the real content.
 *
 * <p>A content file's id is its path under its type folder without {@code .json}. Folders whose name starts with {@code _} hold
 * translations ({@code _translations}) and recipes ({@code _recipes}), not content.</p>
 */
public final class ContentSource {

    public static final String CONTENT = "content", TRANSLATIONS = "_translations", RECIPES = "_recipes";

    private final Path root;
    private final List<Problem> problems = new ArrayList<>();

    private ContentSource(final Path root) {
        this.root = root;
    }

    /**
     * @param root the folder holding {@code assets} ({@code <game>/assets/fs_5} on 1.12)
     */
    public static ContentSource at(final Path root) {
        return new ContentSource(root);
    }

    public Path root() {
        return this.root;
    }

    /**
     * @return The namespace folder ({@code assets/<namespace>}), holding {@code content}, {@code blockstates}, {@code models} and
     *     {@code textures}
     */
    public Path namespace(final String namespace) {
        return this.root.resolve("assets").resolve(namespace);
    }

    /**
     * @return The namespaces with a content folder, sorted
     */
    public List<String> namespaces() {
        final Path assets = this.root.resolve("assets");
        if (!Files.isDirectory(assets)) {
            return List.of();
        }
        try (Stream<Path> children = Files.list(assets)) {
            return children.filter(path -> Files.isDirectory(path.resolve(CONTENT)))
                .map(path -> path.getFileName().toString())
                .sorted()
                .toList();
        } catch (final IOException e) {
            this.problem(assets, "Couldn't list the namespaces: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * @return Every content file of a type in every namespace, by id
     */
    public List<ContentFile> files(final String type) {
        final List<ContentFile> files = new ArrayList<>();
        for (final String namespace : this.namespaces()) {
            final Path typeRoot = this.namespace(namespace).resolve(CONTENT).resolve(type);
            for (final Path file : this.jsonFiles(typeRoot, false)) {
                final JsonObject json = this.readObject(file);
                if (json != null) {
                    files.add(new ContentFile(namespace, type, idPath(typeRoot, file), file, json));
                }
            }
        }
        files.sort(Comparator.comparing(ContentFile::id));
        return files;
    }

    /**
     * @return Every translation file of a type in every namespace for a locale (such as {@code en_us})
     */
    public List<TranslationFile> translations(final String type, final String locale) {
        final List<TranslationFile> files = new ArrayList<>();
        for (final String namespace : this.namespaces()) {
            final Path typeRoot = this.namespace(namespace).resolve(CONTENT).resolve(type);
            for (final Path folder : this.specialFolders(typeRoot, TRANSLATIONS)) {
                final Path file = folder.resolve(locale + ".json");
                if (!Files.isRegularFile(file)) {
                    continue;
                }
                final JsonObject json = this.readObject(file);
                if (json == null) {
                    continue;
                }
                final Map<String, String> entries = new TreeMap<>();
                for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (entry.getValue().isJsonPrimitive()) {
                        entries.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
                // The content the translations are for sits in the folder above
                final String prefix = idPath(typeRoot, folder.getParent().resolve("x.json"));
                files.add(new TranslationFile(namespace, type, prefix.substring(0, prefix.length() - 1), file, entries));
            }
        }
        return files;
    }

    /**
     * @return Every recipe of a type in every namespace; a recipe's id is its folder's path under the type folder with its name
     */
    public List<ContentFile> recipes(final String type) {
        final List<ContentFile> files = new ArrayList<>();
        for (final String namespace : this.namespaces()) {
            final Path typeRoot = this.namespace(namespace).resolve(CONTENT).resolve(type);
            for (final Path folder : this.specialFolders(typeRoot, RECIPES)) {
                for (final Path file : this.jsonFiles(folder, true)) {
                    final JsonObject json = this.readObject(file);
                    if (json != null) {
                        final String folderPath = idPath(typeRoot, folder.getParent().resolve("x.json"));
                        final String name = file.getFileName().toString();
                        files.add(new ContentFile(namespace, type, type + "/" + folderPath.substring(0, folderPath.length() - 1)
                            + name.substring(0, name.length() - ".json".length()), file, json));
                    }
                }
            }
        }
        files.sort(Comparator.comparing(ContentFile::id));
        return files;
    }

    /**
     * @return Every file under the namespace's folder with an extension, relative to it with forward slashes (such as
     *     {@code models/block/alfalfabale.obj})
     */
    public List<String> assetFiles(final String namespace) {
        final Path folder = this.namespace(namespace);
        try (Stream<Path> walk = Files.walk(folder)) {
            return walk.filter(Files::isRegularFile)
                .map(path -> folder.relativize(path).toString().replace('\\', '/'))
                .filter(path -> !path.startsWith(CONTENT + "/"))
                .sorted()
                .toList();
        } catch (final IOException e) {
            this.problem(folder, "Couldn't list the files: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Reads a JSON object leniently, as 1.12's loader did (comments and trailing commas are allowed).
     *
     * @return The object, or null (with a problem noted) if the file isn't one
     */
    public JsonObject readObject(final Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            final JsonReader json = new JsonReader(reader);
            json.setStrictness(Strictness.LENIENT);
            final JsonElement element = JsonParser.parseReader(json);
            if (!element.isJsonObject()) {
                this.problem(file, "Not a JSON object");
                return null;
            }
            return element.getAsJsonObject();
        } catch (final IOException | JsonParseException | IllegalStateException e) {
            this.problem(file, "Couldn't read: " + e.getMessage());
            return null;
        }
    }

    public List<Problem> problems() {
        return List.copyOf(this.problems);
    }

    public void problem(final Path file, final String message) {
        this.problems.add(new Problem(file, message));
    }

    private List<Path> jsonFiles(final Path folder, final boolean includeSpecial) {
        if (!Files.isDirectory(folder)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(folder)) {
            return walk.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .filter(path -> includeSpecial || !inSpecialFolder(folder, path))
                .sorted()
                .toList();
        } catch (final IOException e) {
            this.problem(folder, "Couldn't list the files: " + e.getMessage());
            return List.of();
        }
    }

    private List<Path> specialFolders(final Path typeRoot, final String name) {
        if (!Files.isDirectory(typeRoot)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(typeRoot)) {
            return walk.filter(Files::isDirectory).filter(path -> path.getFileName().toString().equals(name)).sorted().toList();
        } catch (final IOException e) {
            this.problem(typeRoot, "Couldn't list the folders: " + e.getMessage());
            return List.of();
        }
    }

    private static boolean inSpecialFolder(final Path root, final Path file) {
        for (final Path part : root.relativize(file.getParent())) {
            if (part.toString().startsWith("_")) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code <typeRoot>/normal/ingredient/aniseseed.json} as {@code normal/ingredient/aniseseed}.
     */
    static String idPath(final Path typeRoot, final Path file) {
        final String relative = typeRoot.relativize(file).toString().replace('\\', '/');
        return relative.endsWith(".json") ? relative.substring(0, relative.length() - ".json".length()) : relative;
    }

    /**
     * A content file: the namespace, the type folder it's in, its id's path and what it holds.
     */
    public record ContentFile(String namespace, String type, String path, Path file, JsonObject json) {

        public String id() {
            return this.namespace + ":" + this.path;
        }
    }

    /**
     * A translation file: its entries are for the content in the folder {@code folder}, an id path prefix (such as
     * {@code normal/ingredient/}, ending with a slash, or empty for the type folder itself).
     */
    public record TranslationFile(String namespace, String type, String folder, Path file, Map<String, String> entries) {
    }

    public record Problem(Path file, String message) {

        @Override
        public String toString() {
            return this.file + ": " + this.message;
        }
    }
}
