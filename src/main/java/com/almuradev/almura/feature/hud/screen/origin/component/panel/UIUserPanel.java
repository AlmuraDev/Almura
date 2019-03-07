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
import com.almuradev.almura.shared.client.ui.Fonts;
import com.almuradev.almura.shared.util.MathUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.util.FontColors;
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

import java.text.DecimalFormat;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class UIUserPanel extends AbstractPanel
{

	@Inject
	private static HeadUpDisplay hudData;

	private final int baseHeight;
	private final UIImage currencyImage;
	private final UIAvatarImage userAvatarImage;
	private final UIXPOrbImage xpOrbImage;
	private final UILabel currencyLabel, levelLabel, usernameLabel;
	private final UIPropertyBar airBar, armorBar, experienceBar, healthBar, hungerBar, mountHealthBar, staminaBar;
	private final DecimalFormat df = new DecimalFormat("0.##");
	@Nullable
	private UIComponent lastVisibleComponent;

	private int update = 1;

	public UIUserPanel(MalisisGui gui, int width, int height)
	{
		super(gui, width, height);

		baseHeight = height;

		// Avatar
		userAvatarImage = new UIAvatarImage(gui, client.player.getPlayerInfo());
		userAvatarImage.setPosition(2, 2);
		userAvatarImage.setSize(16, 16);

		// Username
		usernameLabel = new UILabel(gui, TextFormatting.WHITE + client.player.getDisplayName().getFormattedText());
		usernameLabel.setPosition(BasicScreen.getPaddedX(userAvatarImage, 3), 2);

		// Level
		levelLabel = new UILabel(gui, "");
		levelLabel.setPosition(BasicScreen.getPaddedX(userAvatarImage, 3), BasicScreen.getPaddedY(usernameLabel, 0));
		levelLabel.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.7F));

		// Currency
		currencyImage = new UIImage(gui, MalisisGui.BLOCK_TEXTURE, Icon.from(Items.EMERALD));
		currencyImage.setSize(8, 8);
		currencyLabel = new UILabel(gui, "");
		currencyLabel.setVisible(false);
		currencyImage.setVisible(false);

		// Bars
		int barWidth = this.width - 10;
		int barHeight = 9;

		// XP Orb Image
		xpOrbImage = new UIXPOrbImage(gui);
		xpOrbImage.setSize(9, 9);
		xpOrbImage.setPosition(2, BasicScreen.getPaddedY(levelLabel, 3));

		// TODO: Toggle for font scaling? Looks horrid on GUI Size: Normal

		// Experience
		experienceBar = new UIPropertyBar(gui, barWidth - 11, 7).setPosition(6, xpOrbImage.getY(), Anchor.TOP | Anchor.CENTER)
																.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
																.setColor(org.spongepowered.api.util.Color.ofRgb(0, 150, 0).getRgb());

		// Health
		// TODO Show effects (or icon) for golden apple, wither, poison, etc
		healthBar = new UIPropertyBar(gui, barWidth, barHeight).setColor(org.spongepowered.api.util.Color.ofRgb(187, 19, 19).getRgb())
															   .setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
		//															   .setIcons(GuiConfig.Icon.VANILLA_HEART_BACKGROUND,
		//																		 GuiConfig.Icon.VANILLA_HEART_FOREGROUND)
		;
		healthBar.setTooltip("Health Value");

		// Armor
		armorBar = new UIPropertyBar(gui, barWidth, barHeight).setColor(org.spongepowered.api.util.Color.ofRgb(102, 103, 109).getRgb())
															  .setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
		//													  .setBackgroundIcon(GuiConfig.Icon.VANILLA_ARMOR)
		;
		armorBar.setTooltip("Armor Value");

		// Hunger
		hungerBar = new UIPropertyBar(gui, barWidth, barHeight).setColor(org.spongepowered.api.util.Color.ofRgb(137, 89, 47).getRgb())
															   .setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F));
		//															   .setIcons(GuiConfig.Icon.VANILLA_HUNGER_BACKGROUND,
		//																			  GuiConfig.Icon.VANILLA_HUNGER_FOREGROUND);

		// Stamina
		staminaBar = new UIPropertyBar(gui, barWidth, barHeight).setColor(org.spongepowered.api.util.Color.ofRgb(0, 148, 255).getRgb())
																.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
		//.setBackgroundIcon(GuiConfig.Icon.STAMINA)
		//.setSpritesheet(GuiConfig.SpriteSheet.ALMURA)
		; // You must call this
		// if you are loading icon within the TexturePack when in-game.

		// Air
		airBar = new UIPropertyBar(gui, barWidth, barHeight).setColor(org.spongepowered.api.util.Color.ofRgb(0, 148, 255).getRgb())
															.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
		//															.setBackgroundIcon(GuiConfig.Icon.VANILLA_AIR)
		;
		// Mount Health
		mountHealthBar =
				new UIPropertyBar(gui, barWidth, barHeight).setColor(org.spongepowered.api.util.Color.ofRgb(239, 126, 74).getRgb())
																	.setFontOptions(Fonts.colorAndScale(FontColors.WHITE, 0.8F))
		//																	.setBackgroundIcon(GuiConfig.Icon.VANILLA_MOUNT)
		;

		add(userAvatarImage,
			usernameLabel,
			xpOrbImage,
			levelLabel,
			experienceBar,
			currencyImage,
			currencyLabel,
			healthBar,
			armorBar,
			hungerBar,
			staminaBar,
			airBar,
			mountHealthBar);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (client.player == null || client.player.world == null)
		{
			return;
		}
		super.drawForeground(renderer, mouseX, mouseY, partialTick);

		if (--update == 0)
		{
			// Reset variables
			height = baseHeight;
			lastVisibleComponent = null;

			// Update
			updateUserImage();
			updateDisplayName();
			updateCurrency();
			updateLevel();

			// Update property bars, call order defines visual layout
			updateExperience();
			updateHealth();
			updateArmor();
			updateHunger();

			// Update temporary property bars, call order defines visual layout
			updateStamina();
			updateAir();
			updateMountHealth();
			update = 30;
		}
	}

	private void updateDisplayName()
	{
		if (client.player != null)
		{
			usernameLabel.setText(TextFormatting.WHITE + client.player.getDisplayName().getFormattedText());
		}
	}

	private void updateUserImage()
	{
		if (client.player != null && client.player.hasPlayerInfo())
		{
			userAvatarImage.setPlayerInfo(client.player.getPlayerInfo());
		}
	}

	private void updateCurrency()
	{
		if (hudData.isEconomyPresent)
		{
			//this.currencyImage.setVisible(true);
			currencyLabel.setVisible(true);
			currencyLabel.setText("$ " + hudData.economyAmount);
			currencyLabel.setFontOptions(Fonts.colorAndScale(FontColors.GOLD, 0.7F));
			currencyLabel.setPosition(-4, BasicScreen.getPaddedY(usernameLabel, 1), Anchor.TOP | Anchor.RIGHT);
			currencyImage.setPosition(-(currencyLabel.getWidth() + 10),
									  BasicScreen.getPaddedY(usernameLabel, -1),
									  Anchor.TOP | Anchor.RIGHT);
			currencyImage.setVisible(false);
		}
	}

	private void updateLevel()
	{
		levelLabel.setText("Lv. " + client.player.experienceLevel);
	}

	private void updateExperience()
	{
		int experienceCap = client.player.xpBarCap();
		int experience = (int) (client.player.experience * experienceCap);

		updateBarProperties(experienceBar, experience, experienceCap, false, false);

		xpOrbImage.setPosition(2,
							   lastVisibleComponent != null ?
							   BasicScreen.getPaddedY(lastVisibleComponent, 2) :
							   BasicScreen.getPaddedY(levelLabel, 3));
		experienceBar.setPosition(6, xpOrbImage.getY(), Anchor.TOP | Anchor.CENTER);
		lastVisibleComponent = xpOrbImage;
	}

	private void updateHealth()
	{
		updateBarProperties(healthBar, client.player.getHealth(), client.player.getMaxHealth());
	}

	private void updateArmor()
	{
		int maxArmor = 0;
		int currentDamage = 0;
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
		{
			if (slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND)
			{
				continue;
			}
			ItemStack stack = client.player.getItemStackFromSlot(slot);
			if (stack.getItem() instanceof ItemArmor)
			{
				maxArmor += ((ItemArmor) stack.getItem()).getArmorMaterial().getDurability(slot);
				currentDamage += stack.getItem().getDamage(stack);
			}
		}
		updateBarProperties(armorBar, maxArmor - currentDamage, maxArmor);
	}

	private void updateHunger()
	{
		updateBarProperties(hungerBar, client.player.getFoodStats().getFoodLevel(), 20f);
	}

	private void updateStamina()
	{
		float staminaLevel = client.player.getFoodStats().getSaturationLevel();

		staminaBar.setVisible(staminaLevel > 0);
		if (staminaBar.isVisible())
		{
			updateBarProperties(staminaBar, staminaLevel, 20f);
		}
	}

	private void updateAir()
	{
		airBar.setVisible(client.player.isInsideOfMaterial(Material.WATER));

		// TODO Hardcoded to not care above 300, if we can do this better in the future then we should do so
		if (airBar.isVisible())
		{
			int air = Math.max(client.player.getAir(), 0);
			updateBarProperties(airBar, air, 300);
		}
	}

	private void updateMountHealth()
	{
		Entity entity = client.player.getRidingEntity();
		boolean entityIsLiving = entity != null && entity instanceof EntityLivingBase;
		mountHealthBar.setVisible(entityIsLiving);
		if (entityIsLiving)
		{
			EntityLivingBase ridingEntityLivingBase = (EntityLivingBase) entity;

			updateBarProperties(mountHealthBar, ridingEntityLivingBase.getHealth(), ridingEntityLivingBase.getMaxHealth());
		}
	}

	private void updateBarProperties(UIPropertyBar propertyBar, float value, float maxValue)
	{
		updateBarProperties(propertyBar, value, maxValue, true, true);
	}

	private void updateBarProperties(UIPropertyBar propertyBar, float value, float maxValue, boolean updatePosition,
			boolean updateLastVisible)
	{
		// Update text
		String text = ClientStaticAccess.configAdapter.get().general.displayNumericHUDValues ?
					  df.format(value) + "/" + df.format(maxValue) :
					  "";
		propertyBar.setText(text);

		// Update amount value
		propertyBar.setAmount(MathUtil.scalef(value, 0, maxValue, 0f, 1f));

		// Pad against last visible component
		if (updatePosition)
		{
			propertyBar.setPosition(0,
									lastVisibleComponent != null ?
									BasicScreen.getPaddedY(lastVisibleComponent, 1) :
									BasicScreen.getPaddedY(levelLabel, 3),
									Anchor.TOP | Anchor.CENTER);
		}

		// Update last visible component
		if (updateLastVisible)
		{
			lastVisibleComponent = propertyBar;
		}

		// Update height
		height += 10;
	}
}
