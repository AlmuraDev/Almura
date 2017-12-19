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

import java.util.Optional;

public interface ContentFinder<C extends CatalogedContent, B extends ContentBuilder<C>> {

    Optional<B> findBuilder(final String id);
}
