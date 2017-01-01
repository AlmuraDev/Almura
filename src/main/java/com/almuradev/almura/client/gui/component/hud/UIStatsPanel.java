/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud;

import com.almuradev.almura.Constants;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.util.MathUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

@SideOnly(Side.CLIENT)
public class UIStatsPanel extends UIHUDPanel {

    private final UIPropertyBar airBar, armorBar, healthBar, hungerBar, mountHealthBar;

    public UIStatsPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        final int barWidth = this.width - 10;
        final int barHeight = 9;

        // Health
        // TODO Show effects (or icon) for golden apple, wither, poison, etc
        this.healthBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setPosition(0, 2, Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(187, 19, 19).getRgb())
                .setIcons(Constants.Gui.ICON_VANILLA_HEART_BACKGROUND, Constants.Gui.ICON_VANILLA_HEART_FOREGROUND);

        // Armor
        this.armorBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setPosition(0, SimpleScreen.getPaddedY(this.healthBar, 1), Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(184, 185, 196).getRgb())
                .setBackgroundIcon(Constants.Gui.ICON_VANILLA_ARMOR);

        // Hunger
        this.hungerBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setPosition(0, SimpleScreen.getPaddedY(this.armorBar, 1), Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(137, 89, 47).getRgb())
                .setIcons(Constants.Gui.ICON_VANILLA_HUNGER_BACKGROUND, Constants.Gui.ICON_VANILLA_HUNGER_FOREGROUND);

        // Air
        this.airBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setPosition(0, SimpleScreen.getPaddedY(this.hungerBar, 1), Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(0, 148, 255).getRgb())
                .setBackgroundIcon(Constants.Gui.ICON_VANILLA_AIR);

        // Mount Health
        this.mountHealthBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setPosition(0, SimpleScreen.getPaddedY(this.airBar, 1), Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(239, 126, 74).getRgb())
                .setBackgroundIcon(Constants.Gui.ICON_VANILLA_MOUNT);

        this.add(this.healthBar, this.armorBar, this.hungerBar, this.airBar, this.mountHealthBar);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().player.world == null) {
            return;
        }
        super.drawForeground(renderer, mouseX, mouseY, partialTick);
        this.updateHealth();
        this.updateArmor();
        this.updateHunger();
        this.updateAir();
        this.updateMountHealth();
        this.updatePanel();
    }

    public void updateHealth() {
        final float health = Minecraft.getMinecraft().player.getHealth();
        final float maxHealth = Minecraft.getMinecraft().player.getMaxHealth();
        this.healthBar.setAmount(MathUtil.convertToRange(health,0f, maxHealth, 0f, 1f));
        this.healthBar.setText(Text.of(String.format("%.0f/%.0f", health, maxHealth)));
    }

    public void updateArmor() {
        int maxArmor = 0;
        int currentArmor = 0;
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND) {
                continue;
            }
            final ItemStack stack = Minecraft.getMinecraft().player.getItemStackFromSlot(slot);
            if (stack.getItem() instanceof ItemArmor) {
                maxArmor += ((ItemArmor) stack.getItem()).getArmorMaterial().getDurability(slot);
                currentArmor += stack.getItem().getDamage(stack);
            }
        }
        this.armorBar.setText(Text.of(String.format("%d/%d", currentArmor, maxArmor)));
        this.armorBar.setAmount(MathUtil.convertToRange(maxArmor - currentArmor, 0, maxArmor, 0f, 1f));
    }

    public void updateHunger() {
        final float foodLevel = Minecraft.getMinecraft().player.getFoodStats().getFoodLevel();
        this.hungerBar.setText(Text.of(String.format("%.0f/%d", foodLevel, 20)));
        this.hungerBar.setAmount(MathUtil.convertToRange(foodLevel, 0, 20, 0f, 1f));
    }

    public void updateAir() {
        this.airBar.setVisible(Minecraft.getMinecraft().player.isInsideOfMaterial(Material.WATER));

        // TODO Hardcoded to not care above 300, if we can do this better in the future then we should do so
        if (this.airBar.isVisible()) {
            final int air = Math.max(Minecraft.getMinecraft().player.getAir(), 0);
            this.airBar.setText(Text.of(String.format("%d/%d", air, 300)));
            this.airBar.setAmount(MathUtil.convertToRange(air, 0, 300, 0f, 1f));
        }
    }

    public void updateMountHealth() {
        final EntityLivingBase ridingEntityLivingBase = (EntityLivingBase) Minecraft.getMinecraft().player.getRidingEntity();
        this.mountHealthBar.setVisible(ridingEntityLivingBase != null);
        if (this.mountHealthBar.isVisible() && ridingEntityLivingBase != null) {
            final float health = ridingEntityLivingBase.getHealth();
            final float maxHealth = ridingEntityLivingBase.getMaxHealth();
            this.mountHealthBar.setText(Text.of(String.format("%.0f/%.0f", health, maxHealth)));
            this.mountHealthBar.setAmount(MathUtil.convertToRange(health, 0, maxHealth, 0f, 1f));
            this.mountHealthBar.setPosition(0, SimpleScreen.getPaddedY(this.airBar.isVisible() ? this.airBar : this.hungerBar, 1));
        }
    }

    public void updatePanel() {
        int newHeight = 0;
        newHeight += this.healthBar.isVisible() ? this.healthBar.getHeight() : 0;
        newHeight += this.armorBar.isVisible() ? this.armorBar.getHeight() : 0;
        newHeight += this.hungerBar.isVisible() ? this.hungerBar.getHeight() : 0;
        newHeight += this.airBar.isVisible() ? this.airBar.getHeight() : 0;
        newHeight += this.mountHealthBar.isVisible() ? this.mountHealthBar.getHeight() : 0;
        this.setVisible(newHeight != 0);

        this.setSize(this.width, newHeight + 12);
    }
}
