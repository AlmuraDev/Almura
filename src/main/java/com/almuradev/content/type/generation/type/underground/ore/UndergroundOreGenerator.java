/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.underground.ore;

import com.almuradev.content.type.generation.ContentGenerator;
import com.almuradev.content.type.generation.type.underground.UndergroundGenerator;

public interface UndergroundOreGenerator extends ContentGenerator.Weighted, UndergroundGenerator {
    interface Builder extends ContentGenerator.Builder<UndergroundOreGenerator> {
        void push(final UndergroundOreDefinition definition);
    }
}
