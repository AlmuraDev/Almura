/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdecay;

public interface BlockDecayActionConfig {
    String CHANCE = "chance";

    String DROP = "drop";

    interface Chance {
        String BIOME = "biome";
    }
}
