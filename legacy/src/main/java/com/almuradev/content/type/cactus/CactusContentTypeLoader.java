/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.cactus;

import com.almuradev.content.loader.SingleTypeContentLoader;
import com.almuradev.core.event.Witness;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class CactusContentTypeLoader extends SingleTypeContentLoader<Cactus, Cactus.Builder> implements Witness {
    private final CactusRegistryModule module;

    @Inject
    private CactusContentTypeLoader(final CactusRegistryModule module) {
        this.module = module;
    }

    @Listener
    public void init(final GameInitializationEvent event) {
        this.build();

        for (final Entry<Cactus, Cactus.Builder> entry : this.entries.values()) {
            this.module.registerAdditionalCatalog(entry.value);
        }
    }
}
