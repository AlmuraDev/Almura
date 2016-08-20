/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui;

import com.almuradev.almura.Almura;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.renderer.icon.GuiIcon;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class GuiConstants {

    public static final String FORUM_URL = "http://www.almuramc.com";
    public static final String ISSUES_URL = "https://github.com/AlmuraDev/Almura/issues";
    public static final String MAP_URL = "http://srv1.almuramc.com:8123";
    public static final String SHOP_URL = ""; // TODO: Proper URL
    public static final String STATISTICS_URL = "http://srv1.almuramc.com:8080";
    public static final String TRADEMARK = "Minecraft is a registered trademark of Mojang AB";
    public static final String COPYRIGHT = "Copyright AlmuraDev 2012 - 2016";
    public static final ResourceLocation LOCATION_GUI_SPRITE_SHEET;
    public static final ResourceLocation ALMURA_LOGO_LOCATION;
    public static final ResourceLocation ALMURA_MAN_LOCATION;
    public static final ResourceLocation SPONGIE_LOCATION;
    public static final ResourceLocation SPONGEPOWERED_LOGO_LOCATION;
    public static final ResourceLocation AVATAR_GENERIC_LOCATION;
    public static final GuiTexture TEXTURE_SPRITESHEET;
    public static final GuiIcon ICON_EMPTY;
    public static final GuiIcon LEGACY_ICON_BAR;
    public static final GuiIcon LEGACY_ICON_HEART;
    public static final GuiIcon LEGACY_ICON_ARMOR;
    public static final GuiIcon LEGACY_ICON_HUNGER;
    public static final GuiIcon LEGACY_ICON_STAMINA;
    public static final GuiIcon LEGACY_ICON_XP;
    public static final GuiIcon LEGACY_ICON_PLAYER;
    public static final GuiIcon LEGACY_ICON_COMPASS;
    public static final GuiIcon LEGACY_ICON_MAP;
    public static final GuiIcon LEGACY_ICON_WORLD;
    public static final GuiIcon LEGACY_ICON_CLOCK;
    public static final GuiIcon ICON_CLOSE_NORMAL;
    public static final GuiIcon ICON_CLOSE_HOVER;
    public static final GuiIcon ICON_CLOSE_PRESSED;
    public static final GuiIcon ICON_FORUM;
    public static final GuiIcon ICON_FA_GITHUB;
    public static final GuiIcon ICON_FA_COG;
    public static final GuiIcon ICON_FA_SITEMAP;
    public static final GuiIcon ICON_FA_MAP;
    public static final GuiIcon ICON_FA_PIE_CHART;
    public static final GuiIcon ICON_FA_TROPHY;
    public static final GuiIcon ICON_FA_BOOK;
    public static final GuiIcon ICON_FA_SHOPPING_BAG;
    public static final UUID UNIQUE_ID_BLOOD;
    public static final UUID UNIQUE_ID_DOCKTER;
    public static final UUID UNIQUE_ID_GRINCH;
    public static final UUID UNIQUE_ID_MUMFREY;
    public static final UUID UNIQUE_ID_WIFEE;
    public static final UUID UNIQUE_ID_ZIDANE;
    public static final int BUTTON_WIDTH_LONG = 200;
    public static final int BUTTON_WIDTH_SHORT = 98;
    public static final int BUTTON_WIDTH_TINY = 64;
    public static final int BUTTON_WIDTH_ICON = 24;
    public static final int BUTTON_HEIGHT_ICON = 24;
    public static final int BUTTON_HEIGHT = 20;

    static {
        LOCATION_GUI_SPRITE_SHEET = new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/gui.png");
        ALMURA_LOGO_LOCATION = new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/almura_logo.png");
        ALMURA_MAN_LOCATION = new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/almura_man.png");
        SPONGIE_LOCATION = new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/spongie.png");
        SPONGEPOWERED_LOGO_LOCATION = new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/spongepowered_logo.png");
        AVATAR_GENERIC_LOCATION = new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/skins/avatars/generic.png");

        TEXTURE_SPRITESHEET = new GuiTexture(LOCATION_GUI_SPRITE_SHEET, 300, 144);

        ICON_EMPTY = TEXTURE_SPRITESHEET.getIcon(299, 141, 1, 1);

        LEGACY_ICON_BAR = TEXTURE_SPRITESHEET.getIcon(0, 126, 256, 14);
        LEGACY_ICON_HEART = TEXTURE_SPRITESHEET.getIcon(149, 62, 26, 26);
        LEGACY_ICON_ARMOR = TEXTURE_SPRITESHEET.getIcon(64, 63, 20, 27);
        LEGACY_ICON_HUNGER = TEXTURE_SPRITESHEET.getIcon(198, 96, 28, 29);
        LEGACY_ICON_STAMINA = TEXTURE_SPRITESHEET.getIcon(99, 93, 32, 31);
        LEGACY_ICON_XP = TEXTURE_SPRITESHEET.getIcon(169, 98, 24, 24);
        LEGACY_ICON_PLAYER = TEXTURE_SPRITESHEET.getIcon(67, 92, 28, 32);
        LEGACY_ICON_COMPASS = TEXTURE_SPRITESHEET.getIcon(118, 66, 30, 26);
        LEGACY_ICON_MAP = TEXTURE_SPRITESHEET.getIcon(0, 95, 32, 26);
        LEGACY_ICON_WORLD = TEXTURE_SPRITESHEET.getIcon(133, 93, 32, 32);
        LEGACY_ICON_CLOCK = TEXTURE_SPRITESHEET.getIcon(86, 64, 28, 26);

        ICON_CLOSE_NORMAL = TEXTURE_SPRITESHEET.getIcon(239, 69, 45, 19);
        ICON_CLOSE_HOVER = TEXTURE_SPRITESHEET.getIcon(239, 88, 45, 19);
        ICON_CLOSE_PRESSED = TEXTURE_SPRITESHEET.getIcon(239, 107, 45, 19);
        ICON_FORUM = TEXTURE_SPRITESHEET.getIcon(284, 0, 16, 16);

        ICON_FA_GITHUB = TEXTURE_SPRITESHEET.getIcon(284, 16, 16, 16);
        ICON_FA_COG = TEXTURE_SPRITESHEET.getIcon(284, 32, 16, 16);
        ICON_FA_SITEMAP = TEXTURE_SPRITESHEET.getIcon(284, 48, 16, 16);
        ICON_FA_MAP = TEXTURE_SPRITESHEET.getIcon(284, 64, 16, 16);
        ICON_FA_PIE_CHART = TEXTURE_SPRITESHEET.getIcon(284, 80, 16, 16);
        ICON_FA_TROPHY = TEXTURE_SPRITESHEET.getIcon(284, 96, 16, 16);
        ICON_FA_BOOK = TEXTURE_SPRITESHEET.getIcon(284, 112, 16, 16);
        ICON_FA_SHOPPING_BAG = TEXTURE_SPRITESHEET.getIcon(284, 128, 16, 16);

        UNIQUE_ID_BLOOD = UUID.fromString("87caf570-b1fc-4100-bd95-3e7f1fa2e153");
        UNIQUE_ID_DOCKTER = UUID.fromString("bcbce24c-20fc-4914-8f49-5aaed0cd3696");
        UNIQUE_ID_GRINCH = UUID.fromString("7c104888-df99-4224-a8ba-2c4e15dbc777");
        UNIQUE_ID_MUMFREY = UUID.fromString("e8e0361e-9b3b-481a-b06a-5c314a6c1ef0");
        UNIQUE_ID_WIFEE = UUID.fromString("5f757396-8bc7-4dff-8b1f-37fd454a86b7");
        UNIQUE_ID_ZIDANE = UUID.fromString("85271de5-8380-4db5-9f05-ada3b4aa785c");
    }

    private GuiConstants() {
    }
}
