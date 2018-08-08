/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.asm.ClientStaticAccess;
import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.feature.hud.screen.origin.UIAvatarImage;
import com.almuradev.almura.feature.hud.screen.origin.component.UIXPOrbImage;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.Fonts;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.util.MathUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.icon.Icon;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

import java.text.DecimalFormat;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class UIUserPanel extends AbstractPanel {

    @Inject private static HeadUpDisplay hudData;

    private final int baseHeight;
    private final UIImage currencyImage;
    private final UIAvatarImage userAvatarImage;
    private final UIXPOrbImage xpOrbImage;
    private final UILabel currencyLabel, levelLabel, usernameLabel;
    private final UIPropertyBar airBar, armorBar, experienceBar, healthBar, hungerBar, mountHealthBar, staminaBar;
    private final DecimalFormat df = new DecimalFormat("0.##");
    @Nullable private UIComponent lastVisibleComponent;

    private int update = 60;

    public UIUserPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        this.baseHeight = height;

        // Avatar
        this.userAvatarImage = new UIAvatarImage(gui, this.client.player.getPlayerInfo());
        this.userAvatarImage.setPosition(2, 2);
        this.userAvatarImage.setSize(16, 16);

        // Username
        this.usernameLabel = new UILabel(gui, TextFormatting.WHITE + this.client.player.getDisplayName().getFormattedText());
        this.usernameLabel.setPosition(SimpleScreen.getPaddedX(this.userAvatarImage, 3), 2);

        // Level
        this.levelLabel = new UILabel(gui, "");
        this.levelLabel.setPosition(SimpleScreen.getPaddedX(this.userAvatarImage, 3), SimpleScreen.getPaddedY(this.usernameLabel, 0));
        this.levelLabel.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.7F));

        // Currency
        this.currencyImage = new UIImage(gui, MalisisGui.BLOCK_TEXTURE, Icon.from(Items.EMERALD));
        this.currencyImage.setSize(8, 8);
        this.currencyLabel = new UILabel(gui, "");
        this.currencyLabel.setVisible(false);
        this.currencyImage.setVisible(false);

        // Bars
        final int barWidth = this.width - 10;
        final int barHeight = 9;

        // XP Orb Image
        this.xpOrbImage = new UIXPOrbImage(gui);
        this.xpOrbImage.setSize(9, 9);
        this.xpOrbImage.setPosition(2, SimpleScreen.getPaddedY(this.levelLabel, 3));

        // TODO: Toggle for font scaling? Looks horrid on GUI Size: Normal

        // Experience
        this.experienceBar = new UIPropertyBar(gui, barWidth - 11, 7)
                .setPosition(6, this.xpOrbImage.getY(), Anchor.TOP | Anchor.CENTER)
                .setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
                .setColor(org.spongepowered.api.util.Color.ofRgb(0, 150, 0).getRgb());

        // Health
        // TODO Show effects (or icon) for golden apple, wither, poison, etc
        this.healthBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setColor(org.spongepowered.api.util.Color.ofRgb(187, 19, 19).getRgb())
                .setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
                .setIcons(GuiConfig.Icon.VANILLA_HEART_BACKGROUND, GuiConfig.Icon.VANILLA_HEART_FOREGROUND);
        this.healthBar.setTooltip("Health Value");

        // Armor
        this.armorBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setColor(org.spongepowered.api.util.Color.ofRgb(102, 103, 109).getRgb())
                .setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
                .setBackgroundIcon(GuiConfig.Icon.VANILLA_ARMOR);
        this.armorBar.setTooltip("Armor Value");


        // Hunger
        this.hungerBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setColor(org.spongepowered.api.util.Color.ofRgb(137, 89, 47).getRgb())
                .setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
                .setIcons(GuiConfig.Icon.VANILLA_HUNGER_BACKGROUND, GuiConfig.Icon.VANILLA_HUNGER_FOREGROUND);

        // Stamina
        this.staminaBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setColor(org.spongepowered.api.util.Color.ofRgb(0, 148, 255).getRgb())
                .setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
                .setBackgroundIcon(GuiConfig.Icon.STAMINA)
                .setSpritesheet(GuiConfig.SpriteSheet.ALMURA); // You must call this if you are loading icon within the TexturePack when in-game.

        // Air
        this.airBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setColor(org.spongepowered.api.util.Color.ofRgb(0, 148, 255).getRgb())
                .setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
                .setBackgroundIcon(GuiConfig.Icon.VANILLA_AIR);

        // Mount Health
        this.mountHealthBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setColor(org.spongepowered.api.util.Color.ofRgb(239, 126, 74).getRgb())
                .setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
                .setBackgroundIcon(GuiConfig.Icon.VANILLA_MOUNT);

        this.add(this.userAvatarImage, this.usernameLabel, this.xpOrbImage, this.levelLabel, this.experienceBar, this.currencyImage,
                this.currencyLabel, this.healthBar, this.armorBar, this.hungerBar, this.staminaBar, this.airBar, this.mountHealthBar);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (this.client.player == null || this.client.player.world == null) {
            return;
        }
        super.drawForeground(renderer, mouseX, mouseY, partialTick);

        // Reset variables
        this.height = this.baseHeight;
        this.lastVisibleComponent = null;

        if (--this.update == 0) {
            // Update
            this.updateUserImage();
            this.updateDisplayName();
            this.updateCurrency();
            this.updateLevel();

            // Update property bars, call order defines visual layout
            this.updateExperience();
            this.updateHealth();
            this.updateArmor();
            this.updateHunger();

            // Update temporary property bars, call order defines visual layout
            this.updateStamina();
            this.updateAir();
            this.updateMountHealth();
            this.update = 60;
        }
    }

    private void updateDisplayName() {
        if (this.client.player != null) {
            this.usernameLabel.setText(TextFormatting.WHITE + this.client.player.getDisplayName().getFormattedText());
        }
    }

    private void updateUserImage() {
        if (this.client.player != null && this.client.player.hasPlayerInfo()) {
            this.userAvatarImage.setPlayerInfo(this.client.player.getPlayerInfo());
        }
    }

    private void updateCurrency() {
        if (hudData.isEconomyPresent) {
            //this.currencyImage.setVisible(true);
            this.currencyLabel.setVisible(true);
            this.currencyLabel.setText("$ " + hudData.economyAmount);
            this.currencyLabel.setFontOptions(Fonts.colorAndScale(FontColors.GOLD, 0.7F));
            this.currencyLabel.setPosition(-4, SimpleScreen.getPaddedY(this.usernameLabel, 1),Anchor.TOP | Anchor.RIGHT);
            this.currencyImage.setPosition(-(this.currencyLabel.getWidth() + 10), SimpleScreen.getPaddedY(this.usernameLabel, -1), Anchor.TOP | Anchor.RIGHT);
            this.currencyImage.setVisible(false);
        }
    }

    private void updateLevel() {
        this.levelLabel.setText("Lv. " + this.client.player.experienceLevel);
    }

    private void updateExperience() {
        final int experienceCap = this.client.player.xpBarCap();
        final int experience = (int) (this.client.player.experience * experienceCap);

        this.updateBarProperties(this.experienceBar, experience, experienceCap, false, false);

        this.xpOrbImage.setPosition(2, this.lastVisibleComponent != null
                ? SimpleScreen.getPaddedY(this.lastVisibleComponent, 2)
                : SimpleScreen.getPaddedY(this.levelLabel, 3));
        this.experienceBar.setPosition(6, this.xpOrbImage.getY(), Anchor.TOP | Anchor.CENTER);
        this.lastVisibleComponent = this.xpOrbImage;
    }

    private void updateHealth() {
        this.updateBarProperties(this.healthBar, this.client.player.getHealth(), this.client.player.getMaxHealth());
    }

    private void updateArmor() {
        int maxArmor = 0;
        int currentDamage = 0;
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND) {
                continue;
            }
            final ItemStack stack = this.client.player.getItemStackFromSlot(slot);
            if (stack.getItem() instanceof ItemArmor) {
                maxArmor += ((ItemArmor) stack.getItem()).getArmorMaterial().getDurability(slot);
                currentDamage += stack.getItem().getDamage(stack);
            }
        }
        this.updateBarProperties(this.armorBar, maxArmor - currentDamage, maxArmor);
    }

    private void updateHunger() {
        this.updateBarProperties(this.hungerBar, this.client.player.getFoodStats().getFoodLevel(), 20f);
    }

    private void updateStamina() {
        final float staminaLevel = this.client.player.getFoodStats().getSaturationLevel();

        this.staminaBar.setVisible(staminaLevel > 0);
        if (this.staminaBar.isVisible()) {
            this.updateBarProperties(this.staminaBar, staminaLevel, 20f);
        }
    }

    private void updateAir() {
        this.airBar.setVisible(this.client.player.isInsideOfMaterial(Material.WATER));

        // TODO Hardcoded to not care above 300, if we can do this better in the future then we should do so
        if (this.airBar.isVisible()) {
            final int air = Math.max(this.client.player.getAir(), 0);
            this.updateBarProperties(this.airBar, air, 300);
        }
    }

    private void updateMountHealth() {
        final Entity entity = this.client.player.getRidingEntity();
        final boolean entityIsLiving = entity != null && entity instanceof EntityLivingBase;
        this.mountHealthBar.setVisible(entityIsLiving);
        if (entityIsLiving) {
            final EntityLivingBase ridingEntityLivingBase = (EntityLivingBase) entity;

            this.updateBarProperties(this.mountHealthBar, ridingEntityLivingBase.getHealth(), ridingEntityLivingBase.getMaxHealth());
        }
    }

    private void updateBarProperties(UIPropertyBar propertyBar, float value, float maxValue) {
        this.updateBarProperties(propertyBar, value, maxValue, true, true);
    }

    private void updateBarProperties(UIPropertyBar propertyBar, float value, float maxValue, boolean updatePosition, boolean updateLastVisible) {
        // Update text
        Text text = ClientStaticAccess.configAdapter.get().general.displayNumericHUDValues
                ? Text.of(this.df.format(value) + "/" + this.df.format(maxValue))
                : Text.EMPTY;
        propertyBar.setText(text);

        // Update amount value
        propertyBar.setAmount(MathUtil.convertToRange(value, 0, maxValue, 0f, 1f));

        // Pad against last visible component
        if (updatePosition) {
            propertyBar.setPosition(0, this.lastVisibleComponent != null
                    ? SimpleScreen.getPaddedY(this.lastVisibleComponent, 1)
                    : SimpleScreen.getPaddedY(this.levelLabel, 3), Anchor.TOP | Anchor.CENTER);
        }

        // Update last visible component
        if (updateLastVisible) {
            this.lastVisibleComponent = propertyBar;
        }

        // Update height
        this.height += 10;
    }
}
