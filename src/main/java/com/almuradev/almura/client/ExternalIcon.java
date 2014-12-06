/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class ExternalIcon extends MalisisIcon {

    public ExternalIcon(String texturePath) {
        super(texturePath);
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        String path = location.getResourcePath();
        final String[] tokens = path.split("/");
        final String domainName = tokens[0];
        return !domainName.equalsIgnoreCase("Minecraft") || super.load(manager, location);
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        final String domainName = location.getResourceDomain();
        if (domainName.equalsIgnoreCase("Minecraft")) {
            return super.load(manager, location);
        }
        final String textureName = location.getResourcePath();
        final Path texturePath = Paths.get(Filesystem.CONFIG_PATH.toString(), "images" + File.separator + textureName + ".png");

        final int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
        final boolean anisotropic = Minecraft.getMinecraft().gameSettings.anisotropicFiltering > 1.0F;

        try {
            BufferedImage[] textures = new BufferedImage[1 + mipmapLevels];
            textures[0] = ImageIO.read(Files.newInputStream(texturePath));
            loadSprite(textures, null, anisotropic);
            return false;
        } catch (RuntimeException e) {
            if (e.getMessage().equalsIgnoreCase("broken aspect ratio and not an animation")) {
                if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                    Almura.LOGGER
                            .error("Failed to load icon [" + textureName
                                   + ".png]. Aspect ratio is broken, make sure it is a power of 2 and has an equivalent width and height.",
                                   e);
                } else {
                    Almura.LOGGER.warn(
                            "Failed to load icon [" + textureName
                            + ".png]. Aspect ratio is broken, make sure it is a power of 2 and has an equivalent width and height.");
                }
            } else {
                if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                    Almura.LOGGER.error("Failed to load icon [" + textureName + ".png].", e);
                } else {
                    Almura.LOGGER.warn(
                            "Failed to load icon [" + textureName + ".png].");
                }
            }
        } catch (IOException e1) {
            if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                Almura.LOGGER.error("Failed to load icon [" + textureName + ".png].", e1);
            } else {
                Almura.LOGGER.warn(
                        "Failed to load icon [" + textureName + ".png].");
            }
        }

        return true;
    }
}
