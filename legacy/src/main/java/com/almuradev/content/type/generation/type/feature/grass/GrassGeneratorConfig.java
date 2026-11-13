/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.grass;

public interface GrassGeneratorConfig {

    String CHANCE = "chance";
    String REQUIRES = "requires";
    String WORLDS = "worlds";
    String GRASS = "grass";

    interface Chance {

        String BIOME = "biome";
    }
}
