/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.asset;

import com.almuradev.almura.content.loader.AssetPipeline;
import com.almuradev.almura.content.loader.StageTask;
import com.google.inject.Injector;
import net.kyori.membrane.facet.Enableable;

import java.util.Set;

import javax.inject.Inject;

/**
 * A facet that installs asset factory entries into the asset pipeline.
 */
public final class AssetFactoryInstaller implements Enableable {

    private final Injector injector;
    private final AssetPipeline pipeline;
    private final Set<AssetFactoryBinder.Entry<? extends StageTask<?, ?>>> builders;

    @Inject
    public AssetFactoryInstaller(final Injector injector, final AssetPipeline pipeline, final Set<AssetFactoryBinder.Entry<? extends StageTask<?, ?>>> builders) {
        this.injector = injector;
        this.pipeline = pipeline;
        this.builders = builders;
    }

    @Override
    public void enable() {
        this.builders.forEach(builder -> builder.install(this.pipeline, this.injector));
    }

    @Override
    public void disable() {
        // cannot remove from the asset pipeline
    }
}
