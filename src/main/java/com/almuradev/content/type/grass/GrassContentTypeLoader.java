/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.grass;

import com.almuradev.almura.shared.event.Witness;
import com.almuradev.content.loader.SingleTypeContentLoader;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class GrassContentTypeLoader extends SingleTypeContentLoader<Grass, Grass.Builder> implements Witness {

    private final GrassRegistryModule module;

    @Inject
    private GrassContentTypeLoader(final GrassRegistryModule module) {
        this.module = module;
    }

    @Listener
    public void init(final GameInitializationEvent event) {
        this.build();

        for (final Entry<Grass, Grass.Builder> entry : this.entries.values()) {
            this.module.registerAdditionalCatalog(entry.value);
        }
    }
}
