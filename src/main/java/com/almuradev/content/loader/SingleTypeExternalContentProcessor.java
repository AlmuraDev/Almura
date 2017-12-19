/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import ninja.leaping.configurate.ConfigurationNode;

public interface SingleTypeExternalContentProcessor<C extends CatalogedContent, B extends ContentBuilder<C>> {

    B processExternal(final String namespace, final ConfigurationNode config, final String id);
}
