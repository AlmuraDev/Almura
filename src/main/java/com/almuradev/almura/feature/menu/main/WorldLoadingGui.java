/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import com.almuradev.almura.shared.client.GuiConfig;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

/**
 * Replaces vanilla's "Downloading terrain" screen while joining a world or changing dimension (single player or a server), and the bare
 * progress screen single player shows while logging in to its own server; styled like {@link ConnectingGui}. Vanilla closes it (by opening no
 * screen) once the player's position arrives, so it needs no logic of its own; it just can't be escaped, like vanilla's.
 */
@SideOnly(Side.CLIENT)
public final class WorldLoadingGui extends BasicScreen {

    @Override
    public void construct() {
        this.guiscreenBackground = true;

        final BasicForm form = new BasicForm(this, 200, 135, "");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(false);
        form.setClosable(false);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(3);
        form.setLeftPadding(3);

        final UIImage almuraHeader = new UIImage(this, new GuiTexture(GuiConfig.Location.ALMURA_LOGO), null);
        almuraHeader.setSize(60, 99);
        almuraHeader.setPosition(0, 5, Anchor.TOP | Anchor.CENTER);

        final UISeparator aboveLogoSeparator = new UISeparator(this);
        aboveLogoSeparator.setSize(form.getWidth() - 5, 1);
        aboveLogoSeparator.setPosition(0, 107, Anchor.TOP | Anchor.CENTER);

        final UILabel messageLabel = new UILabel(this, "Loading world...");
        messageLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        // Vertically centred between the separator (y 107-108) and the bottom of the form (inner height 135 - 3 - 3 = 129): text ~10 tall
        messageLabel.setPosition(0, 113, Anchor.CENTER | Anchor.TOP);

        form.add(almuraHeader, aboveLogoSeparator, messageLabel);
        this.addToScreen(form);
    }

    @Override
    protected void keyTyped(final char keyChar, final int keyCode) {
        // Like vanilla's terrain screen, this can't be closed: the world is still loading
        if (keyCode == Keyboard.KEY_ESCAPE) {
            return;
        }
        super.keyTyped(keyChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
