/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.almuradev.core.event.Witness;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;

import javax.inject.Singleton;

@Singleton
public final class ServerTranslationInjector extends TranslationInjector implements Witness {
    @Override
    void inject() {
        this.inject(this.manager.get(TranslationManager.DEFAULT_TRANSLATION_ID));
    }

    @Listener
    public void started(final GameStartedServerEvent event) {
        this.inject();
    }
}
