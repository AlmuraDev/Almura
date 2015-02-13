/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraBackgroundGui;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;

import java.awt.*;

public class AlmuraConfirmMenu extends AlmuraBackgroundGui {

    private UIForm form;
    private UIButton closeButton;
    private UILabel messageLabel;
    private String message, title;

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added

     * @param parent the {@link AlmuraGui} that we came from
     */
    public AlmuraConfirmMenu(AlmuraGui parent, String message, String title) {
        super(parent);
        this.message = message;
        this.title = title;
        setup();
    }

    @Override
    protected void setup() {
        // Create the form
        form = new UIForm(this, 200, 100, title);
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the message label
        messageLabel = new UILabel(this, ChatColor.AQUA + message);
        messageLabel.setFontScale(1.0F);
        messageLabel.setColor(Color.white.getRGB());
        messageLabel.setPosition(0, 0, Anchor.CENTER | Anchor.MIDDLE);

        // Create the close button
        closeButton = new UIButton(this, "Close");
        closeButton.setSize(50, 16);
        closeButton.setPosition(-5, -5, Anchor.RIGHT | Anchor.BOTTOM);
        closeButton.setName("button.close");
        closeButton.register(this);

        form.getContentContainer().add(messageLabel, closeButton);

        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                close();
                break;
        }
    }
}
