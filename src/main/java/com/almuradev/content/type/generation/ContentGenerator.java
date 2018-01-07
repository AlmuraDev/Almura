/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;

public interface ContentGenerator extends CatalogedContent {
    int weight();

    interface Builder<C extends ContentGenerator> extends ContentBuilder<C> {
    }
}
