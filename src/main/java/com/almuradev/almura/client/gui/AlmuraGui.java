/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Filesystem;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public class AlmuraGui extends MalisisGui {

    protected static final GuiTexture TEXTURE_DEFAULT;

    static {
        try {
            final ResourceLocation loc = new ResourceLocation(Almura.MOD_ID, "textures/gui/gui.png");
            final Dimension dimension = Filesystem.getImageDimension(loc);
            TEXTURE_DEFAULT = new GuiTexture(loc, dimension.width, dimension.height);
        } catch (IOException e) {
            throw new RuntimeException("Failed to determine dimensions of gui sprite sheet!", e);
        }
    }

    public AlmuraGui() {
        renderer.setDefaultTexture(TEXTURE_DEFAULT);
    }

    /**
     * Snips out a {@link net.malisis.core.client.gui.icon.GuiIcon} based on the texture coordinates and size
     * @param x in pixels
     * @param y in pixels
     * @param width in pixels
     * @param height in pixels
     * @return the icon
     */
    protected static GuiIcon getIcon(int x, int y, int width, int height) {
        return TEXTURE_DEFAULT.getIcon(x, y, width, height);
    }
}
