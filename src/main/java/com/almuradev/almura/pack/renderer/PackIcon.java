/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.renderer;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.util.FileSystem;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class PackIcon extends MalisisIcon {

    private final IPackObject object;

    public PackIcon(IPackObject object, String textureName) {
        super(Almura.MOD_ID.toLowerCase() + ":" + textureName);
        this.object = object;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        String textureName = location.getResourcePath();

        final Path texturePath = Paths.get(FileSystem.CONFIG_VERSION_PATH.toString(), "images" + File.separator + textureName + ".png");

        final int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
        final boolean anisotropic = Minecraft.getMinecraft().gameSettings.anisotropicFiltering > 1.0F;

        InputStream stream = null;
        try {
            BufferedImage[] textures = new BufferedImage[1 + mipmapLevels];
            stream = Files.newInputStream(texturePath);
            textures[0] = ImageIO.read(stream);
            loadSprite(textures, null, anisotropic);
            return false;
        } catch (RuntimeException e) {
            if (e.getMessage().equalsIgnoreCase("broken aspect ratio and not an animation")) {
                if (Configuration.DEBUG_ALL || Configuration.DEBUG_PACKS) {
                    Almura.LOGGER
                            .error("Failed to load icon [" + textureName + ".png] in [" + FileSystem.CONFIG_IMAGES_PATH + "]  used by [" + object
                                            .getIdentifier() + "] requested by pack ["
                                            + object.getPack().getName()
                                            + "]. Aspect ratio is broken, make sure it is a power of 2 and has an equivalent width and height.",
                                    e);
                } else {
                    Almura.LOGGER.warn(
                            "Failed to load icon [" + textureName + ".png] in [" + FileSystem.CONFIG_IMAGES_PATH + "] used by [" + object
                                    .getIdentifier() + "] requested by pack ["
                                    + object.getPack().getName()
                                    + "]. Aspect ratio is broken, make sure it is a power of 2 and has an equivalent width and height.");
                }
            } else {
                if (Configuration.DEBUG_ALL || Configuration.DEBUG_PACKS) {
                    Almura.LOGGER
                            .error("Failed to load icon [" + textureName + ".png] in [" + FileSystem.CONFIG_IMAGES_PATH + "] used by [" + object
                                    .getIdentifier() + "] requested by pack ["
                                    + object.getPack().getName() + "]", e);
                } else {
                    Almura.LOGGER.warn(
                            "Failed to load icon [" + textureName + ".png] in [" + FileSystem.CONFIG_IMAGES_PATH + "] used by [" + object
                                    .getIdentifier() + "] requested by pack ["
                                    + object.getPack().getName() + "]");
                }
            }
        } catch (IOException e1) {
            if (Configuration.DEBUG_ALL || Configuration.DEBUG_PACKS) {
                Almura.LOGGER.error("Failed to load icon [" + textureName + ".png] in [" + FileSystem.CONFIG_IMAGES_PATH + "] used by [" + object
                        .getIdentifier() + "] requested by pack ["
                        + object.getPack().getName() + "]", e1);
            } else {
                Almura.LOGGER.warn(
                        "Failed to load icon [" + textureName + ".png] in [" + FileSystem.CONFIG_IMAGES_PATH + "] used by [" + object
                                .getIdentifier() + "] requested by pack [" + object
                                .getPack().getName()
                                + "]");
            }

        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
            }
        }
        return true;
    }
}
