/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.client.BufferedTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
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
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class Filesystem {

    public static final Path CONFIG_PATH = Paths.get("config" + File.separator + Almura.MOD_ID);
    public static final Path CONFIG_BACKGROUNDS_PATH = Paths.get(CONFIG_PATH.toString(), "backgrounds");
    public static final Path CONFIG_SETTINGS_PATH = Paths.get(CONFIG_PATH.toString(), "settings.yml");
    public static final Path CONFIG_MAPPINGS_PATH = Paths.get(CONFIG_PATH.toString(), "mappings.yml");
    public static final Path CONFIG_VERSION_PATH = Paths.get(CONFIG_PATH.toString(), Almura.PACK_VERSION);
    public static final Path CONFIG_YML_PATH = Paths.get(CONFIG_VERSION_PATH.toString(), "packs");
    public static final Path CONFIG_IMAGES_PATH = Paths.get(CONFIG_VERSION_PATH.toString(), "images");
    public static final Path CONFIG_MODELS_PATH = Paths.get(CONFIG_VERSION_PATH.toString(), "models");

    public static DirectoryStream.Filter<Path> DIRECTORIES_ONLY_FILTER = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return Files.isDirectory(entry);
        }
    };

    public static DirectoryStream.Filter<Path> FILES_ONLY_FILTER = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return !Files.isDirectory(entry);
        }
    };

    public static DirectoryStream.Filter<Path> ICON_FILES_ONLY_FILTER = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return entry.getFileName().endsWith(".png") || entry.getFileName().endsWith(".jpg");
        }
    };

    public static DirectoryStream.Filter<Path> YML_FILES_ONLY_FILTER = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return !Files.isDirectory(entry) && (entry.getFileName().toString().endsWith(".yml"));
        }
    };

    public static DirectoryStream.Filter<Path> MODEL_FILES_ONLY_FILTER = new DirectoryStream.Filter<Path>() {
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
                throw new RuntimeException("Failed to copy over mapping file.", e);
            }
        }

        try {
            Files.createDirectories(CONFIG_YML_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory [" + CONFIG_YML_PATH + "].", e);
        }

        if (Configuration.IS_CLIENT) {
            try {
                Files.createDirectories(CONFIG_IMAGES_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory [" + CONFIG_IMAGES_PATH + "].", e);
            }

            try {
                Files.createDirectories(CONFIG_MODELS_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory [" + CONFIG_MODELS_PATH + "].", e);
            }

            try {
                Files.createDirectories(CONFIG_BACKGROUNDS_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory [" + CONFIG_BACKGROUNDS_PATH + "].", e);
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

    public static Dimension getImageDimension(InputStream stream) throws IOException {
        Dimension dim = null;

        ImageInputStream img = ImageIO.createImageInputStream(stream);
        try {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(img);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(img);
                    dim = new Dimension(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            }
        } finally {
            if (img != null) {
                img.close();
            }
        }

        return dim;
    }

    public static Dimension getImageDimension(Path path) throws IOException {
        return getImageDimension(Files.newInputStream(path));
    }

    public static Dimension getImageDimension(ResourceLocation location) throws IOException {
        return getImageDimension(Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream());
    }

    public static ResourceLocation registerTexture(String modid, String key, Path path) throws IOException {
        final BufferedImage image = ImageIO.read(Files.newInputStream(path));
        final ResourceLocation location = new ResourceLocation(modid, key);
        Minecraft.getMinecraft().getTextureManager().loadTexture(location, new BufferedTexture(location, image));
        return location;
    }
}
