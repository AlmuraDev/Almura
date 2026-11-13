/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.figure;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Util;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.layout.Spacer;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Puts the figure in a Strata screen. Strata draws only its own kinds of pictures, so the screen holds an empty space of the figure's size
 * and the figure is drawn over it after the screen, wherever Strata laid the space out, in the frames Strata painted it.
 */
public final class FigureSlot {

    // Spaces painted since the screen last drew
    private static final List<FigureSlot> PAINTED = new ArrayList<>();
    // Not drawn for this long: it was off screen, play the sequence again
    private static final long RESTART_AFTER_MILLIS = 500L;

    private final Spacer space;
    private final float lift;
    // When the sequence started, -1 until it first shows; and when it was last drawn
    private long startMillis = -1L, lastDrawnMillis;

    private FigureSlot(final Spacer space, final float lift) {
        this.space = space;
        this.lift = lift;
    }

    /**
     * Makes the space for a figure: as tall as given, wide enough for its raised sword.
     *
     * @param lift how far the figure stands above its space (1.12 raised it over the logo's text)
     * @return the space, to add to the screen
     */
    public static Spacer create(final ComponentFactory factory, final float height, final float lift) {
        final Spacer space = factory.spacer((float) Math.ceil(height * AlmuraFigure.WIDTH_RATIO), height);
        final FigureSlot slot = new FigureSlot(space, lift);
        space.onEvent(Component.PAINTED, event -> FigureSlot.PAINTED.add(slot));
        return space;
    }

    /**
     * Draws the figures whose spaces the screen just painted, over the screen. Registered on the game's event bus by
     * {@link com.almuradev.almura.AlmuraClient}.
     */
    public static final class Drawer {

        @SubscribeEvent
        public void onScreenRender(final ScreenEvent.Render.Post event) {
            if (FigureSlot.PAINTED.isEmpty()) {
                return;
            }
            final GuiGraphicsExtractor graphics = event.getGuiGraphics();
            // Above what the screen drew
            graphics.nextStratum();
            for (final FigureSlot slot : List.copyOf(FigureSlot.PAINTED)) {
                slot.draw(graphics);
            }
            FigureSlot.PAINTED.clear();
        }
    }

    private void draw(final GuiGraphicsExtractor graphics) {
        final Rect bounds = this.space.bounds();
        if (bounds.width() <= 0f || bounds.height() <= 0f) {
            return;
        }
        final long now = Util.getMillis();
        // Standing until the loading overlay has gone, so the sequence plays where it can be seen
        if (Minecraft.getInstance().gui.overlay() != null || this.startMillis < 0L || now - this.lastDrawnMillis > RESTART_AFTER_MILLIS) {
            this.startMillis = now;
        }
        this.lastDrawnMillis = now;
        final float seconds = (now - this.startMillis) / 1000.0F;

        final int x0 = Math.round(bounds.x()), y0 = Math.round(bounds.y() - this.lift);
        final int x1 = Math.round(bounds.x() + bounds.width()), y1 = Math.round(bounds.y() + bounds.height() - this.lift);
        final float pixelsPerBlock = (y1 - y0) / AlmuraFigure.HEIGHT_BLOCKS;
        graphics.submitPictureInPictureRenderState(new AlmuraFigureRenderState(seconds, x0, y0, x1, y1, pixelsPerBlock));

        // The star burst at the sword's tip, turning slowly, growing as it flares
        final float burst = AlmuraFigure.burstAmount(seconds);
        if (burst > 0.0F) {
            final Vector3f tip = AlmuraFigure.tip(seconds, x0, y0, x1 - x0, y1 - y0, pixelsPerBlock);
            final int size = Math.max(2, Math.round(2.0F * pixelsPerBlock * AlmuraFigure.BURST_SIZE * (0.5F + 0.5F * burst)));
            final int alpha = Math.round(burst * 255.0F);
            graphics.pose().pushMatrix();
            graphics.pose().translate(tip.x(), tip.y());
            graphics.pose().rotate((float) Math.toRadians(seconds * 40.0F));
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlmuraFigure.BURST, -size / 2, -size / 2, size, size, alpha << 24 | 0xFFFFFF);
            graphics.pose().popMatrix();
        }
    }
}
