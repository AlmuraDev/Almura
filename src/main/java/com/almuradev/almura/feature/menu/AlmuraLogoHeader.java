/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.menu.figure.FigureSlot;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.theme.SpriteId;

/**
 * The header of the menus outside of a world: Dockter_Almura in 3D raising the sword, standing over "Almura Reborn". The title screen and the
 * server list put it at the top of a column of the same height, so the logo stays in place going from one to the other, as on 1.12.
 */
public final class AlmuraLogoHeader {

    // The column the header tops, on the title screen and the server list: room for the taller of the two (the server list's: the header, its
    // two servers, Join, and Back and Other)
    public static final float MENU_HEIGHT = 246f;

    private static final SpriteId TEXT = SpriteId.of(Almura.ID, "almura_logo_text");
    // The figure over the text, standing 10 above its place; the text 20% larger than 1.12's
    private static final float FIGURE_HEIGHT = 116f, FIGURE_LIFT = 10f, TEXT_WIDTH = 72f, TEXT_HEIGHT = 23f;

    private AlmuraLogoHeader() {
    }

    public static StackPanel create(final ComponentFactory factory) {
        final Image text = factory.image(TEXT).setImageSize(TEXT_WIDTH, TEXT_HEIGHT).setScaling(Image.Scaling.FIT);
        text.setSize(TEXT_WIDTH, TEXT_HEIGHT);
        return factory.stackPanel()
            .add(FigureSlot.create(factory, FIGURE_HEIGHT, FIGURE_LIFT).setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP))
            .add(text.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP));
    }
}
