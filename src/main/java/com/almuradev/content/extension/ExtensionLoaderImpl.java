/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.extension;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

@Singleton
public final class ExtensionLoaderImpl implements ExtensionLoader {
    private final Set<Path> extensions = new HashSet<>();

    @Override
    public void push(final Path path) {
        this.extensions.add(path);
    }
}
