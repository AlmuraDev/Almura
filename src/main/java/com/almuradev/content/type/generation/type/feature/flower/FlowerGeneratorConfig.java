/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.flower;

public interface FlowerGeneratorConfig {

    String CHANCE = "chance";
    String REQUIRES = "requires";
    String WORLDS = "worlds";
    String FLOWER = "flower";

    interface Chance {

        String BIOME = "biome";
    }
}
