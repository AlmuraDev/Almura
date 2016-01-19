/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.FontRenderOptionsConstants;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.client.network.play.B00PlayerDeathConfirmation;
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

public class IngameDied extends SimpleGui {

    protected static final ResourceLocation ALMURA_LOGO_LOCATION;

    static {
        try {
            ALMURA_LOGO_LOCATION = FileSystem.registerTexture(Almura.PLUGIN_ID, "textures/gui/almura.png", FileSystem.CONFIG_GUI_LOGO_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load logo.", e);
        }
    }

    public IngameDied() {
    }

    @Override
    public void construct() {
        guiscreenBackground = true;
        // Create the form
        final UIForm form = new UIForm(this, 225, 225, "Almura", false);
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the logo
        final UIImage logoImage = new UIImage(this, new GuiTexture(ALMURA_LOGO_LOCATION), null);
        logoImage.setAnchor(Anchor.CENTER | Anchor.TOP);
        logoImage.setSize(65, 95);

        final int padding = 4;

        // Died message
        final UILabel deathMessage = new UILabel(this, Colors.WHITE + "You have died...");
        deathMessage.setFontRenderOptions(FontRenderOptionsConstants.FRO_SCALE_110);
        deathMessage.setPosition(0, getPaddedY(logoImage, 10), Anchor.CENTER | Anchor.TOP);

        final UILabel returnMessage = new UILabel(this, Colors.WHITE + "But death is only the beginning " + this.mc.thePlayer.getDisplayName() + ".");
        returnMessage.setPosition(0, getPaddedY(logoImage, 25), Anchor.CENTER | Anchor.TOP);

        // Create the Respawn button
        final UIButton respawnButton = new UIButton(this, Colors.AQUA + "Respawn");
        respawnButton.setSize(100, 16);
        respawnButton.setPosition(0, getPaddedY(logoImage, 40), Anchor.CENTER | Anchor.TOP);
        respawnButton.setName("button.respawn");
        respawnButton.register(this);
        
        // Create the Respawn button
        final UIButton reviveButton = new UIButton(this, Colors.AQUA + "Revive");
        reviveButton.setSize(100, 16);
        reviveButton.setPosition(0, getPaddedY(logoImage, 60), Anchor.CENTER | Anchor.TOP);
        reviveButton.setName("button.revive");
        reviveButton.register(this);
        if (HUDData.PERMISSION_LEVEL < 6) {
            reviveButton.setDisabled(false);
            reviveButton.setTooltip("Requires Contributor or higher net.eq2online.permissions");
        } else {
            reviveButton.setDisabled(true);
            reviveButton.setTooltip("Revive at your current location");
        }

        // Create the quit button
        final UIButton quitButton = new UIButton(this, "Quit");
        quitButton.setSize(50, 16);
        quitButton.setPosition(0, getPaddedY(respawnButton, padding * 7), Anchor.CENTER | Anchor.TOP);
        quitButton.setName("button.quit");
        quitButton.register(this);

        form.getContentContainer().add(logoImage, deathMessage, returnMessage, respawnButton, reviveButton, quitButton);
        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException, AWTException {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.respawn":
                this.mc.thePlayer.setHealth(0.1F);
                this.mc.thePlayer.respawnPlayer();
                close();
                break;
            case "button.revive":                
                B00PlayerDeathConfirmation message = new B00PlayerDeathConfirmation(true, (int) mc.thePlayer.posX, (int) mc.thePlayer.posY, (int) mc.thePlayer.posZ, HUDData.WORLD_DISPLAY);
                ClientProxy.NETWORK_BUKKIT.sendToServer(message);
                this.mc.thePlayer.setHealth(0.1F);
                this.mc.thePlayer.respawnPlayer();
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
