/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client;

import com.almuradev.almura.Almura;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.render.GuiIcon;
import net.malisis.ego.gui.render.GuiTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface GuiConfig
{
	public static int PADDING = 4;

	@SideOnly(Side.CLIENT)
	interface Icon
	{
		GuiIcon ALMURA_LOGO = new GuiIcon(SpriteSheet.ALMURA_LOGO);
		GuiIcon VANILLA_CONTAINER_INVENTORY_ADVANCEMENT = null;//SpriteSheet.VANILLA_CONTAINER_INVENTORY.getXYResizableIcon(141, 166, 24,
		// 24, 5);
		GuiIcon EMPTY = new GuiIcon(SpriteSheet.ALMURA, 299, 141, 1, 1);
		GuiIcon VANILLA_HEART_BACKGROUND = new GuiIcon(SpriteSheet.VANILLA_ICON, 16, 0, 9, 9);
		GuiIcon VANILLA_HEART_FOREGROUND = new GuiIcon(SpriteSheet.VANILLA_ICON, 52, 0, 9, 9);
		GuiIcon VANILLA_HUNGER_BACKGROUND = new GuiIcon(SpriteSheet.VANILLA_ICON, 16, 27, 9, 9);
		GuiIcon VANILLA_HUNGER_FOREGROUND = new GuiIcon(SpriteSheet.VANILLA_ICON, 52, 27, 9, 9);
		GuiIcon VANILLA_ARMOR = new GuiIcon(SpriteSheet.VANILLA_ICON, 34, 9, 9, 9);
		GuiIcon VANILLA_AIR = new GuiIcon(SpriteSheet.VANILLA_ICON, 16, 18, 9, 9);
		GuiIcon VANILLA_MOUNT = new GuiIcon(SpriteSheet.VANILLA_ICON, 88, 9, 9, 9);
		GuiIcon ENJIN = new GuiIcon(SpriteSheet.ALMURA, 0, 90, 16, 16);
		GuiIcon FA_GITHUB = new GuiIcon(SpriteSheet.ALMURA, 16, 90, 14, 14);
		GuiIcon FA_COG = new GuiIcon(SpriteSheet.ALMURA, 32, 90, 15, 15);
		GuiIcon FA_SITEMAP = new GuiIcon(SpriteSheet.ALMURA, 48, 90, 16, 14);
		GuiIcon FA_MAP = new GuiIcon(SpriteSheet.ALMURA, 64, 90, 16, 15);
		GuiIcon FA_PIE_CHART = new GuiIcon(SpriteSheet.ALMURA, 80, 90, 15, 15);
		GuiIcon FA_TROPHY = new GuiIcon(SpriteSheet.ALMURA, 96, 90, 16, 14);
		GuiIcon FA_BOOK = new GuiIcon(SpriteSheet.ALMURA, 112, 90, 16, 14);
		GuiIcon FA_SHOPPING_BAG = new GuiIcon(SpriteSheet.ALMURA, 128, 90, 14, 14);
		GuiIcon STAMINA = new GuiIcon(SpriteSheet.ALMURA, 132, 65, 9, 9);

		/*
		 * Note: you must call .setSpritesheet(GuiConfig.SpriteSheet."SPRITENAME" if you are using custom icon's in-game.
		 */
	}

	@SideOnly(Side.CLIENT)
	interface Location
	{
		ResourceLocation ALMURA_MAN = new ResourceLocation(Almura.ID, "textures/gui/almura_man.png");
		ResourceLocation SPONGEPOWERED_LOGO = new ResourceLocation(Almura.ID, "textures/gui/spongepowered_logo.png");
		ResourceLocation SPONGEPOWERED_SPONGIE = new ResourceLocation(Almura.ID, "textures/gui/spongie.png");
		ResourceLocation GENERIC_AVATAR = new ResourceLocation(Almura.ID, "textures/gui/skins/avatars/generic.png");
		ResourceLocation DEAD_STEVE = new ResourceLocation(Almura.ID, "textures/gui/dead_steve.png");
	}

	@SideOnly(Side.CLIENT)
	interface SpriteSheet
	{
		GuiTexture ALMURA_LOGO = new GuiTexture(new ResourceLocation(Almura.ID, "textures/gui/almura_logo.png"), 300, 495);
		ResourceLocation ALMURA_LOCATION = new ResourceLocation(Almura.ID, "textures/gui/gui.png");
		GuiTexture ALMURA = new GuiTexture(ALMURA_LOCATION, 284, 106);
		GuiTexture VANILLA_ICON = new GuiTexture(MalisisGui.ICONS, 256, 256);
		ResourceLocation VANILLA_CONTAINER_INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/inventory.png");
		GuiTexture VANILLA_CONTAINER_INVENTORY = new GuiTexture(VANILLA_CONTAINER_INVENTORY_LOCATION, 256, 256);
	}

	@SideOnly(Side.CLIENT)
	interface Url
	{
		String FORUM = "https://almura.enjin.com/forum";
		String ISSUES = "https://github.com/AlmuraDev/Almura/issues";
		String MAP = "http://srv1.almuramc.com:8123";
		String SHOP = "http://www.almuramc.com"; // TODO: Proper URL
		String SKINS = "https://mc-heads.net/avatar/%s/%d.png";
		String STATISTICS = "http://srv1.almuramc.com:8080";
	}
}
