/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame.residence;

import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraGui;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

public class IngameResidenceHUD extends AlmuraGui {

    public static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    public UILabel title, resName, resOwner, resOwnerOnline, resBank, resLeaseCost, resLeaseExpireTitle, resLeaseExpire;
    public UIBackgroundContainer resPane;
    private boolean firstDraw = true;

    public IngameResidenceHUD() {
        super(null);
        setup();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void setup() {
        guiscreenBackground = false;

        // Construct Hud with all elements
        resPane = new UIBackgroundContainer(this, 90, 55);
        resPane.setAnchor(Anchor.RIGHT | Anchor.BOTTOM);
        resPane.setColor(Integer.MIN_VALUE);
        resPane.setBackgroundAlpha(125);

        //////////////////////////////// LEFT COLUMN //////////////////////////////////////

        // Player Display Name
        title = new UILabel(this, ChatColor.AQUA + "Residence Info");
        title.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);
        title.setColor(0xffffffff);
        title.setSize(7, 7);
        title.setFontScale(0.8F);

        resName = new UILabel(this, ChatColor.WHITE + "Name: DocksArea");
        resName.setPosition(3, title.getY() + 8, Anchor.LEFT | Anchor.TOP);
        resName.setColor(0xffffffff);
        resName.setSize(7, 7);
        resName.setFontScale(0.7F);

        resOwner = new UILabel(this, ChatColor.WHITE + "Owner: ~Dockter");
        resOwner.setPosition(7, resName.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resOwner.setColor(0xffffffff);
        resOwner.setSize(7, 7);
        resOwner.setFontScale(0.7F);

        resOwnerOnline = new UILabel(this, ChatColor.WHITE + "Owner Last Online: NOW");
        resOwnerOnline.setPosition(7, resOwner.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resOwnerOnline.setColor(0xffffffff);
        resOwnerOnline.setSize(7, 7);
        resOwnerOnline.setFontScale(0.7F);

        resBank = new UILabel(this, ChatColor.WHITE + "Vault: $999,000.00");
        resBank.setPosition(3, resOwnerOnline.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resBank.setColor(0xffffffff);
        resBank.setSize(7, 7);
        resBank.setFontScale(0.7F);

        resLeaseCost = new UILabel(this, ChatColor.WHITE + "Lease Cost: $999,000.00");
        resLeaseCost.setPosition(3, resBank.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resLeaseCost.setColor(0xffffffff);
        resLeaseCost.setSize(7, 7);
        resLeaseCost.setFontScale(0.7F);

        resLeaseExpireTitle = new UILabel(this, ChatColor.WHITE + "Lease Expires:");
        resLeaseExpireTitle.setPosition(3, resLeaseCost.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resLeaseExpireTitle.setColor(0xffffffff);
        resLeaseExpireTitle.setSize(7, 7);
        resLeaseExpireTitle.setFontScale(0.7F);

        resLeaseExpire = new UILabel(this, ChatColor.WHITE + "Never");
        resLeaseExpire.setPosition(8, resLeaseExpireTitle.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resLeaseExpire.setColor(0xffffffff);
        resLeaseExpire.setSize(7, 7);
        resLeaseExpire.setFontScale(0.7F);

        resPane.add(title, resName, resOwner, resOwnerOnline, resBank, resLeaseCost, resLeaseExpireTitle, resLeaseExpire);
        addToScreen(resPane);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (!Configuration.DISPLAY_RESIDENCE_HUD || !ResidenceData.WITHIN_RESIDENCE || mc.isSingleplayer()) {
            return;
        }

        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            setWorldAndResolution(MINECRAFT, event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
            if (firstDraw) { // This is used to fix alignment issues.
            	title.setText(ChatColor.AQUA + "Residence Info");
            	firstDraw = false;
            }
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
        }
    }

    public void refreshFromData() {
        resName.setText("Name: " + ChatColor.GREEN + ResidenceData.NAME);
        if (ResidenceData.OWNER_NAME.equalsIgnoreCase("mcsnetworks")) {
            resOwner.setText("Owner: " + ChatColor.RED + "~Dockter");
        } else {
            resOwner.setText("Owner: " + ChatColor.RED + ResidenceData.OWNER_NAME);
        }
        resOwnerOnline.setText("Last Seen: " + ChatColor.LIGHT_PURPLE + ResidenceData.LAST_ONLINE);
        resBank.setText("Vault: " + ResidenceData.VAULT);
        resLeaseCost.setText("Lease Cost: " + ChatColor.BLUE + ResidenceData.LEASE_COST);
        resLeaseExpire.setText(ChatColor.YELLOW + ResidenceData.LEASE_EXPIRATION);
        int originalWidth = resPane.getWidth();
        if (resOwnerOnline.getWidth() + 20 > originalWidth) {
        	resPane.setSize(resOwnerOnline.getWidth() + 10, resPane.getHeight());
        }

        if (resLeaseExpire.getWidth() + 20 > originalWidth) {
        	resPane.setSize(resLeaseExpire.getWidth() + 10, resPane.getHeight());
        }
    }
}
