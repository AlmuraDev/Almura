/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame.hud;

import com.almuradev.almura.Constants;
import com.almuradev.almura.client.gui.component.hud.UIDetailsPanel;
import com.almuradev.almura.client.gui.component.hud.UIStatsPanel;
import com.almuradev.almura.client.gui.component.hud.UITargetPanel;
import com.almuradev.almura.client.gui.component.hud.UIUserPanel;
import com.almuradev.almura.client.gui.component.hud.UIWorldPanel;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class OriginHUD extends AbstractHUD {

    private UIDetailsPanel detailsPanel;
    private UIStatsPanel statsPanel;
    private UIUserPanel userPanel;
    private UIWorldPanel worldPanel;
    @Nullable private UITargetPanel targetPanel;

    @Override
    public void construct() {
        guiscreenBackground = false;

        this.renderer.setDefaultTexture(Constants.Gui.VANILLA_ACHIEVEMENT_BACKGROUND_SPRITESHEET);

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

        if (this.targetPanel != null) {
            this.targetPanel.updateHealth();
            this.targetPanel.updateUsername();
            if (!this.targetPanel.getEntity().isEntityAlive()) {
                ((UIContainer) this.targetPanel.getParent()).remove(this.targetPanel);
                this.targetPanel = null;
            }
        }
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

    @SubscribeEvent
    public void onEntityAttack(LivingAttackEvent event) {
        final Entity source = event.getSource().getEntity();
        final EntityLivingBase target = event.getEntityLiving();

        if (this.targetPanel != null && target == this.targetPanel.getEntity()) {
            return;
        }

        if (source == Minecraft.getMinecraft().player) {
            this.targetPanel = new UITargetPanel(this, 124, 33, target);
            this.targetPanel.setPosition(0, SimpleScreen.getPaddedY(this.statsPanel, 2));

            addToScreen(this.targetPanel);
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        final EntityLivingBase target = event.getEntityLiving();

        if (this.targetPanel != null && target == this.targetPanel.getEntity()) {
            ((UIContainer) this.targetPanel.getParent()).remove(this.targetPanel);
            this.targetPanel = null;
        }
    }
}
