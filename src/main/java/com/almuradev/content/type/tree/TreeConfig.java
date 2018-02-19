/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.tree;

public interface TreeConfig {
    String FRUIT = "fruit";
    String HANGING = "hanging";
    String HEIGHT = "height";
    String LEAVES = "leaves";
    String LOG = "log";

    interface Block {
        String BLOCK = "block";
        String CHANCE = "chance";

        interface Chance {
            String BIOME = "biome";
        }
    }

    interface Height {
        String BIOME = "biome";
        String MIN = "min";
        String VARIANCE = "variance";
    }
}
