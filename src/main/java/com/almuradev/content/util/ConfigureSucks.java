/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.util;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.Collections;
import java.util.List;

// Screw you, configurate.
public final class ConfigureSucks {
    /*
     * HELLO, I'M CONFIGURATE AND I'M A POOP HEAD.
     */
    public static List<? extends ConfigurationNode> children(final ConfigurationNode config) {
        if (config.hasListChildren()) {
            return config.getChildrenList();
        }
        return Collections.singletonList(config);
    }
}
