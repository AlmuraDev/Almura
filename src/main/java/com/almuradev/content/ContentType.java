/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content;

import com.almuradev.content.loader.ContentLoader;
import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.google.inject.Injector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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

    /**
     * A marker interface for a content type with multiple children types.
     */
    interface MultiType<C extends CatalogedContent, B extends ContentBuilder<C>> {
        /**
         * Gets the id of this type.
         *
         * @return the id
         */
        String id();

        // cannot be name(), enum controls that method
        /**
         * Gets the friendly name of this type.
         *
         * @return the friendly name
         */
        default String friendlyName() {
            return this.id();
        }

        /**
         * Gets the builder class of this type.
         *
         * @return the builder class
         */
        Class<B> builder();

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        @interface Name {
            String value();
        }
    }
}
