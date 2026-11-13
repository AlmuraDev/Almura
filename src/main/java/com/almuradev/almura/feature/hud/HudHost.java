/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.feature.menu.AlmuraScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.inspirenxe.strata.core.StrataCore;
import org.inspirenxe.strata.minecraft.MinecraftCanvas;
import org.inspirenxe.strata.minecraft.MinecraftPlatform;
import org.inspirenxe.strata.spi.ScreenHost;
import org.jspecify.annotations.Nullable;

/**
 * Shows a Strata screen on the in-game HUD. Strata's Minecraft adapter only hosts screens, so this hosts one itself, the way the adapter's
 * StrataScreen does, but drawn from a HUD layer each frame and taking no input: the HUD never has the mouse or the keyboard.
 */
final class HudHost {

    private final Minecraft minecraft;
    private final org.inspirenxe.strata.api.screen.Screen content;
    private @Nullable ScreenHost host;
    private @Nullable MinecraftCanvas canvas;
    private float width = -1f, height = -1f, pixelsPerUnit = -1f;

    HudHost(final Minecraft minecraft, final org.inspirenxe.strata.api.screen.Screen content) {
        this.minecraft = minecraft;
        this.content = content;
    }

    /**
     * Lays the screen out to the HUD's size and draws it.
     */
    void render(final GuiGraphicsExtractor graphics) {
        final ScreenHost host = this.host();
        final float width = graphics.guiWidth(), height = graphics.guiHeight();
        final float pixelsPerUnit = Math.max(1, this.minecraft.getWindow().getGuiScale());
        if (pixelsPerUnit != this.pixelsPerUnit) {
            this.pixelsPerUnit = pixelsPerUnit;
            host.setPixelsPerUnit(pixelsPerUnit);
        }
        if (width != this.width || height != this.height) {
            this.width = width;
            this.height = height;
            host.resize(width, height);
        }
        if (host.screen().isEmpty()) {
            host.open(this.content);
        }
        this.canvas.begin(graphics, pixelsPerUnit);
        host.render(this.canvas);
    }

    private ScreenHost host() {
        if (this.host == null) {
            this.canvas = new MinecraftCanvas(this.minecraft.font);
            final MinecraftPlatform platform = new MinecraftPlatform(this.minecraft, this.canvas.textMetrics(), new NoFocus());
            // Every image on the HUD is given its size, so the host needs no sprite sizes; nothing closes it
            this.host = StrataCore.createScreenHost(platform, AlmuraScreens.THEME_REF, () -> { });
        }
        return this.host;
    }

    /**
     * What the platform reports text focus against; the HUD never takes it.
     */
    private static final class NoFocus implements GuiEventListener {

        @Override
        public void setFocused(final boolean focused) {
        }

        @Override
        public boolean isFocused() {
            return false;
        }
    }
}
