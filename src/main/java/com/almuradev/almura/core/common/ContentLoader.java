/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.common;

import com.almuradev.content.ContentConfig;
import com.almuradev.content.loader.RootContentLoader;
import net.kyori.membrane.facet.Enableable;

import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Named;

public final class ContentLoader implements Enableable {

    private final Path assets;
    private final RootContentLoader loader;

    @Inject
    private ContentLoader(@Named("assets") final Path assets, final RootContentLoader loader) {
        this.assets = assets;
        this.loader = loader;
    }

    @Override
    public void enable() {
        this.loader.searchJars(this.assets);
        this.loader.searchFileSystem(this.assets.resolve(RootContentLoader.FILESYSTEM_ASSETS).resolve(ContentConfig.ASSETS_DIRECTORY));
        this.loader.load();
    }

    @Override
    public void disable() {
    }
}
