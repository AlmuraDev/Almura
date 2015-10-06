/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.util;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.Dimension;
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

public final class FileSystem {
    public static final Path CONFIG_PATH = Paths.get("config" + File.separator + Almura.MOD_ID);    
    public static final Path CONFIG_SETTINGS_PATH = Paths.get(CONFIG_PATH.toString(), "settings.yml");
    public static final Path CONFIG_MAPPINGS_PATH = Paths.get(CONFIG_PATH.toString(), "mappings.yml");
    public static final Path CONFIG_ENTITY_MAPPINGS_PATH = Paths.get(CONFIG_PATH.toString(), "entity_mappings.yml");
    public static final Path CONFIG_VERSION_PATH = Paths.get(CONFIG_PATH.toString(), Almura.PACK_VERSION);
    public static final Path CONFIG_YML_PATH = Paths.get(CONFIG_VERSION_PATH.toString(), "packs");
    public static final Path CONFIG_IMAGES_PATH = Paths.get(CONFIG_VERSION_PATH.toString(), "images");
    public static final Path CONFIG_BACKGROUNDS_PATH = Paths.get(CONFIG_IMAGES_PATH.toString(), "backgrounds");
    public static final Path CONFIG_GUI_SPRITESHEET_PATH = Paths.get(CONFIG_IMAGES_PATH.toString(), "gui.png");
    public static final Path CONFIG_GUI_LOGO_PATH = Paths.get(CONFIG_IMAGES_PATH.toString(), "almura.png");
    public static final Path CONFIG_GUI_RESTOKENICON_PATH = Paths.get(CONFIG_IMAGES_PATH.toString(), "almuracustom1_restoken.png");
    public static final Path CONFIG_MODELS_PATH = Paths.get(CONFIG_VERSION_PATH.toString(), "models");
    public static final Path CONFIG_ACCESSORIES_PATH = Paths.get(CONFIG_IMAGES_PATH.toString(), "accessories");

    public static final DirectoryStream.Filter<Path> FILTER_DIRECTORIES_ONLY = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return Files.isDirectory(entry);
        }
    };

    public static final DirectoryStream.Filter<Path> FILTER_FILES_ONLY = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return !Files.isDirectory(entry);
        }
    };

    public static final DirectoryStream.Filter<Path> FILTER_IMAGE_FILES_ONLY = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return entry.getFileName().toString().endsWith(".png") || entry.getFileName().toString().endsWith(".jpg");
        }
    };

    public static final DirectoryStream.Filter<Path> FILTER_YAML_FILES_ONLY = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return entry.getFileName().toString().endsWith(".yml");
        }
    };

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

                InputStream stream = FileSystem.class.getResourceAsStream("/config/settings.yml");
                Files.copy(stream, FileSystem.CONFIG_SETTINGS_PATH);
            } catch (Exception e) {
                throw new RuntimeException("Failed to copy over settings file.", e);
            }
        }

        if (Files.notExists(CONFIG_MAPPINGS_PATH)) {
            try {
                Files.createDirectories(CONFIG_PATH);

                InputStream stream = FileSystem.class.getResourceAsStream("/config/mappings.yml");
                Files.copy(stream, FileSystem.CONFIG_MAPPINGS_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy over mappings file.", e);
            }
        }

        if (Files.notExists(CONFIG_ENTITY_MAPPINGS_PATH)) {
            try {
                Files.createDirectories(CONFIG_PATH);

                InputStream stream = FileSystem.class.getResourceAsStream("/config/entity_mappings.yml");
                Files.copy(stream, FileSystem.CONFIG_ENTITY_MAPPINGS_PATH);
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

    @SideOnly(Side.CLIENT)
    public static ResourceLocation registerTexture(String modid, String key, Path path) throws IOException {
        return registerTexture(modid, key, Files.newInputStream(path));
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation registerTexture(String modid, String key, String path) throws IOException {
        return registerTexture(modid, key, FileSystem.class.getResourceAsStream(path));
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation registerTexture(String modid, String key, InputStream stream) throws IOException {
        final BufferedImage image = ImageIO.read(stream);
        final ResourceLocation location = new ResourceLocation(modid, key);
        Minecraft.getMinecraft().getTextureManager().loadTexture(location, new BufferedTexture(location, image));
        return location;
    }

    @SideOnly(Side.CLIENT)
    private static final class BufferedTexture extends SimpleTexture {

        private final BufferedImage image;

        public BufferedTexture(ResourceLocation key, BufferedImage image) {
            super(key);
            this.image = image;
        }

        @Override
        public void loadTexture(IResourceManager p_110551_1_) throws IOException {
            TextureUtil.uploadTextureImageAllocate(getGlTextureId(), image, false, false);
        }
    }
}
