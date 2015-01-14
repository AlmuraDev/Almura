/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.common.FMLCommonHandler;

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
    public static boolean DISPLAY_ENHANCED_DEBUG;
    //RENDER DISTANCE WITHIN SIGNS AND CHEST MIXINS
    public static int CHEST_RENDER_DISTANCE;
    public static int SIGN_RENDER_DISTANCE;

    private static YamlConfiguration reader;

    static {
        DEBUG_LANGUAGES_MODE = false;
        DEBUG_PACKS_MODE = false;
        DISPLAY_ENHANCED_GUI = true;
        try {
            load();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() throws ConfigurationException {
        reader = new YamlConfiguration(Filesystem.CONFIG_SETTINGS_PATH.toFile());
        reader.load();

        final ConfigurationNode debugConfigurationNode = reader.getNode("debug");
        DEBUG_MODE = debugConfigurationNode.getChild("all").getBoolean(false);
        DEBUG_LANGUAGES_MODE = debugConfigurationNode.getChild("language").getBoolean(false);
        DEBUG_PACKS_MODE = debugConfigurationNode.getChild("pack").getBoolean(false);
        DEBUG_MAPPINGS_MODE = debugConfigurationNode.getChild("mappings").getBoolean(false);
        DEBUG_RECIPES_MODE = debugConfigurationNode.getChild("recipes").getBoolean(false);

        final ConfigurationNode clientConfigurationNode = reader.getNode("client");
        DISPLAY_ENHANCED_GUI = clientConfigurationNode.getChild("enhanced-gui").getBoolean(true);
        DISPLAY_ENHANCED_DEBUG = clientConfigurationNode.getChild("enhanced-debug").getBoolean(true);
        CHEST_RENDER_DISTANCE = clientConfigurationNode.getChild("chest-render-distance").getInt(32);
        SIGN_RENDER_DISTANCE = clientConfigurationNode.getChild("sign-render-distance").getInt(32);

    }

    public static void save() throws ConfigurationException {
        reader.getConfiguration();
        // In-Game Almura GUI
        ConfigurationNode enhanced_gui = reader.getNode("client.enhanced-gui");
        enhanced_gui.setValue(DISPLAY_ENHANCED_GUI);
        reader.setNode(enhanced_gui);

        // In-Game Almura GUI
        ConfigurationNode enhanced_debug = reader.getNode("client.enhanced-debug");
        enhanced_debug.setValue(DISPLAY_ENHANCED_DEBUG);
        reader.setNode(enhanced_debug);
        
        // In-Game Render Distance for Chests
        ConfigurationNode chest_render_distance = reader.getNode("client.chest-render-distance");
        chest_render_distance.setValue(CHEST_RENDER_DISTANCE);
        reader.setNode(chest_render_distance);
        
        // In-Game Render Distance for Signs
        ConfigurationNode sign_render_distance = reader.getNode("client.sign-render-distance");
        sign_render_distance.setValue(SIGN_RENDER_DISTANCE);
        reader.setNode(sign_render_distance);

        // Debug All
        ConfigurationNode debug_mode = reader.getNode("debug.all");
        debug_mode.setValue(DEBUG_MODE);
        reader.setNode(debug_mode);

        // Debug Language Mode
        ConfigurationNode debug_language = reader.getNode("debug.language");
        debug_language.setValue(DEBUG_LANGUAGES_MODE);
        reader.setNode(debug_language);

        // Debug Packs Mode
        ConfigurationNode debug_packs = reader.getNode("debug.pack");
        debug_packs.setValue(DEBUG_PACKS_MODE);
        reader.setNode(debug_packs);

        // Debug Mappings Mode
        ConfigurationNode debug_mappings = reader.getNode("debug.mappings");
        debug_mappings.setValue(DEBUG_MAPPINGS_MODE);
        reader.setNode(debug_mappings);

        // Debug Recipes Mode
        ConfigurationNode debug_recipes = reader.getNode("debug.recipes");
        debug_recipes.setValue(DEBUG_RECIPES_MODE);
        reader.setNode(debug_recipes);

        reader.save();
    }

    public static void toggleEnhancedGUI(boolean value) {
        DISPLAY_ENHANCED_GUI = value;
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
    
    public static void setChestRenderDinstance(int value) {
        CHEST_RENDER_DISTANCE = value;
    }
    
    public static void setSignRenderDinstance(int value) {
        SIGN_RENDER_DISTANCE = value;
    }
}
