/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;

public interface ActionContentType extends CatalogedContent {
    interface Builder<C extends ActionContentType> extends ContentBuilder<C> {
    }
}
