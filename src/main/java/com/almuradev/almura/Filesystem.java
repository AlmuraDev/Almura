/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Filesystem {

    public static final Path CONFIG_PATH = Paths.get("config" + File.separator + Almura.MOD_ID);
    public static final Path CONFIG_BACKGROUNDS_PATH = Paths.get(CONFIG_PATH.toString(), "backgrounds");
    public static final Path CONFIG_SETTINGS_PATH = Paths.get(CONFIG_PATH.toString(), "settings.yml");
    public static final Path CONFIG_MAPPINGS_PATH = Paths.get(CONFIG_PATH.toString(), "mappings.yml");
    public static final Path CONFIG_ENTITY_MAPPINGS_PATH = Paths.get(CONFIG_PATH.toString(), "entity_mappings.yml");
    public static final Path CONFIG_VERSION_PATH = Paths.get(CONFIG_PATH.toString(), Almura.PACK_VERSION);
    public static final Path CONFIG_YML_PATH = Paths.get(CONFIG_VERSION_PATH.toString(), "packs");
    public static final Path CONFIG_IMAGES_PATH = Paths.get(CONFIG_VERSION_PATH.toString(), "images");
    public static final Path CONFIG_GUI_SPRITESHEET_PATH = Paths.get(CONFIG_IMAGES_PATH.toString(), "gui.png");
    public static final Path CONFIG_GUI_LOGO_PATH = Paths.get(CONFIG_IMAGES_PATH.toString(), "almura.png");
    public static final Path CONFIG_GUI_RESTOKENICON_PATH = Paths.get(CONFIG_IMAGES_PATH.toString(), "almuracustom1_restoken.png");
    public static final Path CONFIG_MODELS_PATH = Paths.get(CONFIG_VERSION_PATH.toString(), "models");
    public static final Path CONFIG_ACCESSORIES_PATH = Paths.get(CONFIG_IMAGES_PATH.toString(), "accessories");

    public static DirectoryStream.Filter<Path> FILTER_MODEL_FILES_ONLY = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return !Files.isDirectory(entry) && (entry.getFileName().toString().endsWith(".shape"));
        }
    };

    static {
        if (Files.notExists(CONFIG_SETTINGS_PATH)) {
            try {
                Files.createDirectories(CONFIG_PATH);

                InputStream stream = Filesystem.class.getResourceAsStream("/config/settings.yml");
                Files.copy(stream, Filesystem.CONFIG_SETTINGS_PATH);
            } catch (Exception e) {
                throw new RuntimeException("Failed to copy over settings file.", e);
            }
        }

        if (Files.notExists(CONFIG_MAPPINGS_PATH)) {
            try {
                Files.createDirectories(CONFIG_PATH);

                InputStream stream = Filesystem.class.getResourceAsStream("/config/mappings.yml");
                Files.copy(stream, Filesystem.CONFIG_MAPPINGS_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy over mappings file.", e);
            }
        }

        if (Files.notExists(CONFIG_ENTITY_MAPPINGS_PATH)) {
            try {
                Files.createDirectories(CONFIG_PATH);

                InputStream stream = Filesystem.class.getResourceAsStream("/config/entity_mappings.yml");
                Files.copy(stream, Filesystem.CONFIG_ENTITY_MAPPINGS_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy over entity_mappings file.", e);
            }
        }

        try {
            Files.createDirectories(CONFIG_YML_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory [" + CONFIG_YML_PATH + "].", e);
        }

        try {
            Files.createDirectories(CONFIG_MODELS_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory [" + CONFIG_MODELS_PATH + "].", e);
        }

        if (Configuration.IS_CLIENT) {
            try {
                Files.createDirectories(CONFIG_IMAGES_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory [" + CONFIG_IMAGES_PATH + "].", e);
            }

            try {
                Files.createDirectories(CONFIG_BACKGROUNDS_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory [" + CONFIG_BACKGROUNDS_PATH + "].", e);
            }

            try {
                Files.createDirectories(CONFIG_ACCESSORIES_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory [" + CONFIG_ACCESSORIES_PATH + "].", e);
            }
        }
    }

    protected Filesystem() {
    }
}
