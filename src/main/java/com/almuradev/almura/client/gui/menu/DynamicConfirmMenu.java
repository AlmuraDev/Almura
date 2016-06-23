/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.client.gui.util.FontRenderOptionsConstants;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.util.text.TextFormatting;

import java.util.Locale;

public class DynamicConfirmMenu extends SimpleGui {

    private String message1, message2, title;

    public DynamicConfirmMenu(SimpleGui parent, String message1, String message2, String title) {
        super(parent);
        this.message1 = message1;
        this.message2 = message2;
        this.title = title;
    }

    @Override
    public void construct() {

        // Create the form
        final UIForm form = new UIForm(this, 200, 100, title);
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the message label
        final UILabel messageLabel1 = new UILabel(this, TextFormatting.AQUA + message1);
        messageLabel1.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_WHITE);
        messageLabel1.setPosition(0, -5, Anchor.CENTER | Anchor.MIDDLE);

        final UILabel messageLabel2 = new UILabel(this, TextFormatting.WHITE + message2);
        messageLabel2.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_WHITE);
        messageLabel2.setPosition(0, +5, Anchor.CENTER | Anchor.MIDDLE);

        // Create the close button
        final UIButton closeButton = new UIButton(this, "Close");
        closeButton.setSize(50, 16);
        closeButton.setPosition(-5, -5, Anchor.RIGHT | Anchor.BOTTOM);
        closeButton.setName("button.close");
        closeButton.register(this);

        form.getContentContainer().add(messageLabel1, messageLabel2, closeButton);

        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH)) {
            case "button.close":
                close();
                break;
        }
    }
}
