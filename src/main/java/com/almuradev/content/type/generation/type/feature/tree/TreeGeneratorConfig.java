/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.tree;

public interface TreeGeneratorConfig {
    String CHANCE = "chance";
    String REQUIRES = "requires";
    String TREE = "tree";
    String WORLDS = "worlds";

    interface Chance {
        String BIOME = "biome";
    }
}
