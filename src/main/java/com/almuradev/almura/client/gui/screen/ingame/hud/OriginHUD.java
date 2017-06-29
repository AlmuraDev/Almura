/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame.hud;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Constants;
import com.almuradev.almura.asm.mixin.interfaces.IMixinGuiBossOverlay;
import com.almuradev.almura.client.gui.component.hud.UIBossBarPanel;
import com.almuradev.almura.client.gui.component.hud.UIDetailsPanel;
import com.almuradev.almura.client.gui.component.hud.UIStatsPanel;
import com.almuradev.almura.client.gui.component.hud.UIUserPanel;
import com.almuradev.almura.client.gui.component.hud.UIWorldPanel;
import com.almuradev.almura.client.gui.component.hud.debug.UIDebugBlockPanel;
import com.almuradev.almura.client.gui.component.hud.debug.UIDebugDetailsPanel;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.configuration.type.ClientConfiguration;
import net.malisis.core.client.gui.Anchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OriginHUD extends AbstractHUD {

    private final ClientConfiguration config = (ClientConfiguration) Almura.proxy.getPlatformConfigAdapter().getConfig();
    private UIBossBarPanel bossBarPanel;
    private UIDebugDetailsPanel debugDetailsPanel;
    private UIDebugBlockPanel debugBlockPanel;
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

        // Debug block panel
        this.debugBlockPanel = new UIDebugBlockPanel(this, 124, 45);
        this.debugBlockPanel.setPosition(0, SimpleScreen.getPaddedY(this.statsPanel, 2));

        // World panel
        this.worldPanel = new UIWorldPanel(this, 124, 33);
        this.worldPanel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        // Details panel
        this.detailsPanel = new UIDetailsPanel(this, 124, 37);
        this.detailsPanel.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);

        // Debug details panel
        this.debugDetailsPanel = new UIDebugDetailsPanel(this, 155, 64);
        this.debugDetailsPanel.setPosition(0, SimpleScreen.getPaddedY(this.detailsPanel, 2), Anchor.TOP | Anchor.RIGHT);

        // Boss bar panel
        this.bossBarPanel = new UIBossBarPanel(this, 124, 33);
        this.bossBarPanel.setPosition(0, SimpleScreen.getPaddedY(this.worldPanel, 2), Anchor.TOP | Anchor.CENTER);

        addToScreen(this.userPanel, this.statsPanel, this.debugBlockPanel, this.worldPanel, this.detailsPanel, this.debugDetailsPanel,
                this.bossBarPanel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Set current alpha value
        userPanel.setAlpha(config.client.originHudOpacity);
        statsPanel.setAlpha(config.client.originHudOpacity);
        worldPanel.setAlpha(config.client.originHudOpacity);
        detailsPanel.setAlpha(config.client.originHudOpacity);

        // Show debug panels if necessary
        final boolean isDebugEnabled = Minecraft.getMinecraft().gameSettings.showDebugInfo;
        this.debugDetailsPanel.setVisible(isDebugEnabled);
        if (isDebugEnabled) {
            // Get proper position based on what potion effects are being shown
            int yOffset = SimpleScreen.getPaddedY(this.detailsPanel, 2);
            if (Minecraft.getMinecraft().player.getActivePotionEffects().stream().anyMatch(potion -> potion.getPotion().isBeneficial())) {
                yOffset += 26; // 24 for potion icon, 2 for padding
            }
            if (Minecraft.getMinecraft().player.getActivePotionEffects().stream().anyMatch(potion -> !potion.getPotion().isBeneficial())) {
                yOffset += 26; // 24 for potion icon, 2 for padding
            }
            // Debug block panel
            this.debugBlockPanel.setPosition(0, SimpleScreen.getPaddedY(this.statsPanel, 2));
            this.debugBlockPanel.setAlpha(config.client.originHudOpacity);

            // Debug details panel
            this.debugDetailsPanel.setPosition(0, yOffset);
            this.debugDetailsPanel.setAlpha(config.client.originHudOpacity);
        }

        // Show boss panel if necessary
        final GuiIngame guiIngame = Minecraft.getMinecraft().ingameGUI;
        if (guiIngame != null) {
            this.bossBarPanel.setVisible(!((IMixinGuiBossOverlay) (Object) guiIngame.getBossOverlay()).getBossInfo().isEmpty());
        }

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
