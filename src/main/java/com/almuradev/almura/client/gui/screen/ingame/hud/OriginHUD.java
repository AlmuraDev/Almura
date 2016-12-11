/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame.hud;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.GuiConstants;
import com.almuradev.almura.client.gui.GuiRemoteTexture;
import com.almuradev.almura.client.gui.component.UIExpandingLabel;
import com.almuradev.almura.client.gui.component.UIHUDPanel;
import com.almuradev.almura.client.gui.component.UIPropertyBar;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import com.almuradev.almura.util.MathUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.icon.Icon;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;

import java.text.NumberFormat;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class OriginHUD extends AbstractHUD {

    private static final int PROPERTY_BAR_WIDTH = 114;
    private static final int PROPERTY_BAR_HEIGHT = 9;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.getDefault());

    private UILabel compassLabel, coordsLabel, currencyLabel, levelLabel, playerCountLabel, playerLabel, worldLabel;
    private UIImage timeImage, currencyImage, playerCountImage, playerImage;
    private UIPropertyBar healthBar, armorBar, hungerBar, experienceBar, airBar, mountHealthBar;
    private UIHUDPanel leftContainer, middleContainer, rightContainer, statsContainer;

    @Override
    public void construct() {
        guiscreenBackground = false;

        this.renderer.setDefaultTexture(GuiConstants.VANILLA_ACHIEVEMENT_BACKGROUND_SPRITESHEET);

        this.leftContainer = new UIHUDPanel(this, 124, 32);
        this.leftContainer.setPosition(0, 0, Anchor.TOP | Anchor.LEFT);

        this.playerImage = new UIImage(this, new GuiRemoteTexture(
                GuiConstants.AVATAR_GENERIC_LOCATION,
                new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/skins/avatars/" + this.mc.player.getUniqueID() + ".png"),
                String.format(GuiConstants.SKIN_URL_BASE, this.mc.player.getUniqueID().toString(), 16),
                16, 16), null);
        this.playerImage.setPosition(2, 2);
        this.playerImage.setSize(16, 16);

        this.playerLabel = new UILabel(this, TextFormatting.WHITE + this.mc.player.getName());
        this.playerLabel.setPosition(SimpleScreen.getPaddedX(this.playerImage, 3), 2);

        this.levelLabel = new UILabel(this, "");
        this.levelLabel.setPosition(SimpleScreen.getPaddedX(this.playerImage, 3), SimpleScreen.getPaddedY(this.playerLabel, 1),
                Anchor.TOP | Anchor.LEFT);
        this.levelLabel.setFontOptions(FontOptions.builder().from(FontOptionsConstants.FRO_COLOR_WHITE).scale(0.65f).build());

        this.currencyImage = new UIImage(this, MalisisGui.BLOCK_TEXTURE, Icon.from(Items.EMERALD));
        this.currencyImage.setSize(8, 8);

        this.currencyLabel = new UILabel(this, "");
        this.currencyLabel.setFontOptions(FontOptions.builder().from(FontOptionsConstants.FRO_COLOR_WHITE).scale(0.65f).build());

        this.experienceBar = new UIPropertyBar(this, PROPERTY_BAR_WIDTH,  PROPERTY_BAR_HEIGHT);
        this.experienceBar.setPosition(0, SimpleScreen.getPaddedY(this.playerImage, 2), Anchor.CENTER | Anchor.TOP);
        this.experienceBar.setColor(Color.ofRgb(0, 211, 0).getRgb());
        this.experienceBar.setSize(PROPERTY_BAR_WIDTH, 3);

        this.leftContainer.add(this.playerImage, this.playerLabel, this.levelLabel, this.currencyImage, this.currencyLabel, this.experienceBar);

        // Stats container
        this.statsContainer = new UIHUDPanel(this, 85, 49);
        this.statsContainer.setPosition(0, SimpleScreen.getPaddedY(this.leftContainer, 2), Anchor.TOP | Anchor.LEFT);

        this.healthBar = new UIPropertyBar(this, PROPERTY_BAR_WIDTH, PROPERTY_BAR_HEIGHT);
        this.healthBar.setPosition(0, 2, Anchor.CENTER | Anchor.TOP);
        this.healthBar.setColor(Color.ofRgb(187, 19, 19).getRgb());
        this.healthBar.setIcons(GuiConstants.VANILLA_ICON_HEART_BACKGROUND, GuiConstants.VANILLA_ICON_HEART_FOREGROUND);

        this.armorBar = new UIPropertyBar(this, PROPERTY_BAR_WIDTH, PROPERTY_BAR_HEIGHT);
        this.armorBar.setPosition(0, SimpleScreen.getPaddedY(healthBar, 1), Anchor.CENTER | Anchor.TOP);
        this.armorBar.setColor(Color.ofRgb(184, 185, 196).getRgb());
        this.armorBar.setBackgroundIcon(GuiConstants.VANILLA_ICON_ARMOR);

        this.hungerBar = new UIPropertyBar(this, PROPERTY_BAR_WIDTH, PROPERTY_BAR_HEIGHT);
        this.hungerBar.setPosition(0, SimpleScreen.getPaddedY(armorBar, 1), Anchor.CENTER | Anchor.TOP);
        this.hungerBar.setColor(Color.ofRgb(157, 109, 67).getRgb());
        this.hungerBar.setIcons(GuiConstants.VANILLA_ICON_HUNGER_BACKGROUND, GuiConstants.VANILLA_ICON_HUNGER_FOREGROUND);

        this.airBar = new UIPropertyBar(this, PROPERTY_BAR_WIDTH, PROPERTY_BAR_HEIGHT);
        this.airBar.setPosition(0, SimpleScreen.getPaddedY(experienceBar, 1), Anchor.CENTER | Anchor.TOP);
        this.airBar.setColor(Color.ofRgb(0, 148, 255).getRgb());
        this.airBar.setBackgroundIcon(GuiConstants.VANILLA_ICON_AIR);

        this.mountHealthBar = new UIPropertyBar(this, PROPERTY_BAR_WIDTH,  PROPERTY_BAR_HEIGHT);
        this.mountHealthBar.setPosition(0, SimpleScreen.getPaddedY(airBar, 1), Anchor.CENTER | Anchor.TOP);
        this.mountHealthBar.setColor(Color.ofRgb(239, 126, 74).getRgb());
        this.mountHealthBar.setBackgroundIcon(GuiConstants.VANILLA_ICON_MOUNT);

        this.statsContainer.add(healthBar, armorBar, hungerBar, airBar, mountHealthBar);

        // Middle container
        this.middleContainer = new UIHUDPanel(this, 124, 27);
        this.middleContainer.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        this.worldLabel = new UILabel(this, "");
        this.worldLabel.setPosition(0, 2, Anchor.TOP | Anchor.CENTER);

        this.compassLabel = new UILabel(this, "");
        this.compassLabel.setFontOptions(FontOptions.builder().from(FontOptionsConstants.FRO_COLOR_WHITE).scale(0.65f).build());
        this.compassLabel.setPosition(0, -1, Anchor.BOTTOM | Anchor.CENTER);

        this.middleContainer.add(this.worldLabel, this.compassLabel);

        // Right container
        this.rightContainer = new UIHUDPanel(this, 124, 32);
        this.rightContainer.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);

        this.timeImage = new UIImage(this, new ItemStack(Items.CLOCK));
        this.timeImage.setPosition(-2, 0, Anchor.RIGHT | Anchor.MIDDLE);

        this.playerCountLabel = new UILabel(this, "");
        this.playerCountLabel.setFontOptions(FontOptions.builder().from(FontOptionsConstants.FRO_COLOR_WHITE).scale(0.65f).build());

        this.playerCountImage = new UIImage(this, new GuiTexture(GuiConstants.AVATAR_GENERIC_LOCATION), null);
        this.playerCountImage.setSize(16, 16);

        this.coordsLabel = new UIExpandingLabel(this, "", true);
        this.coordsLabel.setPosition(2, 1, Anchor.TOP | Anchor.LEFT);
        this.coordsLabel.setFontOptions(FontOptions.builder().from(FontOptionsConstants.FRO_COLOR_WHITE).scale(0.85f).build());

        this.rightContainer.add(this.timeImage, this.playerCountImage, this.playerCountLabel, this.coordsLabel);

        addToScreen(this.statsContainer);
        addToScreen(this.leftContainer);
        addToScreen(this.middleContainer);
        addToScreen(this.rightContainer);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().player.world == null) {
            return;
        }

        this.updateHealth();
        this.updateArmor();
        this.updateHunger();
        this.updateExperience();
        this.updateAir();
        this.updateMountHealth();
        this.updatePlayerStats();
        this.updateLevel();
        this.updateCompass();
        this.updateCoordinates();
        this.updateCurrency();
        this.updatePlayerCount();
        this.updateWorldInformation();
    }

    private void updateHealth() {
        this.healthBar.setAmount(MathUtil.ConvertToRange(Minecraft.getMinecraft().player.getHealth(),
                0f, Minecraft.getMinecraft().player.getMaxHealth(), 0f, 1f));
    }

    private void updateArmor() {
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
        this.armorBar.setAmount(MathUtil.ConvertToRange(maxArmor - currentArmor, 0, maxArmor, 0f, 1f));
    }

    private void updateHunger() {
        this.hungerBar.setAmount(MathUtil.ConvertToRange(Minecraft.getMinecraft().player.getFoodStats().getFoodLevel(),0, 20,0f, 1f));
    }

    private void updateExperience() {
        final int experienceCap = Minecraft.getMinecraft().player.xpBarCap();
        final int experience = (int) (Minecraft.getMinecraft().player.experience * experienceCap);

        this.experienceBar.setText(Text.of(experience + "/" + experienceCap));
        this.experienceBar.setAmount(MathUtil.ConvertToRange(experience, 0, experienceCap, 0f, 1f));
    }

    private void updateAir() {
        this.airBar.setVisible(Minecraft.getMinecraft().player.isInsideOfMaterial(Material.WATER));

        // TODO Hardcoded to not care above 300, if we can do this better in the future then we should do so
        if (this.airBar.isVisible()) {
            this.airBar.setAmount(MathUtil.ConvertToRange(Minecraft.getMinecraft().player.getAir(),0, 300,0f, 1f));
        }
    }

    private void updateMountHealth() {
        EntityLivingBase ridingEntityLivingBase = (EntityLivingBase) Minecraft.getMinecraft().player.getRidingEntity();
        this.mountHealthBar.setVisible(ridingEntityLivingBase != null);
        if (this.mountHealthBar.isVisible() && ridingEntityLivingBase != null) {
            this.mountHealthBar.setAmount(MathUtil.ConvertToRange(ridingEntityLivingBase.getHealth(), 0, ridingEntityLivingBase.getMaxHealth(), 0f, 1f));
            this.mountHealthBar.setPosition(0, SimpleScreen.getPaddedY(this.airBar.isVisible() ? this.airBar : this.experienceBar, 1));
        }
    }

    private void updatePlayerStats() {
        int height = 0;
        height += this.healthBar.isVisible() ? this.healthBar.getHeight() : 0;
        height += this.armorBar.isVisible() ? this.armorBar.getHeight() : 0;
        height += this.hungerBar.isVisible() ? this.hungerBar.getHeight() : 0;
        height += this.airBar.isVisible() ? this.airBar.getHeight() : 0;
        height += this.mountHealthBar.isVisible() ? this.mountHealthBar.getHeight() : 0;
        this.statsContainer.setVisible(height != 0);

        this.statsContainer.setSize(PROPERTY_BAR_WIDTH + 10, height + 12);
    }

    private void updateLevel() {
        this.levelLabel.setText("Lv. " + Minecraft.getMinecraft().player.experienceLevel);
    }

    private void updateCompass() {
        this.compassLabel.setText(HUDData.getCompass());
    }

    private void updateCoordinates() {
        this.coordsLabel.setText(
                TextFormatting.GOLD + "X: " + TextFormatting.RESET + NUMBER_FORMAT.format(Minecraft.getMinecraft().player.getPosition().getX()) +
                "\n" +
                TextFormatting.GOLD + "Y: " + TextFormatting.RESET + NUMBER_FORMAT.format(Minecraft.getMinecraft().player.getPosition().getY()) +
                "\n" +
                TextFormatting.GOLD + "Z: " + TextFormatting.RESET + NUMBER_FORMAT.format(Minecraft.getMinecraft().player.getPosition().getZ()));
    }

    private void updateCurrency() {
        this.currencyImage.setVisible(!HUDData.IS_ECONOMY_PRESENT);
        this.currencyLabel.setVisible(!HUDData.IS_ECONOMY_PRESENT);
        if (currencyImage.isVisible() && currencyLabel.isVisible()) {
            this.currencyImage.setPosition(SimpleScreen.getPaddedX(this.levelLabel, 2), SimpleScreen.getPaddedY(this.playerLabel, 0),
                    Anchor.TOP | Anchor.LEFT);
            this.currencyLabel.setText(TextFormatting.WHITE + HUDData.PLAYER_CURRENCY);
            this.currencyLabel.setPosition(SimpleScreen.getPaddedX(this.currencyImage, 2), SimpleScreen.getPaddedY(this.playerLabel, 1),
                    Anchor.TOP | Anchor.LEFT);
        }
    }

    private void updatePlayerCount() {
        final boolean isOnline = !Minecraft.getMinecraft().isSingleplayer() || (Minecraft.getMinecraft().isSingleplayer() && Minecraft.getMinecraft()
                .getIntegratedServer().getPublic());
        if (isOnline) {
            this.playerCountLabel.setText(TextFormatting.WHITE.toString() + 1 + "/" + 50);
            this.playerCountLabel.setSize((int) this.playerCountLabel.getFont().getStringWidth(this.playerCountLabel.getText(), this.playerCountLabel
                    .getFontOptions()), (int) this.playerCountLabel.getFont().getStringHeight(this.playerCountLabel.getFontOptions()));
            this.playerCountImage.setPosition(SimpleScreen.getPaddedX(this.playerCountLabel, 2, Anchor.RIGHT), 0, Anchor.RIGHT | Anchor.MIDDLE);
        }
        this.playerCountImage.setVisible(isOnline);
        this.playerCountLabel.setVisible(isOnline);
    }

    private void updateWorldInformation() {
        this.worldLabel.setText(TextFormatting.WHITE + HUDData.WORLD_NAME);
        this.worldLabel.setPosition(0, 2, Anchor.TOP | Anchor.CENTER);
    }

    @Override
    public int getOriginBossBarOffsetY() {
        return middleContainer.getHeight() + 12;
    }

    @Override
    public int getTabMenuOffsetY() {
        return middleContainer.getHeight() + 2;
    }

    @Override
    public int getPotionOffsetY() {
        return rightContainer.getHeight() + 2;
    }
}
