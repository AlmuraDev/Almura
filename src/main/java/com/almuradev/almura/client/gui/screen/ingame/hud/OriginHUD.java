/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame.hud;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Constants;
import com.almuradev.almura.client.gui.component.hud.UIDebugPanel;
import com.almuradev.almura.client.gui.component.hud.UIDetailsPanel;
import com.almuradev.almura.client.gui.component.hud.UIStatsPanel;
import com.almuradev.almura.client.gui.component.hud.UIUserPanel;
import com.almuradev.almura.client.gui.component.hud.UIWorldPanel;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.configuration.type.ClientConfiguration;
import net.malisis.core.client.gui.Anchor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OriginHUD extends AbstractHUD {

    private final ClientConfiguration config = (ClientConfiguration) Almura.proxy.getPlatformConfigAdapter().getConfig();
    private UIDebugPanel debugPanel;
    private UIDetailsPanel detailsPanel;
    private UIStatsPanel statsPanel;
    private UIUserPanel userPanel;
    private UIWorldPanel worldPanel;

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.renderer.setDefaultTexture(Constants.Gui.SPRITE_SHEET_VANILLA_ADVANCEMENTS_WIDGETS);

        // User panel
        this.userPanel = new UIUserPanel(this, 124, 37);
        this.userPanel.setPosition(0, 0);

        // Stats panel
        this.statsPanel = new UIStatsPanel(this, 124, 49);
        this.statsPanel.setPosition(0, SimpleScreen.getPaddedY(this.userPanel, 2));

        // World panel
        this.worldPanel = new UIWorldPanel(this, 124, 33);
        this.worldPanel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        // Details panel
        this.detailsPanel = new UIDetailsPanel(this, 124, 37);
        this.detailsPanel.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);

        // Debug panel
        this.debugPanel = new UIDebugPanel(this, 155, 64);
        this.debugPanel.setPosition(0, SimpleScreen.getPaddedY(this.detailsPanel, 2), Anchor.TOP | Anchor.RIGHT);

        addToScreen(this.userPanel, this.statsPanel, this.worldPanel, this.detailsPanel, this.debugPanel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Set current alpha value
        userPanel.setAlpha(config.client.originHudOpacity);
        statsPanel.setAlpha(config.client.originHudOpacity);
        worldPanel.setAlpha(config.client.originHudOpacity);
        detailsPanel.setAlpha(config.client.originHudOpacity);
        debugPanel.setAlpha(config.client.originHudOpacity);

        debugPanel.setVisible(Minecraft.getMinecraft().gameSettings.showDebugInfo);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public int getOriginBossBarOffsetY() {
        return this.worldPanel.getHeight() + 12;
    }

    @Override
    public int getTabMenuOffsetY() {
        return this.worldPanel.getHeight() + 2;
    }

    @Override
    public int getPotionOffsetY() {
        return this.detailsPanel.getHeight() + 2;
    }
}
