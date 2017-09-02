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
import org.apache.commons.lang3.text.WordUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

    public interface Plugin {

        String ID = "almura";
        String NAME = WordUtils.capitalize(ID);
        String PROXY_CLIENT_CLASSPATH = "com.almuradev.almura.client.ClientProxy";
        String PROXY_SERVER_CLASSPATH = "com.almuradev.almura.server.ServerProxy";
        String NETWORK_CHANNEL = "AM|FOR";
        String ASSETS_VERSION = "3.0";
    }

    public interface FileSystem {

        Path PATH_ROOT = Paths.get(".");

        Path PATH_ASSETS = PATH_ROOT.resolve("assets");
        Path PATH_ASSETS_ALMURA = PATH_ASSETS.resolve(Plugin.ID);
        Path PATH_ASSETS_ALMURA_30 = PATH_ASSETS_ALMURA.resolve(Plugin.ASSETS_VERSION);
        Path PATH_ASSETS_ALMURA_30_PACKS = PATH_ASSETS_ALMURA_30.resolve("packs");

        Path PATH_CONFIG = PATH_ROOT.resolve("config");
        Path PATH_CONFIG_ALMURA = PATH_CONFIG.resolve(Plugin.ID);

        String CONFIG_CLIENT_NAME = "client.conf";
    }

    public interface Model {

        String COMMENT = "#";

        interface Obj {

            String MATERIAL_LIBRARY = "mtllib";
            String USE_MATERIAL = "usemtl";
            String VERTEX = "v";
            String VERTEX_NORMAL = "vn";
            String VERTEX_TEXTURE_COORDINATE = "vt";
            String GROUP = "g";
            String FACE = "f";
            String PERSPECTIVE = "pe";
            String TRANSLATION = "tn";
            String ROTATION = "rn";
            String SCALE = "se";
        }

        interface Material {

            String NEW_MATERIAL = "newmtl";
            String DIFFUSE = "map_Kd";
        }

    }

    public interface Config {

        String HEADER = "2.0\nAlmura configuration\n\nFor further assistance, join #almura on EsperNet.";

    }

    @SideOnly(Side.CLIENT)
    public static final class Gui {

        public static final String FORUM_URL = "http://www.almuramc.com";
        public static final String ISSUES_URL = "https://github.com/AlmuraDev/Almura/issues";
        public static final String MAP_URL = "http://srv1.almuramc.com:8123";
        public static final String SHOP_URL = "http://www.almura.com"; // TODO: Proper URL
        public static final String SKIN_URL_BASE = "https://mc-heads.net/avatar/%s/%d.png";
        public static final String STATISTICS_URL = "http://srv1.almuramc.com:8080";
        public static final ResourceLocation LOCATION_SPRITE_SHEET_ALMURA_GUI;
        public static final ResourceLocation LOCATION_SPRITE_SHEET_VANILLA_CONTAINER_INVENTORY_ADVANCEMENT;
        public static final ResourceLocation LOCATION_ALMURA_LOGO;
        public static final ResourceLocation LOCATION_ALMURA_MAN;
        public static final ResourceLocation LOCATION_SPONGEPOWERED_SPONGIE;
        public static final ResourceLocation LOCATION_SPONGEPOWERED_LOGO;
        public static final ResourceLocation LOCATION_AVATAR_GENERIC;
        public static final GuiTexture SPRITE_SHEET_ALMURA;
        public static final GuiTexture SPRITE_SHEET_VANILLA_ICON;
        public static final GuiTexture SPRITE_SHEET_VANILLA_CONTAINER_INVENTORY;
        public static final GuiIcon ICON_VANILLA_CONTAINER_INVENTORY_ADVANCEMENT;
        public static final GuiIcon ICON_EMPTY;
        public static final GuiIcon ICON_VANILLA_HEART_BACKGROUND;
        public static final GuiIcon ICON_VANILLA_HEART_FOREGROUND;
        public static final GuiIcon ICON_VANILLA_HUNGER_BACKGROUND;
        public static final GuiIcon ICON_VANILLA_HUNGER_FOREGROUND;
        public static final GuiIcon ICON_VANILLA_ARMOR;
        public static final GuiIcon ICON_VANILLA_AIR;
        public static final GuiIcon ICON_VANILLA_MOUNT;
        public static final GuiIcon ICON_ENJIN;
        public static final GuiIcon ICON_FA_GITHUB;
        public static final GuiIcon ICON_FA_COG;
        public static final GuiIcon ICON_FA_SITEMAP;
        public static final GuiIcon ICON_FA_MAP;
        public static final GuiIcon ICON_FA_PIE_CHART;
        public static final GuiIcon ICON_FA_TROPHY;
        public static final GuiIcon ICON_FA_BOOK;
        public static final GuiIcon ICON_FA_SHOPPING_BAG;
        public static final int BUTTON_WIDTH_LONG = 200;
        public static final int BUTTON_WIDTH_SHORT = 98;
        public static final int BUTTON_WIDTH_TINY = 64;
        public static final int BUTTON_WIDTH_ICON = 24;
        public static final int BUTTON_HEIGHT_ICON = 24;
        public static final int BUTTON_HEIGHT = 20;

        static {
            LOCATION_SPRITE_SHEET_ALMURA_GUI = new ResourceLocation(Constants.Plugin.ID, "textures/gui/gui.png");
            LOCATION_SPRITE_SHEET_VANILLA_CONTAINER_INVENTORY_ADVANCEMENT = new ResourceLocation("textures/gui/container/inventory.png");
            LOCATION_ALMURA_LOGO = new ResourceLocation(Constants.Plugin.ID, "textures/gui/almura_logo.png");
            LOCATION_ALMURA_MAN = new ResourceLocation(Constants.Plugin.ID, "textures/gui/almura_man.png");
            LOCATION_SPONGEPOWERED_SPONGIE = new ResourceLocation(Constants.Plugin.ID, "textures/gui/spongie.png");
            LOCATION_SPONGEPOWERED_LOGO = new ResourceLocation(Constants.Plugin.ID, "textures/gui/spongepowered_logo.png");
            LOCATION_AVATAR_GENERIC = new ResourceLocation(Constants.Plugin.ID, "textures/gui/skins/avatars/generic.png");

            SPRITE_SHEET_ALMURA = new GuiTexture(LOCATION_SPRITE_SHEET_ALMURA_GUI, 284, 106);
            SPRITE_SHEET_VANILLA_CONTAINER_INVENTORY = new GuiTexture(LOCATION_SPRITE_SHEET_VANILLA_CONTAINER_INVENTORY_ADVANCEMENT, 256, 256);
            SPRITE_SHEET_VANILLA_ICON = new GuiTexture(MalisisGui.ICONS, 256, 256);

            ICON_EMPTY = SPRITE_SHEET_ALMURA.getIcon(299, 141, 1, 1);

            ICON_VANILLA_HEART_BACKGROUND = SPRITE_SHEET_VANILLA_ICON.getIcon(16, 0, 9, 9);
            ICON_VANILLA_HEART_FOREGROUND = SPRITE_SHEET_VANILLA_ICON.getIcon(52, 0, 9, 9);
            ICON_VANILLA_HUNGER_BACKGROUND = SPRITE_SHEET_VANILLA_ICON.getIcon(16, 27, 9, 9);
            ICON_VANILLA_HUNGER_FOREGROUND = SPRITE_SHEET_VANILLA_ICON.getIcon(52, 27, 9, 9);
            ICON_VANILLA_ARMOR = SPRITE_SHEET_VANILLA_ICON.getIcon(34, 9, 9, 9);
            ICON_VANILLA_AIR = SPRITE_SHEET_VANILLA_ICON.getIcon(16, 18, 9, 9);
            ICON_VANILLA_MOUNT = SPRITE_SHEET_VANILLA_ICON.getIcon(88, 9, 9, 9);
            ICON_VANILLA_CONTAINER_INVENTORY_ADVANCEMENT = SPRITE_SHEET_VANILLA_CONTAINER_INVENTORY.getXYResizableIcon(141, 166, 24, 24, 5);

            ICON_ENJIN = SPRITE_SHEET_ALMURA.getIcon(0, 90, 16, 16);

            ICON_FA_GITHUB = SPRITE_SHEET_ALMURA.getIcon(16, 90, 14, 14);
            ICON_FA_COG = SPRITE_SHEET_ALMURA.getIcon(32, 90, 15, 15);
            ICON_FA_SITEMAP = SPRITE_SHEET_ALMURA.getIcon(48, 90, 16, 14);
            ICON_FA_MAP = SPRITE_SHEET_ALMURA.getIcon(64, 90, 16, 15);
            ICON_FA_PIE_CHART = SPRITE_SHEET_ALMURA.getIcon(80, 90, 15, 15);
            ICON_FA_TROPHY = SPRITE_SHEET_ALMURA.getIcon(96, 90, 16, 14);
            ICON_FA_BOOK = SPRITE_SHEET_ALMURA.getIcon(112, 90, 16, 14);
            ICON_FA_SHOPPING_BAG = SPRITE_SHEET_ALMURA.getIcon(128, 90, 14, 14);
        }
    }
}
