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

    public static final boolean IS_DEBUG;
    public static final boolean IS_SERVER = FMLCommonHandler.instance().getEffectiveSide().isServer();
    public static final boolean IS_CLIENT = FMLCommonHandler.instance().getEffectiveSide().isClient();

    static {
        final YamlConfiguration reader = new YamlConfiguration(Filesystem.CONFIG_SETTINGS_PATH.toFile());
        try {
            reader.load();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        IS_DEBUG = reader.getChild("debug").getBoolean(true);
    }
}
