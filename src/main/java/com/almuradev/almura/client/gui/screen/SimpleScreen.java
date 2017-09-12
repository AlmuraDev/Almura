/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen;

import com.almuradev.almura.client.gui.GuiConfig;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public abstract class SimpleScreen extends MalisisGui {

    protected final Optional<GuiScreen> parent;

    /**
     * Creates a gui with an absent parent
     */
    public SimpleScreen() {
        this(null);
    }

    /**
     * Creates a gui with a parent
     *
     * @param parent the {@link SimpleScreen} that we came from
     */
    public SimpleScreen(@Nullable GuiScreen parent) {
        this.parent = Optional.ofNullable(parent);
        this.renderer.setDefaultTexture(GuiConfig.SpriteSheet.ALMURA);
    }

    /**
     * Gets the X position after applying padding against another component, uses {@link Anchor#LEFT} by default
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @return The padded X position
     */
    public static int getPaddedX(UIComponent<?> component, int padding) {
        return getPaddedX(component, padding, Anchor.LEFT);
    }

    /**
     * Gets the X position after applying padding against another component
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @param anchor The direction we're going
     * @return The padded X position
     */
    public static int getPaddedX(UIComponent<?> component, int padding, int anchor) {
        if (anchor == Anchor.LEFT) {
            return component.getX() + component.getWidth() + padding;
        } else if (anchor == Anchor.RIGHT) {
            return component.getX() - component.getWidth() - padding;
        } else {
            throw new IllegalArgumentException("Invalid anchor used [" + anchor + "], anchor must be LEFT or RIGHT.");
        }
    }

    /**
     * Gets the Y position after applying padding against another component, uses {@link Anchor#TOP} by default
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @return The padded Y position
     */
    public static int getPaddedY(UIComponent<?> component, int padding) {
        return getPaddedY(component, padding, Anchor.TOP);
    }

    /**
     * Gets the Y position after applying padding against another component
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @param anchor The direction we're going
     * @return The padded Y position
     */
    public static int getPaddedY(UIComponent<?> component, int padding, int anchor) {
        if (anchor == Anchor.BOTTOM) {
            return component.getY() - component.getHeight() - padding;
        } else if (anchor == Anchor.TOP) {
            return component.getY() + component.getHeight() + padding;
        } else {
            throw new IllegalArgumentException("Invalid anchor used [" + anchor + "], anchor must be BOTTOM or TOP.");
        }
    }

    /**
     * Closes this {@link SimpleScreen} and displays the parent, if present.
     */
    @Override
    public void close() {
        setFocusedComponent(null, true);
        setHoveredComponent(null, true);
        Keyboard.enableRepeatEvents(false);
        if (this.mc.player != null) {
            this.mc.player.closeScreen();
        }

        this.onClose();

        this.mc.displayGuiScreen(this.parent.orElse(null));
        if (!this.parent.isPresent()) {
            this.mc.setIngameFocus();
        }
    }

    public void addToScreen(UIComponent... components) {
        Arrays.stream(components).forEach(this::addToScreen);
    }

    protected void onClose() {
    }
}

