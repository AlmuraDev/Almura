/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Filesystem;
import com.google.common.base.Optional;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public abstract class AlmuraGui extends MalisisGui {

    protected static final GuiTexture TEXTURE_DEFAULT;
    protected final Optional<AlmuraGui> parent;

    static {
        try {
            final ResourceLocation loc = new ResourceLocation(Almura.MOD_ID, "textures/gui/gui.png");
            final Dimension dimension = Filesystem.getImageDimension(loc);
            TEXTURE_DEFAULT = new GuiTexture(loc, dimension.width, dimension.height);
        } catch (IOException e) {
            throw new RuntimeException("Failed to determine dimensions of gui sprite sheet!", e);
        }
    }

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added
     * @param parent the {@link AlmuraGui} that we came from
     */
    public AlmuraGui(AlmuraGui parent) {
        renderer.setDefaultTexture(TEXTURE_DEFAULT);
        this.parent = Optional.fromNullable(parent);
    }

    protected abstract void setup();

    /**
     * Displays the parent screen
     */
    protected void displayParent() {
        if (parent.isPresent()) {
            mc.displayGuiScreen(parent.get());
        } else {
            mc.displayGuiScreen(null);
        }
    }

    protected int getPaddedY(UIComponent component, int padding) {
        if (component == null) {
            return 0;
        }
        return component.getY() + component.getHeight() + padding;
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
