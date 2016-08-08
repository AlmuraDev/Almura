/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public final class FileSystem {

    public static final Path PATH_CONFIG = Paths.get(".").resolve("config").resolve(Almura.PLUGIN_ID);
    public static final Path PATH_CONFIG_CLIENT = PATH_CONFIG.resolve("client.conf");
    public static final Path PATH_CONFIG_PACKS = PATH_CONFIG.resolve("packs");

    public static void init() {
        if (Files.notExists(PATH_CONFIG_PACKS)) {
            try {
                Files.createDirectories(PATH_CONFIG_PACKS);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create [" + PATH_CONFIG_PACKS + "]!", e);
            }
        }
    }
    // Gui Utilities

    @SideOnly(Side.CLIENT)
    public static ResourceLocation registerTexture(String modid, String key, String path) throws IOException {
        return registerTexture(modid, key, FileSystem.class.getResourceAsStream(path));
    }

    @SideOnly(Side.CLIENT)
    private static ResourceLocation registerTexture(String modid, String key, InputStream stream) throws IOException {
        final BufferedImage image = ImageIO.read(stream);
        stream.close();
        final ResourceLocation location = new ResourceLocation(modid, key);
        Minecraft.getMinecraft().getTextureManager().loadTexture(location, new BufferedTexture(location, image));
        return location;
    }

    @SideOnly(Side.CLIENT)
    private static final class BufferedTexture extends SimpleTexture {

        private final BufferedImage image;

        BufferedTexture(ResourceLocation key, BufferedImage image) {
            super(key);
            this.image = image;
        }

        @Override
        public void loadTexture(IResourceManager p_110551_1_) throws IOException {
            TextureUtil.uploadTextureImageAllocate(getGlTextureId(), image, false, false);
        }
    }
}
