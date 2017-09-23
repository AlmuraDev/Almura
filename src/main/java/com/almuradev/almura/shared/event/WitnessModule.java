/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.event;

import net.kyori.membrane.facet.internal.Facets;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStateEvent;
import org.spongepowered.api.plugin.PluginContainer;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import javax.inject.Inject;

public final class WitnessModule extends AbstractModule {

    @Inject private PluginContainer plugin;
    @Inject private EventManager em;
    @Inject private Facets facets;

    @Override
    protected void configure() {
        this.bind(EventBus.class).toInstance(MinecraftForge.EVENT_BUS);
        this.requestInjection(this);
    }

    @Inject
    private void earlyConfigure() {
        this.em.registerListeners(this.plugin, this);
        this.register(this.facets.of(Witness.class, Witness.predicate()));
    }

    @Listener
    public void lifecycleConfigure(final GameStateEvent event) {
        this.register(this.facets.of(Witness.Lifecycle.class, Witness.Lifecycle.predicate(event.getState())));
    }

    private void register(final Stream<? extends Witness> stream) {
        stream.forEach(witness -> {
            if (witness instanceof Witness.Impl) {
                ((Witness.Impl) witness).subscribed = true;
            }

            if (anyMethodHas(witness.getClass(), SubscribeEvent.class)) {
                MinecraftForge.EVENT_BUS.register(witness);
            }

            if (anyMethodHas(witness.getClass(), Listener.class)) {
                this.em.registerListeners(this.plugin, witness);
            }
        });
    }

    private static boolean anyMethodHas(final Class<?> klass, final Class<? extends Annotation> annotation) {
        return Stream.of(klass.getDeclaredMethods()).anyMatch(method -> method.getAnnotation(annotation) != null);
    }
}
