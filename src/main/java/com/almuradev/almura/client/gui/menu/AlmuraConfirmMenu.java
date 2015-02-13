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
    private UILabel messageLabel1, messageLabel2;
    private String message1, message2, title;

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added

     * @param parent the {@link AlmuraGui} that we came from
     */
    public AlmuraConfirmMenu(AlmuraGui parent, String message1, String message2, String title) {
        super(parent);
        this.message1 = message1;
        this.message2 = message2;
        this.title = title;
        setup();
    }

    @Override
    protected void setup() {
        // Create the form
        form = new UIForm(this, 200, 100, title);
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the message label
        messageLabel1 = new UILabel(this, ChatColor.AQUA + message1);
        messageLabel1.setFontScale(1.0F);
        messageLabel1.setColor(Color.white.getRGB());
        messageLabel1.setPosition(0, -5, Anchor.CENTER | Anchor.MIDDLE);
        
        messageLabel2 = new UILabel(this, ChatColor.WHITE + message2);
        messageLabel2.setFontScale(1.0F);
        messageLabel2.setColor(Color.white.getRGB());
        messageLabel2.setPosition(0, + 5, Anchor.CENTER | Anchor.MIDDLE);

        // Create the close button
        closeButton = new UIButton(this, "Close");
        closeButton.setSize(50, 16);
        closeButton.setPosition(-5, -5, Anchor.RIGHT | Anchor.BOTTOM);
        closeButton.setName("button.close");
        closeButton.register(this);

        form.getContentContainer().add(messageLabel1, messageLabel2, closeButton);

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
