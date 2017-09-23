/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.almuradev.almura.content.loader.AssetLoader;
import com.almuradev.almura.content.loader.AssetPipeline;
import com.almuradev.almura.content.loader.AssetRegistry;
import com.almuradev.almura.content.loader.LoaderPhase;
import com.google.inject.Injector;
import net.kyori.membrane.facet.internal.Facets;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

import java.io.IOException;

import javax.inject.Inject;

/**
 * The common platform of Almura. All code meant to run on the client and both dedicated and integrated server go here.
 */
public abstract class CommonProxy {

    private Injector injector;
    @Inject protected AssetRegistry assetRegistry;
    @Inject protected AssetPipeline assetPipeline;
    @Inject protected AssetLoader assetLoader;

    protected final void construct(final Injector injector) {
        this.injector = this.createInjector(injector);
        this.injector.getMembersInjector(CommonProxy.class).injectMembers(this); // HACK
        this.injector.getInstance(Facets.class).enable();
    }

    protected final void destruct() {
        this.injector.getInstance(Facets.class).disable();
    }

    protected abstract Injector createInjector(final Injector parent);

    protected void onGameConstruction(GameConstructionEvent event) {
        this.loadConfig();

        try {
            this.assetRegistry.loadAssetFiles(Constants.FileSystem.PATH_ASSETS_ALMURA_30_PACKS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.assetRegistry.loadAssetContextuals();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.assetPipeline.process(LoaderPhase.CONSTRUCTION, this.assetRegistry);
        this.assetLoader.registerSpongeOnlyCatalogTypes();
    }

    protected void onGamePreInitialization(GamePreInitializationEvent event) {
    }

    protected void loadConfig() {
    }
}
