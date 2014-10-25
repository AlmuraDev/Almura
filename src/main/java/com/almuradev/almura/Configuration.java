/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.flowpowered.cerealization.config.yaml.YamlConfiguration;

public class Configuration {

    public static final boolean IS_DEBUG;

    static {
        final YamlConfiguration reader = new YamlConfiguration(Filesystem.SETTINGS_PATH.toFile());
        IS_DEBUG = reader.getChild("debug").getBoolean(false);
    }
}
