/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.util.FileSystem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;

public class Configuration {

    public static final boolean IS_SERVER = FMLCommonHandler.instance().getEffectiveSide().isServer();
    public static final boolean IS_CLIENT = FMLCommonHandler.instance().getEffectiveSide().isClient();
    private static final Object[] PATH_CLIENT_FIRST_LAUNCH = new String[]{"client", "first-launch"};
    private static final Object[] PATH_CLIENT_HUD_TYPE = new String[]{"client", "hud-type"};
    private static final Object[] PATH_CLIENT_ENHANCED_DEBUG = new String[]{"client", "enhanced-debug"};
    private static final Object[] PATH_CLIENT_RESIDENCE_HUD = new String[]{"client", "residence-hud"};
    private static final Object[] PATH_CLIENT_ANIMAL_HEAT = new String[]{"client", "animal-heat"};
    private static final Object[] PATH_CLIENT_CHEST_RENDER_DISTANCE = new String[]{"client", "chest-render-distance"};
    private static final Object[] PATH_CLIENT_ITEM_FRAME_RENDER_DISTANCE = new String[]{"client", "item-frame-render-distance"};
    private static final Object[] PATH_CLIENT_SIGN_RENDER_DISTANCE = new String[]{"client", "sign-render-distance"};
    private static final Object[] PATH_CLIENT_CHAT_NOTIFICATIONS = new String[]{"client", "chat-notifications"};
    private static final Object[] PATH_CLIENT_USE_OPTIMIZED_LIGHTING = new String[]{"client", "use-optimized-lighting"};
    private static final Object[] PATH_DEBUG_ALL = new String[]{"debug", "all"};
    private static final Object[] PATH_DEBUG_LANGUAGE = new String[]{"debug", "language"};
    private static final Object[] PATH_DEBUG_MAPPINGS = new String[]{"debug", "mappings"};
    private static final Object[] PATH_DEBUG_PACK = new String[]{"debug", "pack"};
    private static final Object[] PATH_DEBUG_RECIPES = new String[]{"debug", "recipes"};
    //DEBUG
    public static boolean DEBUG_ALL;
    public static boolean DEBUG_LANGUAGES;
    public static boolean DEBUG_MAPPINGS;
    public static boolean DEBUG_PACKS;
    public static boolean DEBUG_RECIPES;
    //GUI
    public static String HUD_TYPE = "almura";
    public static boolean DISPLAY_RESIDENCE_HUD = true;
    public static boolean DISPLAY_ANIMAL_HEAT = false;
    public static boolean DISPLAY_ENHANCED_DEBUG = true;
    public static boolean CHAT_NOTIFICATIONS = true;
    //RENDER DISTANCE WITHIN SIGNS AND CHEST MIXINS
    public static int DISTANCE_RENDER_CHEST;
    public static int DISTANCE_RENDER_ITEM_FRAME;
    public static int DISTANCE_RENDER_SIGN;
    //PERFORMANCE
    public static boolean USE_OPTIMIZED_LIGHTING = true;
    //FIRST LAUNCH
    public static boolean FIRST_LAUNCH = true;

    @SuppressWarnings("rawtypes")
	private static ConfigurationLoader loader;
    private static ConfigurationNode root;

    public static void load() throws IOException {
        loader = YAMLConfigurationLoader.builder().setFile(FileSystem.CONFIG_SETTINGS_PATH.toFile()).setFlowStyle
                (DumperOptions.FlowStyle.BLOCK).build();
        root = loader.load();

        FIRST_LAUNCH = root.getNode(PATH_CLIENT_FIRST_LAUNCH).getBoolean(true);

        HUD_TYPE = root.getNode(PATH_CLIENT_HUD_TYPE).getString("almura");

        DISPLAY_ENHANCED_DEBUG = root.getNode(PATH_CLIENT_ENHANCED_DEBUG).getBoolean(true);

        DISPLAY_RESIDENCE_HUD = root.getNode(PATH_CLIENT_RESIDENCE_HUD).getBoolean(true);

        DISPLAY_ANIMAL_HEAT = root.getNode(PATH_CLIENT_ANIMAL_HEAT).getBoolean(false);

        DISTANCE_RENDER_CHEST = root.getNode(PATH_CLIENT_CHEST_RENDER_DISTANCE).getInt(32);

        DISTANCE_RENDER_SIGN = root.getNode(PATH_CLIENT_SIGN_RENDER_DISTANCE).getInt(32);

        DISTANCE_RENDER_ITEM_FRAME = root.getNode(PATH_CLIENT_ITEM_FRAME_RENDER_DISTANCE).getInt(32);

        CHAT_NOTIFICATIONS = root.getNode(PATH_CLIENT_CHAT_NOTIFICATIONS).getBoolean(true);

        USE_OPTIMIZED_LIGHTING = root.getNode(PATH_CLIENT_USE_OPTIMIZED_LIGHTING).getBoolean(true);

        DEBUG_ALL = root.getNode(PATH_DEBUG_ALL).getBoolean(false);

        DEBUG_LANGUAGES = root.getNode(PATH_DEBUG_LANGUAGE).getBoolean(false);

        DEBUG_MAPPINGS = root.getNode(PATH_DEBUG_MAPPINGS).getBoolean(false);

        DEBUG_PACKS = root.getNode(PATH_DEBUG_PACK).getBoolean(false);

        DEBUG_RECIPES = root.getNode(PATH_DEBUG_RECIPES).getBoolean(false);
    }

