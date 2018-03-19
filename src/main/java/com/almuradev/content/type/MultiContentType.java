/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker interface for a content type with multiple children types.
 */
public interface MultiContentType<C extends CatalogedContent, B extends ContentBuilder<C>> {
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
