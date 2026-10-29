/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.component.UIPlayerFigure;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.render.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The Almura logo of the main and multiplayer menus: Dockter_Almura in 3D (the picture until his skin loads) above "Almura / Resurrected".
 */
@SideOnly(Side.CLIENT)
public final class AlmuraLogoHeader {

    // The logo's text at the size the whole logo used to be drawn (a fifth of the picture)
    private static final int TEXT_WIDTH = 60, TEXT_HEIGHT = 19;
    private static final int FIGURE_HEIGHT = 116, FIGURE_WIDTH = (int) (FIGURE_HEIGHT * UIPlayerFigure.WIDTH_RATIO);
    // How far the figure stands above its place over the text
    private static final int FIGURE_LIFT = 10;
    // The old logo's height, which the menus were laid out around
    public static final int OLD_HEIGHT = 99;
    public static final int WIDTH = Math.max(TEXT_WIDTH, FIGURE_WIDTH), HEIGHT = FIGURE_HEIGHT + TEXT_HEIGHT;

    private AlmuraLogoHeader() {
    }

    public static BasicContainer<?> create(final MalisisGui gui) {
        final BasicContainer<?> header = new BasicContainer<>(WIDTH, HEIGHT);
        header.setBackgroundAlpha(0);
        header.setPadding(0, 0);
        // The figure is raised above the header: the raised sword and its burst must not be cut off
        header.setClipContent(false);

        final UIPlayerFigure figure = new UIPlayerFigure(gui, GuiConfig.Location.DOCKTER_ALMURA_SKIN, false);
        figure.setSize(FIGURE_WIDTH, FIGURE_HEIGHT);
        figure.setPosition(0, -FIGURE_LIFT, Anchor.TOP | Anchor.CENTER);

        final UIImage text = new UIImage(GuiConfig.wholeTexture(GuiConfig.Location.ALMURA_LOGO_TEXT));
        text.setSize(TEXT_WIDTH, TEXT_HEIGHT);
        text.setPosition(0, 0, Anchor.BOTTOM | Anchor.CENTER);

        header.add(figure, text);
        return header;
    }
}
