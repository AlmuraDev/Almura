/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type;

import com.almuradev.content.loader.ContentLoader;
import com.google.inject.Injector;

/**
 * A content type.
 */
public interface ContentType {
    /**
     * The id of this content type.
     *
     * <p>The id is used for identification and loading.</p>
     */
    String id();

    /**
     * The loader for this content type.
     */
    Class<? extends ContentLoader> loader();

    default ContentLoader loader(final Injector injector) {
        return injector.getInstance(this.loader());
    }

    class Impl implements ContentType {
        private final String id;
        private final Class<? extends ContentLoader> loader;

        public Impl(final String id, final Class<? extends ContentLoader> loader) {
            this.id = id;
            this.loader = loader;
        }

        @Override
        public String id() {
            return this.id;
        }

        @Override
        public Class<? extends ContentLoader> loader() {
            return this.loader;
        }
    }
}
