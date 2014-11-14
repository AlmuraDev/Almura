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

    public static final boolean DEBUG_MODE;
    public static final boolean DEBUG_LANGUAGES_MODE;
    public static final boolean DEBUG_PACKS_MODE;
    public static final boolean IS_SERVER = FMLCommonHandler.instance().getEffectiveSide().isServer();
    public static final boolean IS_CLIENT = FMLCommonHandler.instance().getEffectiveSide().isClient();

    static {
        final YamlConfiguration reader = new YamlConfiguration(Filesystem.CONFIG_SETTINGS_PATH.toFile());
        try {
            reader.load();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        DEBUG_MODE = reader.getChild("debug").getChild("all", true).getBoolean(false);
        DEBUG_LANGUAGES_MODE = reader.getChild("debug").getChild("language", true).getBoolean(false);
        DEBUG_PACKS_MODE = reader.getChild("debug").getChild("debug").getChild("pack", true).getBoolean(false);
    }
}
