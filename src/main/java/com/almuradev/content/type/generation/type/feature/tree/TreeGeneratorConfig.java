/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.tree;

public interface TreeGeneratorConfig {
    String BIG_TREE = "big_tree";
    String CHANCE = "chance";
    String REQUIRES = "requires";
    String TREE = "tree";
    String WORLDS = "worlds";

    interface BigTree {
        String CHANCE = "chance";
        String TYPE = "type";

        interface Chance {
            String BIOME = "biome";
        }
    }

    interface Chance {
        String BIOME = "biome";
    }
}
