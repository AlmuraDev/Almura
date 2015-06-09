/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame.residence;

import com.almuradev.almura.client.FontRenderOptionsConstants;
import com.almuradev.almura.client.gui.ingame.HUDData;
import com.almuradev.almurasdk.client.gui.SimpleGui;
import com.almuradev.almurasdk.util.Colors;
import com.almuradev.almurasdk.util.FontRenderOptionsBuilder;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;

public class IngameResidenceHUD extends SimpleGui {

    public UILabel title, resName, resOwner, resOwnerOnline, resBank, resLeaseCost, resLeaseExpireTitle, resLeaseExpire;
    public UIBackgroundContainer resPane;

    public IngameResidenceHUD() {
        guiscreenBackground = false;
    }

    @Override
    public void construct() {

        // Construct Hud with all elements
        resPane = new UIBackgroundContainer(this, 90, 55);
        resPane.setAnchor(Anchor.RIGHT | Anchor.BOTTOM);
        resPane.setColor(Integer.MIN_VALUE);
        resPane.setBackgroundAlpha(125);

        //////////////////////////////// LEFT COLUMN //////////////////////////////////////

        // Player Display Name
        title = new UILabel(this, Colors.AQUA + "Residence Info");
        title.setPosition(0, 1, Anchor.CENTER | Anchor.TOP);
        title.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.8f).build());

        resName = new UILabel(this, Colors.WHITE + "Name: DocksArea");
        resName.setPosition(3, title.getY() + 8, Anchor.LEFT | Anchor.TOP);
        resName.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.7f).build());

        resOwner = new UILabel(this, Colors.WHITE + "Owner: ~Dockter");
        resOwner.setPosition(7, resName.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resOwner.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.7f).build());

        resOwnerOnline = new UILabel(this, Colors.WHITE + "Owner Last Online: NOW");
        resOwnerOnline.setPosition(7, resOwner.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resOwnerOnline.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.7f).build());

        resBank = new UILabel(this, Colors.WHITE + "Vault: $999,000.00");
        resBank.setPosition(3, resOwnerOnline.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resBank.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.7f).build());

        resLeaseCost = new UILabel(this, Colors.WHITE + "Lease Cost: $999,000.00");
        resLeaseCost.setPosition(3, resBank.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resLeaseCost.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.7f).build());

        resLeaseExpireTitle = new UILabel(this, Colors.WHITE + "Lease Expires:");
        resLeaseExpireTitle.setPosition(3, resLeaseCost.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resLeaseExpireTitle.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.7f).build());

        resLeaseExpire = new UILabel(this, Colors.WHITE + "Never");
        resLeaseExpire.setPosition(8, resLeaseExpireTitle.getY() + 6, Anchor.LEFT | Anchor.TOP);
        resLeaseExpire.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).fontScale(0.7f).build());

        resPane.add(title, resName, resOwner, resOwnerOnline, resBank, resLeaseCost, resLeaseExpireTitle, resLeaseExpire);
        addToScreen(resPane);
    }

    public void updateWidgets() {
        if (HUDData.OWNER_NAME.equalsIgnoreCase("mcsnetworks")) {
            resOwner.setText("Owner: " + Colors.RED + "~Dockter");
        } else {
            resOwner.setText("Owner: " + Colors.RED + HUDData.OWNER_NAME);
        }
        resOwnerOnline.setText("Last Seen: " + Colors.LIGHT_PURPLE + HUDData.LAST_ONLINE);
        resBank.setText("Vault: " + HUDData.VAULT);
        resLeaseCost.setText("Lease Cost: " + Colors.BLUE + HUDData.LEASE_COST);
        resLeaseExpire.setText(Colors.YELLOW + HUDData.LEASE_EXPIRATION);
        int originalWidth = resPane.getWidth();
        if (resOwnerOnline.getWidth() + 20 > originalWidth) {
            resPane.setSize(resOwnerOnline.getWidth() + 10, resPane.getHeight());
        }

        if (resLeaseExpire.getWidth() + 20 > originalWidth) {
            resPane.setSize(resLeaseExpire.getWidth() + 10, resPane.getHeight());
        }

        // Window re-arrangement if server-owned.
        if (HUDData.OWNER_NAME.equalsIgnoreCase("almura_admin")) {
            title.setText("Protected Server-Owned Area");
            resName.setText("Area Name: " + Colors.GREEN + HUDData.NAME);
            resOwner.setVisible(false);
            resOwnerOnline.setVisible(false);
            resBank.setVisible(false);
            resLeaseCost.setVisible(false);
            resLeaseExpire.setVisible(true);
            resPane.setSize(90, 20);
        } else {
            title.setText("Residence Info");
            resName.setText("Name: " + Colors.GREEN + HUDData.NAME);
            resOwner.setVisible(true);
            resOwnerOnline.setVisible(true);
            resBank.setVisible(true);
            resLeaseCost.setVisible(true);
            resLeaseExpire.setVisible(true);
            resPane.setSize(resPane.getWidth(), 55);
        }
    }
}
