/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.client.network.play.B01ResTokenConfirmation;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.FontRenderOptionsConstants;
import com.almuradev.almura.client.network.play.B00PlayerDeathConfirmation;
import com.almuradev.almurasdk.FileSystem;
import com.almuradev.almurasdk.client.gui.SimpleGui;
import com.almuradev.almurasdk.client.gui.components.UIForm;
import com.almuradev.almurasdk.util.Colors;
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

    public IngameResToken() {
    }

    @Override
    public void construct() {
        guiscreenBackground = true;
        // Create the form
        final UIForm form = new UIForm(this, 225, 225, "Almura", false);
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        final int padding = 4;

        // Res Token Text
        final UILabel line1 = new UILabel(this, Colors.WHITE + "Do you wish to use this");        
        line1.setPosition(0, 10, Anchor.CENTER | Anchor.TOP);

        final UILabel line2 = new UILabel(this, Colors.RED + "[ Res Token ]");
        line2.setFontRenderOptions(FontRenderOptionsConstants.FRO_SCALE_110);
        line2.setPosition(0, 15, Anchor.CENTER | Anchor.TOP);
        
        final UILabel line3 = new UILabel(this, Colors.WHITE + "to remove the lease on this residence?");
        line3.setPosition(0, 15, Anchor.CENTER | Anchor.TOP);

        // Yes Button
        final UIButton yesButton = new UIButton(this, Colors.AQUA + "Yes");
        yesButton.setSize(100, 10);
        yesButton.setPosition(0, 30, Anchor.CENTER | Anchor.TOP);
        yesButton.setName("button.yes");
        yesButton.register(this);
        
        // No Button
        final UIButton noButton = new UIButton(this, Colors.AQUA + "No");
        noButton.setSize(100, 10);
        noButton.setPosition(0, 30, Anchor.CENTER | Anchor.TOP);
        noButton.setName("button.revive");
        noButton.register(this);        

        form.getContentContainer().add(line1, line2, line3, yesButton, noButton);
        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException, AWTException {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.yes":
                B01ResTokenConfirmation message = new B01ResTokenConfirmation(true);
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
