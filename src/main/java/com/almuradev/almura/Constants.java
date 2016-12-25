/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.renderer.icon.GuiIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class Constants {

    public static final class Plugin {
        public static final String ID = "almura";
        public static final String PROXY_CLIENT_CLASSPATH = "com.almuradev.almura.client.ClientProxy";
        public static final String PROXY_SERVER_CLASSPATH = "com.almuradev.almura.server.ServerProxy";
    }

    public static final class FileSystem {
        public static final Path PATH_CONFIG = Paths.get(".").resolve("config").resolve(Plugin.ID);
        public static final Path PATH_CONFIG_CLIENT = PATH_CONFIG.resolve("client.conf");
        public static final Path PATH_CONFIG_PACKS = PATH_CONFIG.resolve("packs");

        public static void construct() {
            if (Files.notExists(PATH_CONFIG_PACKS)) {
                try {
                    Files.createDirectories(PATH_CONFIG_PACKS);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create [" + PATH_CONFIG_PACKS + "]!", e);
                }
            }
        }
    }

    public static final class Model {
        public static final String COMMENT = "#";

        public static final class Obj {
            public static final String MATERIAL_LIBRARY = "mtllib";
            public static final String USE_MATERIAL = "usemtl";
            public static final String VERTEX = "v";
            public static final String VERTEX_NORMAL = "vn";
            public static final String VERTEX_TEXTURE_COORDINATE = "vt";
            public static final String GROUP = "g";
            public static final String FACE = "f";
        }

        public static final class Material {
            public static final String NEW_MATERIAL = "newmtl";
            public static final String DIFFUSE = "map_Kd";
        }

    }
    public static final class Config {
        public static final String HEADER = "2.0\nAlmura configuration\n\nFor further assistance, join #almura on EsperNet.";
    }

    @SideOnly(Side.CLIENT)
    public static final class Gui {
        public static final String FORUM_URL = "http://www.almuramc.com";
        public static final String ISSUES_URL = "https://github.com/AlmuraDev/Almura/issues";
        public static final String MAP_URL = "http://srv1.almuramc.com:8123";
        public static final String OPENGL_WARNING_URL = "https://help.mojang.com/customer/portal/articles/325948?ref=game";
        public static final String SHOP_URL = ""; // TODO: Proper URL
        public static final String SKIN_URL_BASE = "https://mc-heads.net/avatar/%s/%d.png";
        public static final String STATISTICS_URL = "http://srv1.almuramc.com:8080";
        public static final String TRADEMARK = "Minecraft is a registered trademark of Mojang AB";
        public static final String COPYRIGHT = "Copyright AlmuraDev 2012 - 2016";
        public static final ResourceLocation LOCATION_GUI_SPRITE_SHEET;
        public static final ResourceLocation LOCATION_VANILLA_ACHIEVEMENT_BACKGROUND_SPRITESHEET;
        public static final ResourceLocation ALMURA_LOGO_LOCATION;
        public static final ResourceLocation ALMURA_MAN_LOCATION;
        public static final ResourceLocation SPONGIE_LOCATION;
        public static final ResourceLocation SPONGEPOWERED_LOGO_LOCATION;
        public static final ResourceLocation AVATAR_GENERIC_LOCATION;
        public static final GuiTexture TEXTURE_SPRITESHEET;
        public static final GuiTexture VANILLA_ICON_SPRITESHEET;
        public static final GuiTexture VANILLA_ACHIEVEMENT_BACKGROUND_SPRITESHEET;
        public static final GuiIcon ICON_EMPTY;
        public static final GuiIcon VANILLA_ICON_HEART_BACKGROUND;
        public static final GuiIcon VANILLA_ICON_HEART_FOREGROUND;
        public static final GuiIcon VANILLA_ICON_HUNGER_BACKGROUND;
        public static final GuiIcon VANILLA_ICON_HUNGER_FOREGROUND;
        public static final GuiIcon VANILLA_ICON_ARMOR;
        public static final GuiIcon VANILLA_ICON_AIR;
        public static final GuiIcon VANILLA_ICON_MOUNT;
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
        public static final GuiIcon ICON_ORB_BLUE;
        public static final GuiIcon ICON_ORB_BROWN;
        public static final GuiIcon ICON_ORB_GRAY;
        public static final GuiIcon ICON_ORB_GREEN;
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
        public static final UUID UNIQUE_ID_WOLFEYE;
        public static final UUID UNIQUE_ID_ZIDANE;
        public static final int BUTTON_WIDTH_LONG = 200;
        public static final int BUTTON_WIDTH_SHORT = 98;
        public static final int BUTTON_WIDTH_TINY = 64;
        public static final int BUTTON_WIDTH_ICON = 24;
        public static final int BUTTON_HEIGHT_ICON = 24;
        public static final int BUTTON_HEIGHT = 20;

        static {
            LOCATION_GUI_SPRITE_SHEET = new ResourceLocation(Constants.Plugin.ID, "textures/gui/gui.png");
            LOCATION_VANILLA_ACHIEVEMENT_BACKGROUND_SPRITESHEET = new ResourceLocation("textures/gui/achievement/achievement_background.png");
            ALMURA_LOGO_LOCATION = new ResourceLocation(Constants.Plugin.ID, "textures/gui/almura_logo.png");
            ALMURA_MAN_LOCATION = new ResourceLocation(Constants.Plugin.ID, "textures/gui/almura_man.png");
            SPONGIE_LOCATION = new ResourceLocation(Constants.Plugin.ID, "textures/gui/spongie.png");
            SPONGEPOWERED_LOGO_LOCATION = new ResourceLocation(Constants.Plugin.ID, "textures/gui/spongepowered_logo.png");
            AVATAR_GENERIC_LOCATION = new ResourceLocation(Constants.Plugin.ID, "textures/gui/skins/avatars/generic.png");

            TEXTURE_SPRITESHEET = new GuiTexture(LOCATION_GUI_SPRITE_SHEET, 300, 144);
            VANILLA_ACHIEVEMENT_BACKGROUND_SPRITESHEET = new GuiTexture(LOCATION_VANILLA_ACHIEVEMENT_BACKGROUND_SPRITESHEET, 256, 256);
            VANILLA_ICON_SPRITESHEET = new GuiTexture(MalisisGui.ICONS, 256, 256);

            ICON_EMPTY = TEXTURE_SPRITESHEET.getIcon(299, 141, 1, 1);

            VANILLA_ICON_HEART_BACKGROUND = VANILLA_ICON_SPRITESHEET.getIcon(16, 0, 9, 9);
            VANILLA_ICON_HEART_FOREGROUND = VANILLA_ICON_SPRITESHEET.getIcon(52, 0, 9, 9);
            VANILLA_ICON_HUNGER_BACKGROUND = VANILLA_ICON_SPRITESHEET.getIcon(16, 27, 9, 9);
            VANILLA_ICON_HUNGER_FOREGROUND = VANILLA_ICON_SPRITESHEET.getIcon(52, 27, 9, 9);
            VANILLA_ICON_ARMOR = VANILLA_ICON_SPRITESHEET.getIcon(34, 9, 9, 9);
            VANILLA_ICON_AIR = VANILLA_ICON_SPRITESHEET.getIcon(16, 18, 9, 9);
            VANILLA_ICON_MOUNT = VANILLA_ICON_SPRITESHEET.getIcon(88, 9, 9, 9);

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

            ICON_ORB_BLUE = TEXTURE_SPRITESHEET.getIcon(224, 66, 15, 15);
            ICON_ORB_BROWN = TEXTURE_SPRITESHEET.getIcon(224, 81, 15, 15);
            ICON_ORB_GRAY = TEXTURE_SPRITESHEET.getIcon(209, 81, 15, 15);
            ICON_ORB_GREEN = TEXTURE_SPRITESHEET.getIcon(209, 66, 15, 15);

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
            UNIQUE_ID_WOLFEYE = UUID.fromString("33f9598e-9890-4f76-90ff-12cd73ca1e3c");
            UNIQUE_ID_ZIDANE = UUID.fromString("85271de5-8380-4db5-9f05-ada3b4aa785c");
        }
    }
}
