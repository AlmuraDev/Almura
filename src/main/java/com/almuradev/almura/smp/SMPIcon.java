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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class SMPIcon extends MalisisIcon {
    public SMPIcon(String textureName) {
        super(textureName);
    }

    public SMPIcon(String packName, String textureName) {
        super("smp/" + packName + "-" +textureName);
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        if (location.getResourcePath().startsWith("smp")) {
            //almura:smp/kfood_core/food.png
            final String[] tokens = location.getResourcePath().split("/")[1].split("-");
            final String packName = tokens[0];
            final String textureName = tokens[1];

            final Path texturePath = Paths.get(Filesystem.CONFIG_SMPS_PATH.toString(), packName + File.separator + textureName + ".png");

            int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
            boolean anisotropic = Minecraft.getMinecraft().gameSettings.anisotropicFiltering > 1.0F;

            try {
                BufferedImage[] textures = new BufferedImage[1 + mipmapLevels];
                textures[0] = ImageIO.read(Files.newInputStream(texturePath));
                loadSprite(textures, null, anisotropic);
                return false;
            } catch (RuntimeException e) {
                Almura.LOGGER.error("Failed to load icon [" + textureName + ".png] in pack [" + packName + "]", e);
            } catch (IOException ignored) {}
        }

        return true;
    }
}
