/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui;

import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Spacing for the row of buttons at the bottom of a window (Close and the like): whatever sits above the row (a separator, a list) keeps
 * the same gap above the buttons as there is below them.
 */
@SideOnly(Side.CLIENT)
public final class BottomRow {

    private BottomRow() {
    }

    /**
     * @return the space you see below a bottom button: the container's bottom padding, less its border, which is drawn inside the padding
     */
    public static int gap(final BasicContainer<?> container) {
        return Math.max(0, container.getBottomPadding() - container.getBottomBorderSize());
    }

    /**
     * @return how far up from the bottom of the container's content something above a bottom button of this height should end
     */
    public static int above(final BasicContainer<?> container, final int buttonHeight) {
        return buttonHeight + gap(container);
    }

    /**
     * Sets the container's height so its bottom buttons sit as far below the lowest of its other contents as they are above its bottom
     * edge: the {@link #gap}, plus however far the button is raised off the bottom.
     *
     * @param lowest the lowest component above the bottom buttons, placed from the top of the container
     * @param button one of the bottom buttons (anchored to the bottom)
     */
    public static void fitHeight(final BasicContainer<?> container, final UIComponent lowest, final UIComponent button) {
        // An explicit array: with two components Java would pick this method again rather than the varargs one
        fitHeight(container, button, new UIComponent[] {lowest});
    }

    /**
     * As {@link #fitHeight(BasicContainer, UIComponent, UIComponent)}, for side by side contents: the one ending lowest counts.
     *
     * @param button one of the bottom buttons (anchored to the bottom)
     * @param above the contents above the bottom buttons, placed from the top of the container
     */
    public static void fitHeight(final BasicContainer<?> container, final UIComponent button, final UIComponent... above) {
        int bottom = 0;
        for (final UIComponent component : above) {
            bottom = Math.max(bottom, component.getY() + component.getHeight());
        }
        final int padding = container.getHeight() - BasicScreen.getPaddedHeight(container);
        final int raised = Math.max(0, -button.getY());
        container.setHeight(padding + bottom + raised + above(container, button.getHeight()) + raised);
    }
}
