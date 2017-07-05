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
import com.almuradev.almura.client.gui.component.hud.UIPlayerListPanel;
import com.almuradev.almura.client.gui.component.hud.UIUserPanel;
import com.almuradev.almura.client.gui.component.hud.UIWorldPanel;
import com.almuradev.almura.client.gui.component.hud.debug.UIDebugBlockPanel;
import com.almuradev.almura.client.gui.component.hud.debug.UIDebugDetailsPanel;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.configuration.type.ClientConfiguration;
import com.almuradev.almura.util.MathUtil;
import net.malisis.core.client.gui.Anchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;


@SideOnly(Side.CLIENT)
public class OriginHUD extends AbstractHUD {

    private static final int padding = 1;
    private final ClientConfiguration config = (ClientConfiguration) Almura.proxy.getPlatformConfigAdapter().getConfig();
    private final Minecraft client = Minecraft.getMinecraft();
    private UIBossBarPanel bossBarPanel;
    private UIDebugDetailsPanel debugDetailsPanel;
    private UIDebugBlockPanel debugBlockPanel;
    private UIDetailsPanel detailsPanel;
    private UIPlayerListPanel playerListPanel;
    private UIUserPanel userPanel;
    private UIWorldPanel worldPanel;

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.renderer.setDefaultTexture(Constants.Gui.SPRITE_SHEET_VANILLA_CONTAINER_INVENTORY);

        // User panel
        this.userPanel = new UIUserPanel(this, 124, 37);
        this.userPanel.setPosition(0, 0);

        // Debug block panel
        this.debugBlockPanel = new UIDebugBlockPanel(this, 124, 45);
        this.debugBlockPanel.setPosition(0, SimpleScreen.getPaddedY(this.userPanel, padding));

        // World panel
        this.worldPanel = new UIWorldPanel(this, 124, 33);
        this.worldPanel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        // Details panel
        this.detailsPanel = new UIDetailsPanel(this, 124, 37);
        this.detailsPanel.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);

        // Debug details panel
        this.debugDetailsPanel = new UIDebugDetailsPanel(this, 155, 64);
        this.debugDetailsPanel.setPosition(0, SimpleScreen.getPaddedY(this.detailsPanel, padding), Anchor.TOP | Anchor.RIGHT);

        // Boss bar panel
        this.bossBarPanel = new UIBossBarPanel(this, 124, 33);
        this.bossBarPanel.setPosition(0, SimpleScreen.getPaddedY(this.worldPanel, padding), Anchor.TOP | Anchor.CENTER);

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
        // Set current alpha value
        userPanel.setAlpha(config.client.originHudOpacity);
        worldPanel.setAlpha(config.client.originHudOpacity);
        detailsPanel.setAlpha(config.client.originHudOpacity);

        // Show debug panels if necessary
        final boolean isDebugEnabled = this.client.gameSettings.showDebugInfo;
        this.debugDetailsPanel.setVisible(isDebugEnabled);
        if (isDebugEnabled) {
            // Get proper position based on what potion effects are being shown
            int yOffset = SimpleScreen.getPaddedY(this.detailsPanel, padding);
            if (this.client.player.getActivePotionEffects().stream().anyMatch(potion -> potion.getPotion().isBeneficial())) {
                yOffset += 25; // 24 for potion icon, 2 for padding
            }
            if (this.client.player.getActivePotionEffects().stream().anyMatch(potion -> !potion.getPotion().isBeneficial())) {
                yOffset += 25; // 24 for potion icon, 2 for padding
            }
            // Debug block panel
            this.debugBlockPanel.setPosition(0, SimpleScreen.getPaddedY(this.userPanel, padding));
            this.debugBlockPanel.setAlpha(config.client.originHudOpacity);

            // Debug details panel
            this.debugDetailsPanel.setPosition(0, yOffset);
            this.debugDetailsPanel.setAlpha(config.client.originHudOpacity);
        }

        // Show boss panel if necessary
        final GuiIngame guiIngame = this.client.ingameGUI;
        if (guiIngame != null) {
            this.bossBarPanel.setVisible(!((IMixinGuiBossOverlay) guiIngame.getBossOverlay()).getBossInfo().isEmpty());
            this.bossBarPanel.setAlpha(config.client.originHudOpacity);
        }

        // Show player list if necessary
        this.playerListPanel.setVisible(this.client.gameSettings.keyBindPlayerList.isKeyDown());

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public int getTabMenuOffsetY() {
        return this.worldPanel.getHeight() + padding;
    }

    @Override
    public int getPotionOffsetY() {
        return this.detailsPanel.getHeight() + padding;
    }
}
