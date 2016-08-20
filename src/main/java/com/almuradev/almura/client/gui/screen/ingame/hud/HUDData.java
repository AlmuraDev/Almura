/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class HUDData {

    public static final String COMPASS_CHARACTERS = "S.......W.......N.......E.......";
    public static final String TIME_FORMAT = "%2d%s";

    public static String PLAYER_CURRENCY = "";
    public static String WORLD_NAME = "";
    public static String SERVER_COUNT = "";


    private HUDData() {
    }

    public static String getCompass() {
        final int position = (int) ((((Minecraft.getMinecraft().thePlayer.rotationYaw + 11.25) % 360 + 360) % 360) / 360 * 32);

        return "" + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position - 8) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position - 7) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position - 6) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position - 5) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position - 4) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position - 3) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position - 2) & 31)
                + TextFormatting.GRAY + HUDData.COMPASS_CHARACTERS.charAt((position - 1) & 31)
                + TextFormatting.WHITE + HUDData.COMPASS_CHARACTERS.charAt((position) & 31)
                + TextFormatting.GRAY + HUDData.COMPASS_CHARACTERS.charAt((position + 1) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position + 2) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position + 3) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position + 4) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position + 5) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position + 6) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position + 7) & 31)
                + TextFormatting.DARK_GRAY + HUDData.COMPASS_CHARACTERS.charAt((position + 8) & 31);
    }

    public static String getTime() {
        final int hour = (int) ((Math.floor(Minecraft.getMinecraft().thePlayer.worldObj.getWorldTime() / 1000.0) + 6) % 24);

        if (hour == 0) {
            return String.format(HUDData.TIME_FORMAT, hour + 12, "AM");
        } else if (hour == 12) {
            return String.format(HUDData.TIME_FORMAT, hour, "PM");
        } else if (hour > 12) {
            return String.format(HUDData.TIME_FORMAT, hour - 12, "PM");
        } else {
            return String.format(HUDData.TIME_FORMAT, hour, "AM");
        }
    }
}
