/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.Almura;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.inspirenxe.strata.api.extension.Painter;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * The game's GUI graphics behind a Strata painter, while Strata paints, so something Strata can't draw (a player model) is drawn in Strata's
 * order: under what Strata paints after it, such as tooltips. Strata keeps them private (its {@code CanvasPainter}'s {@code canvas}, the
 * Minecraft adapter's {@code MinecraftCanvas}'s {@code graphics}), so they're read by reflection; if a Strata update moves them this says so
 * once and answers null, and callers draw the old way (over the screen).
 * <p>
 * Until Strata offers a hook of its own for this (a visual drawn through a callback, say).
 */
public final class StrataGraphics {

    private static @Nullable Field canvasField, graphicsField;
    private static boolean failed;

    private StrataGraphics() {
    }

    /**
     * @return the GUI graphics the painter draws to, or null if they can't be found
     */
    public static @Nullable GuiGraphicsExtractor of(final Painter painter) {
        if (failed) {
            return null;
        }
        try {
            if (canvasField == null) {
                canvasField = field(painter.getClass(), "canvas");
            }
            final Object canvas = canvasField.get(painter);
            if (canvas == null) {
                return null;
            }
            if (graphicsField == null) {
                graphicsField = field(canvas.getClass(), "graphics");
            }
            return graphicsField.get(canvas) instanceof final GuiGraphicsExtractor graphics ? graphics : null;
        } catch (final ReflectiveOperationException | RuntimeException e) {
            failed = true;
            Almura.LOGGER.warn("Couldn't reach the GUI graphics behind Strata's painter; player previews draw over tooltips", e);
            return null;
        }
    }

    private static Field field(final Class<?> type, final String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                final Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (final NoSuchFieldException ignored) {
                // Look further up
            }
        }
        throw new NoSuchFieldException(type.getName() + "." + name);
    }
}
