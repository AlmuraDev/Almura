/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame.hud;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@SideOnly(Side.CLIENT)
public final class HUDData {

    private static final String COMPASS_CHARACTERS = "S.......W.......N.......E.......";
    public static boolean IS_ECONOMY_PRESENT = false;
    public static String PLAYER_CURRENCY = "1,000,000,000";
    public static String WORLD_NAME = "";
    public static int SERVER_PLAYER_COUNT, SERVER_PLAYER_MAX_COUNT = 0;

    private HUDData() {
    }

    public static Text getCompass() {
        final int position = (int) ((((Minecraft.getMinecraft().player.rotationYaw + 11.25) % 360 + 360) % 360) / 360 * 32);

        return Text.of(TextColors.DARK_GRAY,
                HUDData.COMPASS_CHARACTERS.charAt((position - 8) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position - 7) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position - 6) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position - 5) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position - 4) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position - 3) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position - 2) & 31),
                TextColors.GRAY, HUDData.COMPASS_CHARACTERS.charAt((position - 1) & 31),
                TextColors.WHITE, HUDData.COMPASS_CHARACTERS.charAt((position) & 31),
                TextColors.GRAY, HUDData.COMPASS_CHARACTERS.charAt((position + 1) & 31),
                TextColors.DARK_GRAY, HUDData.COMPASS_CHARACTERS.charAt((position + 2) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position + 3) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position + 4) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position + 5) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position + 6) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position + 7) & 31),
                HUDData.COMPASS_CHARACTERS.charAt((position + 8) & 31));
    }
}
