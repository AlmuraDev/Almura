/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraBackgroundGui;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;

import java.awt.*;

public class AlmuraConfirmMenu extends AlmuraBackgroundGui {

    private UIBackgroundContainer window, uiTitleBar;
    private UIButton xButton, closeButton;
    private UILabel titleLabel, messageLabel;
    private String message, title;

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added

     * @param parent the {@link AlmuraGui} that we came from
     */
    public AlmuraConfirmMenu(AlmuraGui parent, String message, String title) {
        super(parent);
        this.message = message;
        this.title = title;
    }

    @Override
    protected void setup() {
        // Create the window container
        window = new UIBackgroundContainer(this);
        window.setSize(200, 100);
        window.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        window.setColor(Integer.MIN_VALUE);
        window.setBackgroundAlpha(125);

        final int padding = 4;

        // Create the title & Window layout 
        titleLabel = new UILabel(this, "TITLE");
        titleLabel.setPosition(0, padding + 1, Anchor.CENTER | Anchor.TOP);
        titleLabel.setColor(Color.white.getRGB());

        uiTitleBar = new UIBackgroundContainer(this);
        uiTitleBar.setSize(300, 1);
        uiTitleBar.setPosition(0, 17, Anchor.CENTER | Anchor.TOP);
        uiTitleBar.setColor(Color.gray.getRGB());

        xButton = new UIButton(this, ChatColor.BOLD + "X");
        xButton.setSize(5, 1);
        xButton.setPosition(-3, 1, Anchor.RIGHT | Anchor.TOP);
        xButton.setName("button.close");
        xButton.register(this);
        //xButton.setColor(Color.white.getRGB());

        // Create the message label
        messageLabel = new UILabel(this, ChatColor.AQUA + "MESSAGE");
        messageLabel.setFontScale(1.0F);
        messageLabel.setColor(Color.white.getRGB());
        messageLabel.setPosition(0, 0, Anchor.CENTER | Anchor.MIDDLE);

        // Create the close button
        closeButton = new UIButton(this, "Close");
        closeButton.setSize(50, 16);
        closeButton.setPosition(-5, -5, Anchor.RIGHT | Anchor.BOTTOM);
        closeButton.setName("button.close");
        closeButton.register(this);

        window.add(titleLabel, uiTitleBar, xButton, messageLabel, closeButton);

        // Allow the window to move
        new UIMoveHandle(this, window);

        addToScreen(window);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                displayParent();
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (titleLabel.getText() != title) {
            titleLabel.setText(title);
        }
        if (messageLabel.getText() != message) {
            messageLabel.setText(message);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