    public static void save() throws IOException {
        root.getNode(PATH_CLIENT_HUD_TYPE).setValue(HUD_TYPE);

        root.getNode(PATH_CLIENT_ENHANCED_DEBUG).setValue(DISPLAY_ENHANCED_DEBUG);

        root.getNode(PATH_CLIENT_RESIDENCE_HUD).setValue(DISPLAY_RESIDENCE_HUD);

        root.getNode(PATH_CLIENT_ANIMAL_HEAT).setValue(DISPLAY_ANIMAL_HEAT);

        root.getNode(PATH_CLIENT_CHEST_RENDER_DISTANCE).setValue(DISTANCE_RENDER_CHEST);

        root.getNode(PATH_CLIENT_ITEM_FRAME_RENDER_DISTANCE).setValue(DISTANCE_RENDER_ITEM_FRAME);

        root.getNode(PATH_CLIENT_SIGN_RENDER_DISTANCE).setValue(DISTANCE_RENDER_SIGN);

        root.getNode(PATH_CLIENT_USE_OPTIMIZED_LIGHTING).setValue(USE_OPTIMIZED_LIGHTING);

        root.getNode(PATH_DEBUG_ALL).setValue(DEBUG_ALL);

        root.getNode(PATH_DEBUG_LANGUAGE).setValue(DEBUG_LANGUAGES);

        root.getNode(PATH_DEBUG_MAPPINGS).setValue(DEBUG_MAPPINGS);

        root.getNode(PATH_DEBUG_PACK).setValue(DEBUG_PACKS);

        root.getNode(PATH_DEBUG_RECIPES).setValue(DEBUG_RECIPES);

        loader.save(root);
    }

    public static void setFirstLaunch(boolean value) throws IOException {
        if (root == null) {
            loader = YAMLConfigurationLoader.builder().setFile(FileSystem.CONFIG_SETTINGS_PATH.toFile()).setFlowStyle(DumperOptions.FlowStyle
                    .BLOCK).build();
            root = loader.load();
        }
        root.getNode(PATH_CLIENT_FIRST_LAUNCH).setValue(value);
        loader.save(root);
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public static void setOptimizedConfig() {
        final Minecraft mc = Minecraft.getMinecraft();
        mc.gameSettings.ambientOcclusion = 0;
        mc.gameSettings.mipmapLevels = 0;
        mc.gameSettings.guiScale = 3;
        mc.gameSettings.limitFramerate = 120;
        mc.gameSettings.enableVsync = false;
        mc.gameSettings.clouds = 1; // Render Fast
        mc.gameSettings.snooperEnabled = false;
        mc.gameSettings.renderDistanceChunks = 12;
        mc.gameSettings.viewBobbing = false;
        if (!mc.gameSettings.resourcePacks.contains("Almura Preferred Font.zip")) {
            mc.gameSettings.resourcePacks.add("Almura Preferred Font.zip");
        }
        mc.gameSettings.saveOptions();
    }

    public static void setHUDType(String value) {
        HUD_TYPE = value.toLowerCase();
    }

    public static void toggleResidenceHUD(boolean value) {
        DISPLAY_RESIDENCE_HUD = value;
    }

    public static void toggleAnimalHeat(boolean value) {
        DISPLAY_ANIMAL_HEAT = value;
    }

    public static void toggleEnhancedDebug(boolean value) {
        DISPLAY_ENHANCED_DEBUG = value;
    }

    public static void toggleDebugMode(boolean value) {
        DEBUG_ALL = value;
    }

    public static void toggleDebugLanguageMode(boolean value) {
        DEBUG_LANGUAGES = value;
    }

    public static void toggleDebugPacksMode(boolean value) {
        DEBUG_PACKS = value;
    }

    public static void toggleDebugMappingsMode(boolean value) {
        DEBUG_MAPPINGS = value;
    }

    public static void toggleDebugRecipesMode(boolean value) {
        DEBUG_RECIPES = value;
    }

    public static void toggleOptimizedLighting(boolean value) {
        USE_OPTIMIZED_LIGHTING = value;
    }

    public static void setChestRenderDistance(int value) {
        DISTANCE_RENDER_CHEST = value;
    }

    public static void setItemFrameRenderDistance(int value) {
        DISTANCE_RENDER_ITEM_FRAME = value;
    }

    public static void setSignRenderDistance(int value) {
        DISTANCE_RENDER_SIGN = value;
    }

    public static void setChatNotifications(boolean value) {
        CHAT_NOTIFICATIONS = value;
    }
}
