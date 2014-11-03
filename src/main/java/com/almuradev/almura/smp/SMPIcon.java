/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.smp;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Filesystem;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

public class SMPIcon extends MalisisIcon {

    private final SMPPack pack;

    public SMPIcon(SMPPack pack, String textureName) {
        super(pack.getName() + "-" + textureName);
        this.pack = pack;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(Paths.get(Filesystem.CONFIG_SMPS_PATH.toString(), pack.getName() + ".smp").toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (zipFile == null) {
            return true;
        }

        InputStream textureStream = null;

        try {
            textureStream = zipFile.getInputStream(new ZipEntry(getIconName().split("-")[1] + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (textureStream == null) {
            return true;
        }

        int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
        boolean anisotropic = Minecraft.getMinecraft().gameSettings.anisotropicFiltering > 1.0F;

        try {
            BufferedImage[] textures = new BufferedImage[1 + mipmapLevels];
            textures[0] = ImageIO.read(textureStream);
            loadSprite(textures, null, anisotropic);
            return false;
        } catch (RuntimeException e) {
            Almura.LOGGER.error("Failed to load icon [" + getIconName().split("-")[1] + ".png] in pack [" + pack.getName() + "]", e);
        } catch (IOException ignored) {}
        return true;
    }
}
