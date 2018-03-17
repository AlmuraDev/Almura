/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.component.ComponentModule;
import com.almuradev.content.loader.ClientTranslationInjector;
import com.almuradev.content.loader.RootContentLoader;
import com.almuradev.content.loader.ServerTranslationInjector;
import com.almuradev.content.loader.TranslationInjector;
import com.almuradev.content.model.ModelBinder;
import com.almuradev.content.model.obj.OBJModelParser;
import com.almuradev.content.type.TypeModule;
import net.kyori.violet.AbstractModule;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import org.spongepowered.api.Platform;

public final class ContentModule extends AbstractModule implements CommonBinder {
    @Override
    protected void configure() {
        this.facet().add(RootContentLoader.class);

        this.install(new ModelModule());
        this.install(new TranslationModule());

        this.install(new ComponentModule());
        this.install(new TypeModule());
    }

    private static final class ModelModule extends AbstractModule implements CommonBinder {
        @Override
        protected void configure() {
            this.on(Platform.Type.CLIENT, () -> {
                this.facet().add(ModelBinder.Installer.class);
                this.installFactory(OBJModelParser.Factory.class);
            });
        }
    }

    private static final class TranslationModule extends AbstractModule implements CommonBinder {
        @Override
        protected void configure() {
            this.facet().add(ServerTranslationInjector.class);
            this.inSet(TranslationInjector.class).addBinding().to(ServerTranslationInjector.class);
            this.on(Platform.Type.CLIENT, () -> {
                this.inSet(IResourceManagerReloadListener.class).addBinding().to(ClientTranslationInjector.class);
                this.inSet(TranslationInjector.class).addBinding().to(ClientTranslationInjector.class);
            });
        }
    }
}
