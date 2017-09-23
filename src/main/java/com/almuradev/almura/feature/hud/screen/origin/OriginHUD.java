/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin;

import com.almuradev.almura.asm.mixin.interfaces.IMixinGuiBossOverlay;
import com.almuradev.almura.core.client.config.ClientCategory;
import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.almura.feature.hud.screen.AbstractHUD;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIBossBarPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIDetailsPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIPlayerListPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIUserPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIWorldPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.debug.BlockDebugPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.debug.InformationDebugPanel;
import com.almuradev.shared.client.GuiConfig;
import com.almuradev.shared.client.ui.screen.SimpleScreen;
import com.almuradev.shared.config.ConfigurationAdapter;
import com.almuradev.shared.util.MathUtil;
import net.malisis.core.client.gui.Anchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import javax.inject.Inject;


@SideOnly(Side.CLIENT)
public class OriginHUD extends AbstractHUD {

    private static final int PADDING = 1;
    private final ConfigurationAdapter<ClientConfiguration> config;
    private final Minecraft client = Minecraft.getMinecraft();
    private UIBossBarPanel bossBarPanel;
    private InformationDebugPanel debugDetailsPanel;
    private BlockDebugPanel debugBlockPanel;
    private UIDetailsPanel detailsPanel;
    private UIPlayerListPanel playerListPanel;
    private UIUserPanel userPanel;
    private UIWorldPanel worldPanel;

    @Inject
    public OriginHUD(final ConfigurationAdapter<ClientConfiguration> config) {
        this.config = config;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.renderer.setDefaultTexture(GuiConfig.SpriteSheet.VANILLA_CONTAINER_INVENTORY);

        // User panel
        this.userPanel = new UIUserPanel(this, 124, 37);
        this.userPanel.setPosition(0, 0);

        // Debug block panel
        this.debugBlockPanel = new BlockDebugPanel(this, 124, 45);
        this.debugBlockPanel.setPosition(0, SimpleScreen.getPaddedY(this.userPanel, PADDING));

        // World panel
        this.worldPanel = new UIWorldPanel(this, 124, 25);
        this.worldPanel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        // Details panel
        this.detailsPanel = new UIDetailsPanel(this, 124, 37);
        this.detailsPanel.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);

        // Debug details panel
        this.debugDetailsPanel = new InformationDebugPanel(this, 155, 64);
        this.debugDetailsPanel.setPosition(0, SimpleScreen.getPaddedY(this.detailsPanel, PADDING), Anchor.TOP | Anchor.RIGHT);

        // Boss bar panel
        this.bossBarPanel = new UIBossBarPanel(this, 124, 33);
        this.bossBarPanel.setPosition(0, SimpleScreen.getPaddedY(this.worldPanel, PADDING), Anchor.TOP | Anchor.CENTER);

        // Player list panel
        this.playerListPanel = new UIPlayerListPanel(this, 150, 16);
        this.playerListPanel.setPosition(0, 40, Anchor.TOP | Anchor.CENTER);

        addToScreen(this.userPanel, this.debugBlockPanel, this.worldPanel, this.detailsPanel, this.debugDetailsPanel, this.bossBarPanel,
                this.playerListPanel);
    }

    public boolean handleScroll() {
        if (this.playerListPanel.isVisible()) {
            this.playerListPanel.onScrollWheel(Mouse.getEventX(), Mouse.getEventY(), (int) MathUtil.squash(Mouse.getEventDWheel(), -1, 1));
            return true;
        }
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final ClientCategory category = this.config.getConfig().client;
        // Set current alpha value
        this.userPanel.setAlpha(category.originHudOpacity);
        this.worldPanel.setAlpha(category.originHudOpacity);
        this.detailsPanel.setAlpha(category.originHudOpacity);

        // Show debug panels if necessary
        final boolean isDebugEnabled = this.client.gameSettings.showDebugInfo;
        this.debugDetailsPanel.setVisible(isDebugEnabled);
        if (isDebugEnabled) {
            // Get proper position based on what potion effects are being shown
            int yOffset = SimpleScreen.getPaddedY(this.detailsPanel, PADDING);
            if (this.client.player.getActivePotionEffects().stream().anyMatch(potion -> potion.getPotion().isBeneficial())) {
                yOffset += 25; // 24 for potion icon, 2 for padding
            }
            if (this.client.player.getActivePotionEffects().stream().anyMatch(potion -> !potion.getPotion().isBeneficial())) {
                yOffset += 25; // 24 for potion icon, 2 for padding
            }
            // Debug block panel
            this.debugBlockPanel.setPosition(0, SimpleScreen.getPaddedY(this.userPanel, PADDING));
            this.debugBlockPanel.setAlpha(category.originHudOpacity);

            // Debug details panel
            this.debugDetailsPanel.setPosition(0, yOffset);
            this.debugDetailsPanel.setAlpha(category.originHudOpacity);
        }

        // Show boss panel if necessary
        final GuiIngame guiIngame = this.client.ingameGUI;
        if (guiIngame != null) {
            this.bossBarPanel.setVisible(!((IMixinGuiBossOverlay) guiIngame.getBossOverlay()).getBossInfo().isEmpty());
            this.bossBarPanel.setAlpha(category.originHudOpacity);
        }

        // Show player list if necessary
        this.playerListPanel.setVisible(this.client.gameSettings.keyBindPlayerList.isKeyDown());

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public int getTabMenuOffsetY() {
        return this.worldPanel.getHeight() + PADDING;
    }

    @Override
    public int getPotionOffsetY() {
        return this.detailsPanel.getHeight() + PADDING;
    }
}
