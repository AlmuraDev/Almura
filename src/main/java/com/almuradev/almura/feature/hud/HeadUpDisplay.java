/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Singleton;

@Singleton
public class HeadUpDisplay {

    private static final String COMPASS_CHARACTERS = "S.......W.......N.......E.......";

    public String worldName = "";
    public int onlinePlayerCount = 0;
    public int maxPlayerCount = 0;

    public boolean isEconomyPresent = false;
    public String economyAmount = "";

    @SideOnly(Side.CLIENT)
    public Text getCompass() {
        final int position = (int) ((((Minecraft.getMinecraft().player.rotationYaw + 5.25) % 360 + 360) % 360) / 360 * 32);

        return Text.of(TextColors.DARK_GRAY,
                COMPASS_CHARACTERS.charAt((position - 8) & 31),
                COMPASS_CHARACTERS.charAt((position - 7) & 31),
                COMPASS_CHARACTERS.charAt((position - 6) & 31),
                COMPASS_CHARACTERS.charAt((position - 5) & 31),
                COMPASS_CHARACTERS.charAt((position - 4) & 31),
                COMPASS_CHARACTERS.charAt((position - 3) & 31),
                COMPASS_CHARACTERS.charAt((position - 2) & 31),
                TextColors.GRAY, COMPASS_CHARACTERS.charAt((position - 1) & 31),
                TextColors.WHITE, COMPASS_CHARACTERS.charAt((position) & 31),
                TextColors.GRAY, COMPASS_CHARACTERS.charAt((position + 1) & 31),
                TextColors.DARK_GRAY, COMPASS_CHARACTERS.charAt((position + 2) & 31),
                COMPASS_CHARACTERS.charAt((position + 3) & 31),
                COMPASS_CHARACTERS.charAt((position + 4) & 31),
                COMPASS_CHARACTERS.charAt((position + 5) & 31),
                COMPASS_CHARACTERS.charAt((position + 6) & 31),
                COMPASS_CHARACTERS.charAt((position + 7) & 31),
                COMPASS_CHARACTERS.charAt((position + 8) & 31));
    }
}
