/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod;

import com.flowpowered.cerealization.config.yaml.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;

public class Configuration {
    public static final boolean isDebug;

    static {
        try {
            Files.copy(Configuration.class.getResourceAsStream("config/settings.yml"), Filesystem.SETTINGS_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final YamlConfiguration reader = new YamlConfiguration(Filesystem.SETTINGS_PATH.toFile());
        isDebug = reader.getChild("debug").getBoolean(false);
    }
}
