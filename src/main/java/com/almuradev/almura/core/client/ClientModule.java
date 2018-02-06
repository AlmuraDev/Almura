/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.asm.StaticAccess;
import com.almuradev.almura.core.common.CommonModule;
import com.almuradev.almura.feature.menu.MainMenuModule;
import com.almuradev.almura.feature.speed.ClientOptimizationModule;
import com.almuradev.almura.shared.client.keyboard.binder.KeyBindingInstaller;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.content.model.obj.OBJModelLoader;
import com.google.inject.Inject;
import com.google.inject.Provides;
import net.kyori.violet.AbstractModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

/**
 * The root module for the client.
 */
@SideOnly(Side.CLIENT)
public final class ClientModule extends AbstractModule implements ClientBinder {

    @Override
    protected void configure() {
        this.install(new CommonModule());
        this.install(new VanillaModule());
        this.install(new MainMenuModule());
        this.install(new ClientOptimizationModule());
        this.install(new ClientConfiguration.Module());
        this.facet().add(KeyBindingInstaller.class);
        this.model().loader(OBJModelLoader.class, binder -> binder.domains(Almura.ID));
        this.requestStaticInjection(StaticAccess.class);
    }

    private static class VanillaModule extends AbstractModule {
        @Override
        protected void configure() {
            this.requestInjection(this);
        }

        @Provides
        Minecraft client() {
            return Minecraft.getMinecraft();
        }

        @Provides
        LanguageManager languageManager(final Minecraft client) {
            return client.getLanguageManager();
        }

        @Provides
        IResourceManager resourceManager(final Minecraft client) {
            return client.getResourceManager();
        }

        @Inject
        private void configureListeners(final IResourceManager manager, final Set<IResourceManagerReloadListener> listeners) {
            for (final IResourceManagerReloadListener listener : listeners) {
                ((SimpleReloadableResourceManager) manager).registerReloadListener(listener);
            }
        }
    }
}
