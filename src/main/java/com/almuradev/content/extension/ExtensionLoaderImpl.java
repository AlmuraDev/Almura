/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.extension;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.nio.file.Path;

public class ExtensionLoaderImpl implements ExtensionLoader {
    private final Multimap<Path, Extension> extensions = HashMultimap.create();

    @Override
    public Push push(final Path path) {
        return new Push() {
            @Override
            public <E extends Extension> Push with(final E extension) {
                ExtensionLoaderImpl.this.extensions.put(path, extension);
                return this;
            }
        };
    }
}
