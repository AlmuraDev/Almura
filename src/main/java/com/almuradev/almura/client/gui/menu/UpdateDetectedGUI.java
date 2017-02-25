/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.FontRenderOptionsConstants;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.util.Colors;
import com.almuradev.almura.util.FileSystem;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class UpdateDetectedGUI extends SimpleGui {
        
    protected static final ResourceLocation ALMURA_LOGO_LOCATION;

    static {
        try {
            ALMURA_LOGO_LOCATION = FileSystem.registerTexture(Almura.MOD_ID, "textures/gui/almura.png", FileSystem.CONFIG_GUI_LOGO_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load logo.", e);
        }
    }

    public UpdateDetectedGUI(SimpleGui parent) {
        super(parent);
    }
    
    @Override
    public void construct() {

        // Create the form
        final UIForm form = new UIForm(this, 200, 175, "Load optimized settings?");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the logo
        final UIImage logoImage = new UIImage(this, new GuiTexture(ALMURA_LOGO_LOCATION), null);
        logoImage.setAnchor(Anchor.CENTER | Anchor.TOP);
        logoImage.setSize(65, 95);

        // Create the message label
        final UILabel messageLabel1 = new UILabel(this, Colors.AQUA + "Update detected...");
        messageLabel1.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_WHITE);
        messageLabel1.setPosition(0, 25, Anchor.CENTER | Anchor.MIDDLE);

        final UILabel messageLabel2 = new UILabel(this, Colors.WHITE + "Do you want to load optimized settings?");
        messageLabel2.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_WHITE);
        messageLabel2.setPosition(0, 40, Anchor.CENTER | Anchor.MIDDLE);

        // Create the No button
        final UIButton yesButton = new UIButton(this, "Yes");
        yesButton.setSize(40, 16);
        yesButton.setPosition(-50, -5, Anchor.RIGHT | Anchor.BOTTOM);
        yesButton.setName("button.yes");
        yesButton.register(this);
        
        // Create the No button
        final UIButton noButton = new UIButton(this, "No");
        noButton.setSize(40, 16);
        noButton.setPosition(-5, -5, Anchor.RIGHT | Anchor.BOTTOM);
        noButton.setName("button.no");
        noButton.register(this);

        form.getContentContainer().add(logoImage, messageLabel1, messageLabel2, yesButton, noButton);

        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName()) {
            case "button.yes":
                Configuration.setOptimizedConfig();
                try {
                    Configuration.load();
                } catch (IOException crash) {
                    crash.printStackTrace();
                }
                close();
                break;
            case "button.no":
                try {
                    Configuration.setFirstLaunch(false);
                } catch (IOException crash) {
                    crash.printStackTrace();
                }
                close();
                break;
        }
    }
}
