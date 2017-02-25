/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.guide;

import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.util.Colors;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.awt.AWTException;
import java.io.IOException;
import java.net.URISyntaxException;

public class ConfirmGui extends SimpleGui {
    private boolean showCloseButton = false;
    private String message1 = "message1";
    private String message2 = "message2";
    private String titleMessage = "title";

    public ConfirmGui(String message1, String message2, String titleMessage, boolean showCloseButton) {       
        this.message1 = message1;
        this.message2 = message2;
        this.titleMessage = titleMessage;
        this.showCloseButton = showCloseButton;
    }

    @Override
    public void construct() {
        guiscreenBackground = true;
        // Create the form
        final UIForm form = new UIForm(this, 225, 100, titleMessage, showCloseButton);
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Confirmation Window Text
        final UILabel line1 = new UILabel(this, Colors.WHITE + message1);
        line1.setPosition(0, 10, Anchor.CENTER | Anchor.TOP);

        // Confirmation Window Text
        final UILabel line2 = new UILabel(this, Colors.WHITE + message2);        
        line2.setPosition(0, 30, Anchor.CENTER | Anchor.TOP);

        // Close Button
        final UIButton yesButton = new UIButton(this, Colors.AQUA + "Yes");
        yesButton.setSize(30, 10);
        yesButton.setPosition(-45, -10, Anchor.RIGHT | Anchor.BOTTOM);
        yesButton.setName("button.yes");
        yesButton.register(this);

        // Cancel
        final UIButton noButton = new UIButton(this, Colors.AQUA + "No");
        noButton.setSize(30, 10);
        noButton.setPosition(-10, -10, Anchor.RIGHT | Anchor.BOTTOM);
        noButton.setName("button.no");
        noButton.register(this);        

        form.getContentContainer().add(line1, line2, yesButton, noButton);
        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException, AWTException {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.yes":               
                close();
                break;
            case "button.no":
                close();
                new ViewPagesGui().display(); 
                break;
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            // Do Nothing.
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof ConfirmGui)) {
            this.close();
        }
    }
}
