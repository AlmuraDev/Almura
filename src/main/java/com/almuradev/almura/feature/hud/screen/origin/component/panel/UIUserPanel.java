/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.asm.StaticAccess;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

import java.text.DecimalFormat;

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
        this.xpOrbImage.setPosition(2, SimpleScreen.getPaddedY(this.levelLabel, 2));

        // Experience
        this.experienceBar = new UIPropertyBar(gui, barWidth - 11, 7)
                .setPosition(6, this.xpOrbImage.getY(), Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(0, 150, 0).getRgb());

        // Health
        // TODO Show effects (or icon) for golden apple, wither, poison, etc
        this.healthBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setPosition(0, SimpleScreen.getPaddedY(this.experienceBar, 3), Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(187, 19, 19).getRgb())
                .setIcons(GuiConfig.Icon.VANILLA_HEART_BACKGROUND, GuiConfig.Icon.VANILLA_HEART_FOREGROUND);

        // Armor
        this.armorBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setPosition(0, SimpleScreen.getPaddedY(this.healthBar, 1), Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(102, 103, 109).getRgb())
                .setBackgroundIcon(GuiConfig.Icon.VANILLA_ARMOR);

        // Hunger
        this.hungerBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setPosition(0, SimpleScreen.getPaddedY(this.armorBar, 1), Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(137, 89, 47).getRgb())
                .setIcons(GuiConfig.Icon.VANILLA_HUNGER_BACKGROUND, GuiConfig.Icon.VANILLA_HUNGER_FOREGROUND);

        // Stamina
        this.staminaBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setPosition(0, SimpleScreen.getPaddedY(this.hungerBar, 1), Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(0, 148, 255).getRgb())
                .setBackgroundIcon(GuiConfig.Icon.STAMINA)
                .setSpritesheet(GuiConfig.SpriteSheet.ALMURA); // You must call this if you are loading icon within the TexturePack when in-game.

        // Air
        this.airBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setPosition(0, SimpleScreen.getPaddedY(this.staminaBar, 1), Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(0, 148, 255).getRgb())
                .setBackgroundIcon(GuiConfig.Icon.VANILLA_AIR);

        // Mount Health
        this.mountHealthBar = new UIPropertyBar(gui, barWidth, barHeight)
                .setPosition(0, SimpleScreen.getPaddedY(this.airBar, 1), Anchor.TOP | Anchor.CENTER)
                .setColor(org.spongepowered.api.util.Color.ofRgb(239, 126, 74).getRgb())
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

        // Reset height
        this.height = this.baseHeight;

        // Update
        this.updateUserImage();
        this.updateDisplayName();
        this.updateCurrency();
        this.updateExperience();
        this.updateLevel();
        this.updateHealth();
        this.updateArmor();
        this.updateHunger();
        this.updateStatmina();
        this.updateAir();
        this.updateMountHealth();
    }

    private void updateDisplayName() {
        if (this.client.player != null) {
            final ITextComponent displayName = this.client.player.getDisplayName();
            if (displayName != null) {
                this.usernameLabel.setText(TextFormatting.WHITE + this.client.player.getDisplayName().getFormattedText());
            }
        }
    }

    private void updateUserImage() {
        if (this.client.player != null && this.client.player.hasPlayerInfo()) {
            this.userAvatarImage.setPlayerInfo(this.client.player.getPlayerInfo());
        }
    }

    private void updateCurrency() {
        if (hudData.isEconomyPresent) {
            this.currencyImage.setVisible(true);
            this.currencyLabel.setVisible(true);
            this.currencyLabel.setText(hudData.economyAmount);
            this.currencyLabel.setFontOptions(Fonts.colorAndScale(FontColors.GOLD, 0.7F));
            this.currencyLabel.setPosition(-3, SimpleScreen.getPaddedY(this.usernameLabel, 1),Anchor.TOP | Anchor.RIGHT);
            this.currencyImage.setPosition(-(this.currencyLabel.getWidth() + 5), SimpleScreen.getPaddedY(this.usernameLabel, -1), Anchor.TOP | Anchor.RIGHT);
        }
    }

    private void updateLevel() {
        this.levelLabel.setText("Lv. " + this.client.player.experienceLevel);
    }

    private void updateExperience() {
        final int experienceCap = this.client.player.xpBarCap();
        final int experience = (int) (this.client.player.experience * experienceCap);

        if (StaticAccess.config.get().client.displayNumericHUDValues) {
            this.experienceBar.setText(Text.of(this.df.format(experience) + "/" + this.df.format(experienceCap)));
            this.experienceBar.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F));
        } else {
            this.experienceBar.setText(Text.EMPTY);
        }
        this.experienceBar.setAmount(MathUtil.convertToRange(experience, 0, experienceCap, 0f, 1f));
    }

    private void updateHealth() {
        final float health = this.client.player.getHealth();
        final float maxHealth = this.client.player.getMaxHealth();
        this.healthBar.setAmount(MathUtil.convertToRange(health,0f, maxHealth, 0f, 1f));
        if (StaticAccess.config.get().client.displayNumericHUDValues) {
            this.healthBar.setText(Text.of(this.df.format(health) + "/" + this.df.format(maxHealth)));
            this.healthBar.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F));
        } else {
            this.healthBar.setText(Text.EMPTY);
        }
        this.height += 10;
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
        final int currentArmor = maxArmor - currentDamage;
        if (StaticAccess.config.get().client.displayNumericHUDValues) {
            this.armorBar.setText(Text.of(this.df.format((float) currentArmor) + "/" + this.df.format((float) maxArmor)));
            this.armorBar.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F));
        } else {
            this.armorBar.setText(Text.EMPTY);
        }
        this.armorBar.setAmount(MathUtil.convertToRange(currentArmor, 0, maxArmor, 0f, 1f));
        this.height += 10;
    }

    private void updateHunger() {
        final float foodLevel = this.client.player.getFoodStats().getFoodLevel();
        if (StaticAccess.config.get().client.displayNumericHUDValues) {
            this.hungerBar.setText(Text.of(this.df.format(foodLevel) + "/" + this.df.format(20f)));
            this.hungerBar.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F));
        } else {
            this.hungerBar.setText(Text.EMPTY);
        }
        this.hungerBar.setAmount(MathUtil.convertToRange(foodLevel, 0, 20, 0f, 1f));
        this.height += 10;
    }

    private void updateStatmina() {
        this.staminaBar.setVisible(this.client.player.getFoodStats().getSaturationLevel()>0);
        final float staminaLevel = this.client.player.getFoodStats().getSaturationLevel();
        if (this.staminaBar.isVisible()) {
            if (StaticAccess.config.get().client.displayNumericHUDValues) {
                this.staminaBar.setText(Text.of(this.df.format(staminaLevel) + "/" + this.df.format(20f)));
                this.staminaBar.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F));
            } else {
                this.staminaBar.setText(Text.EMPTY);
            }
            this.staminaBar.setAmount(MathUtil.convertToRange(staminaLevel, 0, 20, 0f, 1f));
            this.height += 10;
        }
    }

    private void updateAir() {
        this.airBar.setVisible(this.client.player.isInsideOfMaterial(Material.WATER));

        // TODO Hardcoded to not care above 300, if we can do this better in the future then we should do so
        if (this.airBar.isVisible()) {
            final int air = Math.max(this.client.player.getAir(), 0);
            if (StaticAccess.config.get().client.displayNumericHUDValues) {
                this.airBar.setText(Text.of(this.df.format((float) air) + "/" + this.df.format((float) 300)));
                this.airBar.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F));
            } else {
                this.airBar.setText(Text.EMPTY);
            }
            this.airBar.setAmount(MathUtil.convertToRange(air, 0, 300, 0f, 1f));
            this.height += 10;
        }
    }

    private void updateMountHealth() {
        final Entity entity = this.client.player.getRidingEntity();
        final boolean entityIsLiving = entity != null && entity instanceof EntityLivingBase;
        this.mountHealthBar.setVisible(entityIsLiving);
        if (entityIsLiving) {
            final EntityLivingBase ridingEntityLivingBase = (EntityLivingBase) entity;
            final float health = ridingEntityLivingBase.getHealth();
            final float maxHealth = ridingEntityLivingBase.getMaxHealth();
            if (StaticAccess.config.get().client.displayNumericHUDValues) {
                this.mountHealthBar.setText(Text.of(this.df.format(health) + "/" + this.df.format(maxHealth)));
            } else {
                this.mountHealthBar.setText(Text.EMPTY);
            }
            this.mountHealthBar.setAmount(MathUtil.convertToRange(health, 0, maxHealth, 0f, 1f));
            this.mountHealthBar.setPosition(0, SimpleScreen.getPaddedY(this.airBar.isVisible() ? this.airBar : this.hungerBar, 1));
            this.height += 10;
        }
    }
}
