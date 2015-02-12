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
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

public abstract class AlmuraGui extends MalisisGui {
    public static final String COMPASS_CHARACTERS = "S|.|W|.|N|.|E|.|";
    public static final GuiIcon ICON_EMPTY;
    public static final GuiIcon ICON_BAR;
    public static final GuiIcon ICON_HEART;
    public static final GuiIcon ICON_ARMOR;
    public static final GuiIcon ICON_HUNGER;
    public static final GuiIcon ICON_STAMINA;
    public static final GuiIcon ICON_XP;
    public static final GuiIcon ICON_PLAYER;
    public static final GuiIcon ICON_COMPASS;
    public static final GuiIcon ICON_MAP;
    public static final GuiIcon ICON_WORLD;
    public static final GuiIcon ICON_CLOCK;
    public static final GuiIcon ICON_CLOSE_NORMAL;
    public static final GuiIcon ICON_CLOSE_HOVER;
    public static final GuiIcon ICON_CLOSE_PRESSED;
    public static final GuiTexture TEXTURE_DEFAULT;
    protected final Optional<AlmuraGui> parent;

    static {
        try {
            final ResourceLocation loc = Filesystem.registerTexture(Almura.MOD_ID, "textures/gui/gui.png", Filesystem.CONFIG_GUI_SPRITESHEET_PATH);
            final Dimension dim = Filesystem.getImageDimension(Filesystem.CONFIG_GUI_SPRITESHEET_PATH);
            TEXTURE_DEFAULT = new GuiTexture(loc, dim.width, dim.height);
        } catch (IOException e) {
            throw new RuntimeException("Failed load gui sprite sheet.", e);
        }

        ICON_EMPTY = getIcon(283, 141, 1, 1);
        ICON_BAR = getIcon(0, 126, 256, 14);
        ICON_HEART = getIcon(149, 62, 26, 26);
        ICON_ARMOR = getIcon(64, 63, 20, 27);
        ICON_HUNGER = getIcon(198, 96, 28, 29);
        ICON_STAMINA = getIcon(99, 93, 32, 31);
        ICON_XP = getIcon(169, 98, 24, 24);
        ICON_PLAYER = getIcon(67, 92, 28, 32);
        ICON_COMPASS = getIcon(118, 66, 30, 26);
        ICON_MAP = getIcon(0, 95, 32, 26);
        ICON_WORLD = getIcon(133, 93, 32, 32);
        ICON_CLOCK = getIcon(86, 64, 28, 26);
        ICON_CLOSE_NORMAL = getIcon(239, 69, 45, 19);
        ICON_CLOSE_HOVER = getIcon(239, 88, 45, 19);
        ICON_CLOSE_PRESSED = getIcon(239, 107, 45, 19);
    }

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added
     * @param parent the {@link AlmuraGui} that we came from
     */
    public AlmuraGui(AlmuraGui parent) {
        renderer.setDefaultTexture(TEXTURE_DEFAULT);
        this.parent = Optional.fromNullable(parent);
        mc = Minecraft.getMinecraft();
    }

    /**
     * Snips out a {@link net.malisis.core.client.gui.icon.GuiIcon} based on the texture coordinates and size
     * @param x in pixels
     * @param y in pixels
     * @param width in pixels
     * @param height in pixels
     * @return the icon
     */
    public static GuiIcon getIcon(int x, int y, int width, int height) {
        return TEXTURE_DEFAULT.getIcon(x, y, width, height);
    }

    protected abstract void setup();

    /**
     * Closes the current screen and displays the parent screen
     */
    @Override
    public void close() {
        Keyboard.enableRepeatEvents(false);
        if (mc.thePlayer != null) {
            mc.thePlayer.closeScreen();
        }
        mc.displayGuiScreen(parent.isPresent() ? parent.get() : null);
        mc.setIngameFocus();
    }

    public static int getPaddedX(UIComponent component, int padding) {
        if (component == null) {
            return 0;
        }
        return component.getX() + component.getWidth() + padding;
    }

    public static int getPaddedY(UIComponent component, int padding) {
        if (component == null) {
            return 0;
        }
        return component.getY() + component.getHeight() + padding;
    }
}
