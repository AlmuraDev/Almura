/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.client.config;

import com.almuradev.almura.feature.hud.HUDType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
@SideOnly(Side.CLIENT)
public final class ClientCategory {

    @Setting(value = "first-launch", comment = "Instructs Almura if this is our first launch. Internal use only")
    public boolean firstLaunch = true;

    @Setting(comment = "The hud type to use. Valid options are '" + HUDType.ORIGIN + "' and '" + HUDType.VANILLA + "'")
    public String hud = HUDType.ORIGIN;

    @Setting(value = "enhanced-debug", comment = "Toggles Almura's unique custom debug screen")
    public boolean enhancedDebug = true;

    @Setting(value = "display-animal-heat-status", comment = "Toggles the showing of an Animal's heat status above their head")
    public boolean displayAnimalHeatStatus = true;

    @Setting(value = "chest-render-distance", comment = "Controls a chest's render distance. Valid options are 0|16|32|64|128 (blocks)")
    public int chestRenderDistance = 16;

    @Setting(value = "item-frame-render-distance", comment = "Controls an item frame's render distance. Valid options are 0|16|32|64|128 (blocks)")
    public int itemFrameRenderDistance = 16;

    @Setting(value = "player-name-render-distance", comment = "Controls the Player Name render distance")
    public int playerNameRenderDistance = 8;

    @Setting(value = "enemy-name-render-distance", comment = "Controls the Enemy Name render distance")
    public int enemyNameRenderDistance = 8;

    @Setting(value = "animal-name-render-distance", comment = "Controls the Animal Name render distance")
    public int animalNameRenderDistance = 4;

    @Setting(value = "sign-text-render-distance", comment = "Controls a sign text's render distance. Valid options are 0|16|32|64|128 (blocks)")
    public int signTextRenderDistance = 16;

    @Setting(value = "origin-hud-opacity", comment = "Determines the opacity used for the Origin HUD. Default: 255; Range: 0-255")
    public int originHudOpacity = 255;

    @Setting(value = "display-world-compass-widget", comment = "Toggles the center world / compass widget of the Origin HUD")
    public boolean displayWorldCompassWidget = true;

    @Setting(value = "display-location-widget", comment = "Toggles the right-side location widget of the Origin HUD")
    public boolean displayLocationWidget = true;

    @Setting(value = "display-numeric-hud-values", comment = "Toggles the display of numeric values in the Origin HUD")
    public boolean displayNumericHUDValues = true;
}
