/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.almuradev.content.ContentType;
import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import ninja.leaping.configurate.ConfigurationNode;

public interface MultiTypeExternalContentProcessor<T extends ContentType.MultiType<C, B>, C extends CatalogedContent, B extends ContentBuilder<C>> {
    default <X extends C, Y extends ContentBuilder<X>> Y processExternal(final String namespace, final T type, final ConfigurationNode config, final String id) {
        return (Y) this.processExternal0(namespace, type, config, id);
    }

    /** @deprecated internal hack */
    @Deprecated
    <Y extends B> Y processExternal0(final String namespace, final T type, final ConfigurationNode config, final String id);
}
