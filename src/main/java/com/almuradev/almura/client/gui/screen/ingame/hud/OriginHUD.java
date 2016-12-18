/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame.hud;

import com.almuradev.almura.client.gui.GuiConstants;
import com.almuradev.almura.client.gui.component.hud.UIDetailsPanel;
import com.almuradev.almura.client.gui.component.hud.UIStatsPanel;
import com.almuradev.almura.client.gui.component.hud.UIUserPanel;
import com.almuradev.almura.client.gui.component.hud.UIWorldPanel;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import net.malisis.core.client.gui.Anchor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OriginHUD extends AbstractHUD {

    private UIDetailsPanel detailsPanel;
    private UIStatsPanel statsPanel;
    private UIUserPanel userPanel;
    private UIWorldPanel worldPanel;

    @Override
    public void construct() {
        guiscreenBackground = false;

        this.renderer.setDefaultTexture(GuiConstants.VANILLA_ACHIEVEMENT_BACKGROUND_SPRITESHEET);

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

        addToScreen(this.userPanel);
        addToScreen(this.statsPanel);
        addToScreen(this.worldPanel);
        addToScreen(this.detailsPanel);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().player.world == null) {
            return;
        }

        this.statsPanel.updateHealth();
        this.statsPanel.updateArmor();
        this.statsPanel.updateHunger();
        this.statsPanel.updateAir();
        this.statsPanel.updateMountHealth();
        this.statsPanel.updatePanel();

        this.userPanel.updateCurrency();
        this.userPanel.updateExperience();
        this.userPanel.updateLevel();

        this.worldPanel.updateCompass();
        this.worldPanel.updateWorld();

        this.detailsPanel.updateCoordinates();
        this.detailsPanel.updatePlayerCount();
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
