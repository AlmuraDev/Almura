/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Filesystem {

    public static final Path ASSETS_PATH = Paths.get("assets" + File.separator + Almura.MOD_ID.toLowerCase());

    public static final Path ASSETS_MODELS_PATH = Paths.get(ASSETS_PATH.toString(), "models");
    public static final Path ASSETS_MODELS_BLOCKS_PATH = Paths.get(ASSETS_MODELS_PATH.toString(), "blocks");
    public static final Path ASSETS_MODELS_BLOCKS_SHAPES_PATH = Paths.get(ASSETS_MODELS_BLOCKS_PATH.toString(), "shapes");

    public static final Path ASSETS_TEXTURES_PATH = Paths.get(ASSETS_PATH.toString(), "textures");
    public static final Path ASSETS_TEXTURES_BLOCKS_PATH = Paths.get(ASSETS_TEXTURES_PATH.toString(), "blocks");
    public static final Path ASSETS_TEXTURES_ITEMS_PATH = Paths.get(ASSETS_TEXTURES_PATH.toString(), "items");
    public static final Path ASSETS_TEXTURES_BLOCKS_SMPS_PATH = Paths.get(ASSETS_TEXTURES_BLOCKS_PATH.toString(), "smps");
    public static final Path ASSETS_TEXTURES_ITEMS_SMPS_PATH = Paths.get(ASSETS_TEXTURES_ITEMS_PATH.toString(), "smps");

    public static final Path CONFIG_PATH = Paths.get("config" + File.separator + Almura.MOD_ID.toLowerCase());
    public static final Path CONFIG_SETTINGS_PATH = Paths.get(CONFIG_PATH.toString(), "settings.yml");
    public static final Path CONFIG_SMPS_PATH = Paths.get(CONFIG_PATH.toString(), "smps");

    static {
        if (Files.notExists(CONFIG_SETTINGS_PATH)) {
            try {
                Files.createDirectories(CONFIG_PATH);

                InputStream stream = Filesystem.class.getResourceAsStream("/config/settings.yml");
                Files.copy(stream, Filesystem.CONFIG_SETTINGS_PATH);
            } catch (Exception e) {
                throw new RuntimeException("Failed to copy over configuration files!", e);
            }
        }

        try {
            Files.createDirectories(CONFIG_SMPS_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory " + CONFIG_SMPS_PATH, e);
        }

        if (Configuration.IS_CLIENT) {
            if (Files.exists(ASSETS_MODELS_BLOCKS_SHAPES_PATH)) {
                for (Path file : getPaths(ASSETS_MODELS_BLOCKS_SHAPES_PATH, "*.shape")) {
                    try {
                        Files.deleteIfExists(file);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to remove " + file + " from " + ASSETS_MODELS_BLOCKS_SHAPES_PATH, e);
                    }
                }
            } else {
                try {
                    Files.createDirectories(ASSETS_MODELS_BLOCKS_SHAPES_PATH);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create directory " + ASSETS_MODELS_BLOCKS_SHAPES_PATH, e);
                }
            }

            if (Files.exists(ASSETS_TEXTURES_BLOCKS_SMPS_PATH)) {
                for (Path file : getPaths(ASSETS_TEXTURES_BLOCKS_SMPS_PATH, "*.png")) {
                    try {
                        Files.deleteIfExists(file);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to remove " + file + " from " + ASSETS_TEXTURES_BLOCKS_SMPS_PATH, e);
                    }
                }
            } else {
                try {
                    Files.createDirectories(ASSETS_TEXTURES_BLOCKS_SMPS_PATH);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create directory " + ASSETS_TEXTURES_BLOCKS_SMPS_PATH, e);
                }
            }

            if (Files.exists(ASSETS_TEXTURES_ITEMS_SMPS_PATH)) {
                for (Path file : getPaths(ASSETS_TEXTURES_ITEMS_SMPS_PATH, "*.png")) {
                    try {
                        Files.deleteIfExists(file);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to remove " + file + " from " + ASSETS_TEXTURES_ITEMS_SMPS_PATH, e);
                    }
                }
            } else {
                try {
                    Files.createDirectories(ASSETS_TEXTURES_ITEMS_SMPS_PATH);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create directory " + ASSETS_TEXTURES_ITEMS_SMPS_PATH, e);
                }
            }
        }
    }

    protected Filesystem() {
    }

    public static Collection<URL> getURLs(Path path, String blob) {
        final List<URL> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, blob)) {
            for (Path entry : stream) {
                result.add(entry.toUri().toURL());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Collections.unmodifiableCollection(result);
    }

    public static Collection<Path> getPaths(Path path, String blob) {
        final List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, blob)) {
            for (Path entry : stream) {
                result.add(entry);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Collections.unmodifiableCollection(result);
    }

    public static void writeTo(String fileName, InputStream stream, Path newDir) throws IOException {
        final ReadableByteChannel read = Channels.newChannel(stream);

        final FileOutputStream write = new FileOutputStream(new File(newDir.toFile(), fileName));
        write.getChannel().transferFrom(read, 0, Long.MAX_VALUE);
        write.close();
    }
}
