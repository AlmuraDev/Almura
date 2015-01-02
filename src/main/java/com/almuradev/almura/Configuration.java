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

    }

    public static void save() throws ConfigurationException {
        reader.save();
    }

    public static void toggleEnhancedGUI(boolean value) {
        DISPLAY_ENHANCED_GUI = value;
    }
}
