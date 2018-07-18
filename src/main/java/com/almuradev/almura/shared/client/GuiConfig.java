/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client;

import com.almuradev.almura.Almura;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.renderer.icon.GuiIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface GuiConfig {

    @SideOnly(Side.CLIENT)
    interface Button {

        int WIDTH_TINY = 64;
        int WIDTH_ICON = 24;
        int WIDTH_SHORT = 98;
        int WIDTH_LONG = 200;

        int HEIGHT = 20;
        int HEIGHT_ICON = 24;
    }

    @SideOnly(Side.CLIENT)
    interface Icon {

        GuiIcon VANILLA_CONTAINER_INVENTORY_ADVANCEMENT = SpriteSheet.VANILLA_CONTAINER_INVENTORY.getXYResizableIcon(141, 166, 24, 24, 5);
        GuiIcon EMPTY = SpriteSheet.ALMURA.getIcon(299, 141, 1, 1);
        GuiIcon VANILLA_HEART_BACKGROUND = SpriteSheet.VANILLA_ICON.getIcon(16, 0, 9, 9);
        GuiIcon VANILLA_HEART_FOREGROUND = SpriteSheet.VANILLA_ICON.getIcon(52, 0, 9, 9);
        GuiIcon VANILLA_HUNGER_BACKGROUND = SpriteSheet.VANILLA_ICON.getIcon(16, 27, 9, 9);
        GuiIcon VANILLA_HUNGER_FOREGROUND = SpriteSheet.VANILLA_ICON.getIcon(52, 27, 9, 9);
        GuiIcon VANILLA_ARMOR = SpriteSheet.VANILLA_ICON.getIcon(34, 9, 9, 9);
        GuiIcon VANILLA_AIR = SpriteSheet.VANILLA_ICON.getIcon(16, 18, 9, 9);
        GuiIcon VANILLA_MOUNT = SpriteSheet.VANILLA_ICON.getIcon(88, 9, 9, 9);
        GuiIcon ENJIN = SpriteSheet.ALMURA.getIcon(0, 90, 16, 16);
        GuiIcon FA_GITHUB = SpriteSheet.ALMURA.getIcon(16, 90, 14, 14);
        GuiIcon FA_COG = SpriteSheet.ALMURA.getIcon(32, 90, 15, 15);
        GuiIcon FA_SITEMAP = SpriteSheet.ALMURA.getIcon(48, 90, 16, 14);
        GuiIcon FA_MAP = SpriteSheet.ALMURA.getIcon(64, 90, 16, 15);
        GuiIcon FA_PIE_CHART = SpriteSheet.ALMURA.getIcon(80, 90, 15, 15);
        GuiIcon FA_TROPHY = SpriteSheet.ALMURA.getIcon(96, 90, 16, 14);
        GuiIcon FA_BOOK = SpriteSheet.ALMURA.getIcon(112, 90, 16, 14);
        GuiIcon FA_SHOPPING_BAG = SpriteSheet.ALMURA.getIcon(128, 90, 14, 14);
        GuiIcon STAMINA = SpriteSheet.ALMURA.getIcon(132, 65, 9, 9);

        /*
         * Note: you must call .setSpritesheet(GuiConfig.SpriteSheet."SPRITENAME" if you are using custom icon's in-game.
         */
    }

    @SideOnly(Side.CLIENT)
    interface Location {

        ResourceLocation ALMURA_LOGO = new ResourceLocation(Almura.ID, "textures/gui/almura_logo.png");
        ResourceLocation ALMURA_MAN = new ResourceLocation(Almura.ID, "textures/gui/almura_man.png");
        ResourceLocation SPONGEPOWERED_LOGO = new ResourceLocation(Almura.ID, "textures/gui/spongepowered_logo.png");
        ResourceLocation SPONGEPOWERED_SPONGIE = new ResourceLocation(Almura.ID, "textures/gui/spongie.png");
        ResourceLocation GENERIC_AVATAR = new ResourceLocation(Almura.ID, "textures/gui/skins/avatars/generic.png");
    }

    @SideOnly(Side.CLIENT)
    interface SpriteSheet {

        ResourceLocation ALMURA_LOCATION = new ResourceLocation(Almura.ID, "textures/gui/gui.png");
        GuiTexture ALMURA = new GuiTexture(ALMURA_LOCATION, 284, 106);
        GuiTexture VANILLA_ICON = new GuiTexture(MalisisGui.ICONS, 256, 256);
        ResourceLocation VANILLA_CONTAINER_INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/inventory.png");
        GuiTexture VANILLA_CONTAINER_INVENTORY = new GuiTexture(VANILLA_CONTAINER_INVENTORY_LOCATION, 256, 256);
    }

    @SideOnly(Side.CLIENT)
    interface Url {

        String FORUM = "https://almura.enjin.com/forum";
        String ISSUES = "https://github.com/AlmuraDev/Almura/issues";
        String MAP = "http://srv1.almuramc.com:8123";
        String SHOP = "http://www.almuramc.com"; // TODO: Proper URL
        String SKINS = "https://mc-heads.net/avatar/%s/%d.png";
        String STATISTICS = "http://srv1.almuramc.com:8080";
    }
}
