/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation;

import com.almuradev.almura.shared.event.Witness;
import com.almuradev.content.loader.MultiTypeContentLoader;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;

import javax.inject.Singleton;

@Singleton
public final class GenerationContentTypeLoader extends MultiTypeContentLoader<GenerationGenre, ContentGenerator, ContentGenerator.Builder<ContentGenerator>, GenerationContentProcessor<ContentGenerator, ContentGenerator.Builder<ContentGenerator>>> implements Witness {
    @Listener
    public void init(final GameInitializationEvent event) {
        this.build();

        this.entries.values().forEach(entry -> {
            if (entry.value instanceof IWorldGenerator) {
                GameRegistry.registerWorldGenerator((IWorldGenerator) entry.value, entry.value.weight());
            }
        });
    }
}
