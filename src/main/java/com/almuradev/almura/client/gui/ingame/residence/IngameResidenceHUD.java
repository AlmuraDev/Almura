/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame.residence;

import com.almuradev.almura.Configuration;
import com.almuradev.almurasdk.client.gui.SimpleGui;
import com.almuradev.almurasdk.util.Colors;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

public class IngameResidenceHUD extends SimpleGui {

    public static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    public UILabel title, resName, resOwner, resOwnerOnline, resBank, resLeaseCost, resLeaseExpireTitle, resLeaseExpire;
    public UIBackgroundContainer resPane;
    private boolean firstDraw = true;

    public IngameResidenceHUD() {
        construct();
    }

    @Override
    public void construct() {
        guiscreenBackground = false;

        // Construct Hud with all elements
        resPane = new UIBackgroundContainer(this, 90, 55);
        resPane.setAnchor(Anchor.RIGHT | Anchor.BOTTOM);
        resPane.setColor(Integer.MIN_VALUE);
        resPane.setBackgroundAlpha(125);

        //////////////////////////////// LEFT COLUMN //////////////////////////////////////

        // Player Display Name
        title = new UILabel(this, Colors.AQUA + "Residence Info");
        title.setPosition(0, 1, Anchor.CENTER | Anchor.TOP);
        title.getFontRenderOptions().color = Colors.WHITE.getGuiColorCode();
        title.setSize(7, 7);
        title.getFontRenderOptions().fontScale = 0.8f;

        resName = new UILabel(this, Colors.WHITE + "Name: DocksArea");
        resName.setPosition(3, title.getY() + 8, Anchor.LEFT | Anchor.TOP);
        resName.getFontRenderOptions().color = Colors.WHITE.getGuiColorCode();
        resName.setSize(7, 7);
        resName.getFontRenderOptions().fontScale = 0.7F;

        resOwner = new UILabel(this, Colors.WHITE + "Owner: ~Dockter");
        resOwner.setPosition(7, resName.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resOwner.getFontRenderOptions().color = Colors.WHITE.getGuiColorCode();
        resOwner.setSize(7, 7);
        resOwner.getFontRenderOptions().fontScale = 0.7F;

        resOwnerOnline = new UILabel(this, Colors.WHITE + "Owner Last Online: NOW");
        resOwnerOnline.setPosition(7, resOwner.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resOwnerOnline.getFontRenderOptions().color = Colors.WHITE.getGuiColorCode();
        resOwnerOnline.setSize(7, 7);
        resOwnerOnline.getFontRenderOptions().fontScale = 0.7F;

        resBank = new UILabel(this, Colors.WHITE + "Vault: $999,000.00");
        resBank.setPosition(3, resOwnerOnline.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resBank.getFontRenderOptions().color = Colors.WHITE.getGuiColorCode();
        resBank.setSize(7, 7);
        resBank.getFontRenderOptions().fontScale = 0.7F;

        resLeaseCost = new UILabel(this, Colors.WHITE + "Lease Cost: $999,000.00");
        resLeaseCost.setPosition(3, resBank.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resLeaseCost.getFontRenderOptions().color = Colors.WHITE.getGuiColorCode();
        resLeaseCost.setSize(7, 7);
        resLeaseCost.getFontRenderOptions().fontScale = 0.7F;

        resLeaseExpireTitle = new UILabel(this, Colors.WHITE + "Lease Expires:");
        resLeaseExpireTitle.setPosition(3, resLeaseCost.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resLeaseExpireTitle.getFontRenderOptions().color = Colors.WHITE.getGuiColorCode();
        resLeaseExpireTitle.setSize(7, 7);
        resLeaseExpireTitle.getFontRenderOptions().fontScale = 0.7F;

        resLeaseExpire = new UILabel(this, Colors.WHITE + "Never");
        resLeaseExpire.setPosition(8, resLeaseExpireTitle.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resLeaseExpire.getFontRenderOptions().color = Colors.WHITE.getGuiColorCode();
        resLeaseExpire.setSize(7, 7);
        resLeaseExpire.getFontRenderOptions().fontScale = 0.7F;

        resPane.add(title, resName, resOwner, resOwnerOnline, resBank, resLeaseCost, resLeaseExpireTitle, resLeaseExpire);
        addToScreen(resPane);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onClose() {
        super.onClose();

        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (!Configuration.DISPLAY_RESIDENCE_HUD || !ResidenceData.WITHIN_RESIDENCE || mc.isSingleplayer()) {
            return;
        }

        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            setWorldAndResolution(MINECRAFT, event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
            if (firstDraw) { // This is used to fix alignment issues.
                title.setText(Colors.AQUA + "Residence Info");
                firstDraw = false;
            }
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
        }
    }

    public void refreshFromData() {
        if (ResidenceData.OWNER_NAME.equalsIgnoreCase("mcsnetworks")) {
            resOwner.setText("Owner: " + Colors.RED + "~Dockter");
        } else {
            resOwner.setText("Owner: " + Colors.RED + ResidenceData.OWNER_NAME);
        }
        resOwnerOnline.setText("Last Seen: " + Colors.LIGHT_PURPLE + ResidenceData.LAST_ONLINE);
        resBank.setText("Vault: " + ResidenceData.VAULT);
        resLeaseCost.setText("Lease Cost: " + Colors.BLUE + ResidenceData.LEASE_COST);
        resLeaseExpire.setText(Colors.YELLOW + ResidenceData.LEASE_EXPIRATION);
        int originalWidth = resPane.getWidth();
        if (resOwnerOnline.getWidth() + 20 > originalWidth) {
            resPane.setSize(resOwnerOnline.getWidth() + 10, resPane.getHeight());
        }

        if (resLeaseExpire.getWidth() + 20 > originalWidth) {
            resPane.setSize(resLeaseExpire.getWidth() + 10, resPane.getHeight());
        }

        // Window re-arrangement if server-owned.
        if (ResidenceData.OWNER_NAME.equalsIgnoreCase("almura_admin")) {
            title.setText("Protected Server-Owned Area");
            resName.setText("Area Name: " + Colors.GREEN + ResidenceData.NAME);
            resOwner.setVisible(false);
            resOwnerOnline.setVisible(false);
            resBank.setVisible(false);
            resLeaseCost.setVisible(false);
            resLeaseExpire.setVisible(true);
            resPane.setSize(90, 20);
        } else {
            title.setText("Residence Info");
            resName.setText("Name: " + Colors.GREEN + ResidenceData.NAME);
            resOwner.setVisible(true);
            resOwnerOnline.setVisible(true);
            resBank.setVisible(true);
            resLeaseCost.setVisible(true);
            resLeaseExpire.setVisible(true);
            resPane.setSize(resPane.getWidth(), 55);
        }
    }
}
