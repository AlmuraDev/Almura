/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling;

public interface SaplingBlockConfig {
    String BIG_TREE = "big_tree";
    String TREE = "tree";

    interface BigTree {
        String CHANCE = "chance";
        String TYPE = "type";

        interface Chance {
            String BIOME = "biome";
        }
    }
}
