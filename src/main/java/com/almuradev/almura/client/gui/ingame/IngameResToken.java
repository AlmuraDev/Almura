/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.client.network.play.B01ResTokenConfirmation;
import com.almuradev.almura.util.Colors;
import com.almuradev.almura.util.FileSystem;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;

import java.awt.AWTException;
import java.io.IOException;
import java.net.URISyntaxException;

public class IngameResToken extends SimpleGui {
    
    protected static final ResourceLocation ICON_IMAGE_LOCATION;
    
    static {
        try {
            ICON_IMAGE_LOCATION = FileSystem.registerTexture(Almura.MOD_ID, "textures/gui/almuracustom1_restoken.png", FileSystem
                    .CONFIG_GUI_RESTOKENICON_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load logo.", e);
        }
    }
    public IngameResToken() {
    }

    @Override
    public void construct() {
        guiscreenBackground = true;
        // Create the form
        final UIForm form = new UIForm(this, 225, 175, "Almura", false);
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        // Res Token Text
        final UILabel line1 = new UILabel(this, Colors.WHITE + "Do you wish to use this");        
        line1.setPosition(0, 10, Anchor.CENTER | Anchor.TOP);

        // Create Icon
        final UIImage iconImage = new UIImage(this, new GuiTexture(ICON_IMAGE_LOCATION), null);
        iconImage.setPosition(0, 25, Anchor.CENTER | Anchor.TOP);
        iconImage.setSize(32, 32);        
        
        final UILabel line2 = new UILabel(this, Colors.WHITE + "to remove the lease on this residence?");
        line2.setPosition(0, 60, Anchor.CENTER | Anchor.TOP);

        // Yes Button
        final UIButton yesButton = new UIButton(this, Colors.AQUA + "Yes");
        yesButton.setSize(100, 10);
        yesButton.setPosition(0, 90, Anchor.CENTER | Anchor.TOP);
        yesButton.setName("button.yes");
        yesButton.register(this);
        
        // No Button
        final UIButton noButton = new UIButton(this, Colors.AQUA + "No");
        noButton.setSize(100, 10);
        noButton.setPosition(0, 110, Anchor.CENTER | Anchor.TOP);
        noButton.setName("button.no");
        noButton.register(this);        

        form.getContentContainer().add(iconImage, line1, line2, yesButton, noButton);
        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException, AWTException {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.yes":
                B01ResTokenConfirmation message = new B01ResTokenConfirmation(true);
                ClientProxy.NETWORK_BUKKIT.sendToServer(message);
                close();
                break;
            case "button.no":
                close();
                break;
            case "button.quit":
                close();
                this.mc.theWorld.sendQuittingDisconnectingPacket();
                this.mc.loadWorld((WorldClient) null);
                this.mc.displayGuiScreen(new GuiMainMenu());
                break;
        }
    }
}
