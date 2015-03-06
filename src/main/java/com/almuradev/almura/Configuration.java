/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;

public class Configuration {

    public static final boolean IS_SERVER = FMLCommonHandler.instance().getEffectiveSide().isServer();
    public static final boolean IS_CLIENT = FMLCommonHandler.instance().getEffectiveSide().isClient();

    //DEBUG
    public static boolean DEBUG_MODE;
    public static boolean DEBUG_LANGUAGES_MODE;
    public static boolean DEBUG_PACKS_MODE;
    public static boolean DEBUG_MAPPINGS_MODE;
    public static boolean DEBUG_RECIPES_MODE;
    //GUI
    public static boolean DISPLAY_ENHANCED_GUI;
    public static boolean DISPLAY_RESIDENCE_HUD;
    public static boolean DISPLAY_ENHANCED_DEBUG;
    public static boolean CHAT_NOTIFICATIONS;
    //RENDER DISTANCE WITHIN SIGNS AND CHEST MIXINS
    public static int CHEST_RENDER_DISTANCE;
    public static int SIGN_RENDER_DISTANCE;
    public static int ITEM_FRAME_RENDER_DISTANCE;
    //First Launch Configuration Check
    public static boolean FIRST_LAUNCH;

    private static YAMLConfigurationLoader reader;
    private static ConfigurationNode root;

    static {
        DEBUG_LANGUAGES_MODE = false;
        DEBUG_PACKS_MODE = false;
        DISPLAY_ENHANCED_GUI = true;
    }

    public static void load() throws IOException {
        reader = YAMLConfigurationLoader.builder().setFile(Filesystem.CONFIG_SETTINGS_PATH.toFile()).setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                .build();
        root = reader.load();

        final ConfigurationNode debugConfigurationNode = root.getNode("debug");
        DEBUG_MODE = debugConfigurationNode.getChild("all").getBoolean(false);
        DEBUG_LANGUAGES_MODE = debugConfigurationNode.getChild("language").getBoolean(false);
        DEBUG_PACKS_MODE = debugConfigurationNode.getChild("pack").getBoolean(false);
        DEBUG_MAPPINGS_MODE = debugConfigurationNode.getChild("mappings").getBoolean(false);
        DEBUG_RECIPES_MODE = debugConfigurationNode.getChild("recipes").getBoolean(false);

        final ConfigurationNode clientConfigurationNode = root.getNode("client");
        DISPLAY_ENHANCED_GUI = clientConfigurationNode.getChild("enhanced-gui").getBoolean(true);
        DISPLAY_RESIDENCE_HUD = clientConfigurationNode.getChild("residence-hud").getBoolean(true);
        DISPLAY_ENHANCED_DEBUG = clientConfigurationNode.getChild("enhanced-debug").getBoolean(true);
        CHEST_RENDER_DISTANCE = clientConfigurationNode.getChild("chest-render-distance").getInt(32);
        SIGN_RENDER_DISTANCE = clientConfigurationNode.getChild("sign-render-distance").getInt(32);
        ITEM_FRAME_RENDER_DISTANCE = clientConfigurationNode.getChild("item-frame-render-distance").getInt(32);
        FIRST_LAUNCH = clientConfigurationNode.getChild("first-launch").getBoolean(true);
        CHAT_NOTIFICATIONS = clientConfigurationNode.getChild("chat-notifications").getBoolean(true);
    }

