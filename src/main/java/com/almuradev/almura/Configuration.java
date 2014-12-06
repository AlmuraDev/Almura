/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.common.FMLCommonHandler;

public class Configuration {

    public static final boolean IS_SERVER = FMLCommonHandler.instance().getEffectiveSide().isServer();
    public static final boolean IS_CLIENT = FMLCommonHandler.instance().getEffectiveSide().isClient();
    public static boolean DEBUG_MODE;
    public static boolean DEBUG_LANGUAGES_MODE;
    public static boolean DEBUG_PACKS_MODE;
    public static boolean ALMURA_GUI;

    static {
        DEBUG_LANGUAGES_MODE = false;
        DEBUG_PACKS_MODE = false;
        ALMURA_GUI = true;
        reloadConfig();
    }

    public static void reloadConfig() {
        final YamlConfiguration reader = new YamlConfiguration(Filesystem.CONFIG_SETTINGS_PATH.toFile());
        try {
            reader.load();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        ALMURA_GUI = reader.getChild("almura_gui").getBoolean(true);
        DEBUG_MODE = reader.getChild("debug", true).getChild("all", true).getBoolean(false);
        DEBUG_LANGUAGES_MODE = reader.getChild("debug", true).getChild("language", true).getBoolean(false);
        DEBUG_PACKS_MODE = reader.getChild("debug", true).getChild("pack", true).getBoolean(false);
    }
}
