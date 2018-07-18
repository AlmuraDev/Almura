/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.core.event;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.slf4j.Logger;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.PluginContainer;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class Witnesses {
    @Inject private PluginContainer plugin;
    @Inject private EventManager em;
    @Inject private Logger logger;

    public void register(final Witness witness) {
        final Witness.Scope[] scopes = witness.getClass().getAnnotationsByType(Witness.Scope.class);

        if (classHas(scopes, Witness.Scope.Type.NORMAL)) {
            if (anyMethodHas(witness.getClass(), SubscribeEvent.class)) {
                this.giveSomeInformation(witness, Witness.Scope.Type.NORMAL, MinecraftForge.EVENT_BUS);
                MinecraftForge.EVENT_BUS.register(witness);
            }

            if (anyMethodHas(witness.getClass(), Listener.class)) {
                this.giveSomeInformation(witness, Witness.Scope.Type.NORMAL, this.em);
                this.em.registerListeners(this.plugin, witness);
            }
        }

        if (classHas(scopes, Witness.Scope.Type.TERRAIN_GEN)) {
            if (anyMethodHas(witness.getClass(), SubscribeEvent.class)) {
                this.giveSomeInformation(witness, Witness.Scope.Type.NORMAL, MinecraftForge.TERRAIN_GEN_BUS);
                MinecraftForge.TERRAIN_GEN_BUS.register(witness);
            }
        }
    }

    private void giveSomeInformation(final Witness witness, final Witness.Scope.Type scope, final Object bus) {
        this.logger.debug("Registering witness '{}' with scope '{}' in bus '{}'", witness, scope, bus);
    }

    private static boolean classHas(final Witness.Scope[] scopes, final Witness.Scope.Type type) {
        if (scopes.length == 0 && type == Witness.Scope.Type.NORMAL) {
            return true;
        }
        for (final Witness.Scope scope : scopes) {
            if (scope.value() == type) {
                return true;
            }
        }
        return false;
    }

    private static boolean anyMethodHas(final Class<?> klass, final Class<? extends Annotation> annotation) {
        return Stream.of(klass.getDeclaredMethods()).anyMatch(method -> method.getAnnotation(annotation) != null);
    }
}
