/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation;

import com.almuradev.content.loader.MultiTypeContentLoader;
import com.almuradev.core.event.Witness;
import com.almuradev.core.event.Witnesses;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class GenerationContentTypeLoader extends MultiTypeContentLoader<GenerationGenre, ContentGenerator, ContentGenerator.Builder<ContentGenerator>, GenerationContentProcessor<ContentGenerator, ContentGenerator.Builder<ContentGenerator>>> implements Witness {
    private final Witnesses witnesses;

    @Inject
    private GenerationContentTypeLoader(final Witnesses witnesses) {
        this.witnesses = witnesses;
    }

    @Listener
    public void init(final GameInitializationEvent event) {
        this.build();

        this.entries.values().forEach(entry -> {
            final ContentGenerator generator = entry.value;
            if (generator instanceof IWorldGenerator && generator instanceof ContentGenerator.Weighted) {
                GameRegistry.registerWorldGenerator((IWorldGenerator) generator, ((ContentGenerator.Weighted) generator).weight());
            }
            if (generator instanceof Witness) {
                this.witnesses.register((Witness) generator);
            }
        });
    }
}