    public static void save() throws IOException {
        // In-Game Almura GUI
        ConfigurationNode enhanced_gui = root.getNode("client.enhanced-gui");
        enhanced_gui.setValue(DISPLAY_ENHANCED_GUI);

        // In-Game Residence Hud
        ConfigurationNode residence_hud = root.getNode("client.residence-hud");
        residence_hud.setValue(DISPLAY_RESIDENCE_HUD);

        // In-Game Almura GUI
        ConfigurationNode enhanced_debug = root.getNode("client.enhanced-debug");
        enhanced_debug.setValue(DISPLAY_ENHANCED_DEBUG);

        // In-Game Render Distance for Chests
        ConfigurationNode chest_render_distance = root.getNode("client.chest-render-distance");
        chest_render_distance.setValue(CHEST_RENDER_DISTANCE);

        // In-Game Render Distance for Signs
        ConfigurationNode sign_render_distance = root.getNode("client.sign-render-distance");
        sign_render_distance.setValue(SIGN_RENDER_DISTANCE);

        // In-Game Render Distance for Item Frames
        ConfigurationNode item_frame_render_distance = root.getNode("client.item-frame-render-distance");
        item_frame_render_distance.setValue(ITEM_FRAME_RENDER_DISTANCE);

        // Debug All
        ConfigurationNode debug_mode = root.getNode("debug.all");
        debug_mode.setValue(DEBUG_MODE);

        // Debug Language Mode
        ConfigurationNode debug_language = root.getNode("debug.language");
        debug_language.setValue(DEBUG_LANGUAGES_MODE);

        // Debug Packs Mode
        ConfigurationNode debug_packs = root.getNode("debug.pack");
        debug_packs.setValue(DEBUG_PACKS_MODE);

        // Debug Mappings Mode
        ConfigurationNode debug_mappings = root.getNode("debug.mappings");
        debug_mappings.setValue(DEBUG_MAPPINGS_MODE);

        // Debug Recipes Mode
        ConfigurationNode debug_recipes = root.getNode("debug.recipes");
        debug_recipes.setValue(DEBUG_RECIPES_MODE);

        // Chat Notifications
        ConfigurationNode chat_notifications = root.getNode("client.chat-notifications");
        chat_notifications.setValue(CHAT_NOTIFICATIONS);

        reader.save(root);
    }

    public static void toggleEnhancedGUI(boolean value) {
        DISPLAY_ENHANCED_GUI = value;
    }

    public static void toggleResidenceHUD(boolean value) {
        DISPLAY_RESIDENCE_HUD = value;
    }

    public static void toggleEnhancedDebug(boolean value) {
        DISPLAY_ENHANCED_DEBUG = value;
    }

    public static void toggleDebugMode(boolean value) {
        DEBUG_MODE = value;
    }

    public static void toggleDebugLanguageMode(boolean value) {
        DEBUG_LANGUAGES_MODE = value;
    }

    public static void toggleDebugPacksMode(boolean value) {
        DEBUG_PACKS_MODE = value;
    }

    public static void toggleDebugMappingsMode(boolean value) {
        DEBUG_MAPPINGS_MODE = value;
    }

    public static void toggleDebugRecipesMode(boolean value) {
        DEBUG_RECIPES_MODE = value;
    }

    public static void setChestRenderDistance(int value) {
        CHEST_RENDER_DISTANCE = value;
    }

    public static void setSignRenderDistance(int value) {
        SIGN_RENDER_DISTANCE = value;
    }

    public static void setItemFrameRenderDistance(int value) {
        ITEM_FRAME_RENDER_DISTANCE = value;
    }

    public static void setChatNotifications(boolean value) {
        CHAT_NOTIFICATIONS = value;
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public static void setOptimizedConfig() {
        final Minecraft mc = Minecraft.getMinecraft();
        mc.gameSettings.ambientOcclusion = 0;
        mc.gameSettings.mipmapLevels = 0;
        mc.gameSettings.guiScale = 3;
        mc.gameSettings.advancedOpengl = true;
        mc.gameSettings.anisotropicFiltering = 0;
        mc.gameSettings.limitFramerate = 120;
        mc.gameSettings.enableVsync = false;
        mc.gameSettings.clouds = false;
        mc.gameSettings.snooperEnabled = false;
        mc.gameSettings.renderDistanceChunks = 12;
        mc.gameSettings.viewBobbing = false;
        if (!mc.gameSettings.resourcePacks.contains("Almura Preferred Font.zip")) {
            mc.gameSettings.resourcePacks.add("Almura Preferred Font.zip");
        }
        mc.gameSettings.saveOptions();
    }

    public static void setFirstLaunched(boolean value) {
        ConfigurationNode first_Launch = root.getNode("client.first-launch");
        first_Launch.setValue(value);

        try {
            reader.save(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
