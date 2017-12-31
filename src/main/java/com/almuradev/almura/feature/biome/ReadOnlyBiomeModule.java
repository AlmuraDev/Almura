/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.api.Platform;

import javax.inject.Singleton;

public final class ReadOnlyBiomeModule extends AbstractModule implements CommonBinder {
    @Override
    protected void configure() {
        this.on(Platform.Type.CLIENT, () -> this.facet().add(TerrainControlReadOnlyBiomeSource.ClientNotifier.class));
    }

    @Provides
    @Singleton
    ReadOnlyBiomeSource readOnlyBiomeSource(final Injector injector) {
        if (Loader.isModLoaded(TerrainControlReadOnlyBiomeSource.TERRAIN_CONTROL_ID)) {
            return injector.getInstance(TerrainControlReadOnlyBiomeSource.class);
        }
        return new VanillaReadOnlyBiomeSource();
    }
}
