/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.renderer;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.pack.IPackObject;
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

public class PackIcon extends MalisisIcon {

    private final IPackObject object;

    public PackIcon(IPackObject object, String textureName) {
        super(Almura.MOD_ID.toLowerCase() + ":packs/" + object.getPack().getName() + "/" + textureName);
        this.object = object;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        String path = location.getResourcePath();
        //Hack to remove forced item prefix
        //TODO Figure out how to make this un-necessary one day...
        String[] split = location.getResourcePath().split("textures/items/");
        if (split.length > 1) {
            path = split[1];
        }

        if (path.startsWith("packs")) {
            final String[] tokens = path.split("/");
            final String packName = tokens[1];
            final String textureName = tokens[2];
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
                                .error("Failed to load icon [" + textureName + ".png] used by [" + object.getIdentifier() + "] requested by pack ["
                                       + packName + "]. Aspect ratio is broken, make sure it is a power of 2 and has an equivalent width and height.",
                                       e);
                    } else {
                        Almura.LOGGER.warn(
                                "Failed to load icon [" + textureName + ".png] used by [" + object.getIdentifier() + "] requested by pack ["
                                + packName + "]. Aspect ratio is broken, make sure it is a power of 2 and has an equivalent width and height.");
                    }
                } else {
                    if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                        Almura.LOGGER
                                .error("Failed to load icon [" + textureName + ".png] used by [" + object.getIdentifier() + "] requested by pack ["
                                       + packName + "]", e);
                    } else {
                        Almura.LOGGER.warn(
                                "Failed to load icon [" + textureName + ".png] used by [" + object.getIdentifier() + "] requested by pack ["
                                + packName + "]");
                    }
                }
            } catch (IOException e1) {
                if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                    Almura.LOGGER.error("Failed to load icon [" + textureName + ".png] used by [" + object.getIdentifier() + "] requested by pack ["
                                        + packName + "]", e1);
                } else {
                    Almura.LOGGER.warn(
                            "Failed to load icon [" + textureName + ".png] used by [" + object.getIdentifier() + "] requested by pack [" + packName
                            + "]");
                }
            }
        }

        return true;
    }
}
