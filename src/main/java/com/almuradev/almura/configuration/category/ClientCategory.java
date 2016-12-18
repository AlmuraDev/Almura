/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.category;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@SideOnly(Side.CLIENT)
@ConfigSerializable
public final class ClientCategory {

    @Setting(value = "enhanced-menus", comment = "Toggles Almura's unique custom GUI menus")
    public boolean enhancedGui = true;

    @Setting(value = "first-launch", comment = "Instructs Almura if this is our first launch. Internal use only")
    public boolean firstLaunch = true;

    @Setting(comment = "The hud type to use. Valid options are Origin|Minimal|Legacy|Default")
    public String hud = "Origin";

    @Setting(value = "enhanced-debug", comment = "Toggles Almura's unique custom debug screen")
    public boolean enhancedDebug = true;

    @Setting(value = "display-animal-heat-status", comment = "Toggles the showing of an Animal's heat status above their head")
    public boolean displayAnimalHeatStatus = true;

    @Setting(value = "chest-render-distance", comment = "Controls a chest's render distance. Valid options are 16|32|64 (blocks)")
    public int chestRenderDistance = 16;

    @Setting(value = "item-frame-render-distance", comment = "Controls an item frame's render distance. Valid options are 16|32|64 (blocks)")
    public int itemFrameRenderDistance = 16;

    @Setting(value = "sign-text-render-distance", comment = "Controls a sign text's render distance. Valid options are 16|32|64 (blocks)")
    public int signTextRenderDistance = 16;

    @Setting(value = "chat-notifications", comment = "Toggles if another player saying your name in chat visibly alerts you")
    public boolean chatNotifications = true;

    @Setting(value = "optimized-lighting", comment = "Toggles multi-thread client lighting updates. May cause visual glitches")
    public boolean optimizedLighting = true;

    /**
     * Called on first launch to optimize the client's GUI settings. Addresses many users lack of knowledge of
     * the various settings that can lead to better FPS. Improves overall experience with Almura.
     */
    public void optimizeGame() {
        // TODO Dockter, update your optimization changes
        final Minecraft mc = Minecraft.getMinecraft();
        mc.gameSettings.ambientOcclusion = 0;
        mc.gameSettings.mipmapLevels = 0;
        mc.gameSettings.guiScale = 3;
        //mc.gameSettings.advancedOpengl = true;
        //mc.gameSettings.anisotropicFiltering = 0;
        mc.gameSettings.limitFramerate = 120;
        mc.gameSettings.enableVsync = false;
        //mc.gameSettings.clouds = false;
        mc.gameSettings.snooperEnabled = false;
        mc.gameSettings.renderDistanceChunks = 12;
        mc.gameSettings.viewBobbing = false;
        mc.gameSettings.resourcePacks.add("Almura Preferred Font.zip");
        mc.gameSettings.saveOptions();
    }
}
