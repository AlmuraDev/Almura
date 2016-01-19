/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

@SideOnly(Side.CLIENT)
public class ExternalIcon extends MalisisIcon {

    private final Path root;

    public ExternalIcon(String key, Path root) {
        super(key);
        this.root = root;
    }

    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    public boolean load(IResourceManager manager, ResourceLocation location) {
        final Path texturePath = Paths.get(root.toString(), location.getResourcePath() + ".png");

        InputStream stream = null;
        try {
            BufferedImage[] textures = new BufferedImage[1 + Minecraft.getMinecraft().gameSettings.mipmapLevels];
            stream = Files.newInputStream(texturePath);
            textures[0] = ImageIO.read(stream);
            loadSprite(textures, null, Minecraft.getMinecraft().gameSettings.anisotropicFiltering > 1.0F);
            return false;
        } catch (RuntimeException | IOException ignored) {
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }

        return true;
    }
}
