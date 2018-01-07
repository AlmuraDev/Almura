/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.ore;

public interface OreGenerationConfig {
    String ENTRIES = "entries";
    String WEIGHT = "weight";

    interface Definition {
        String BLOCK = "block";
        String COUNT = "count";
        String SIZE = "size";
    }
}
