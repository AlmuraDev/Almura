/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import com.google.inject.Injector;
import com.google.inject.Provides;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.common.SpongeImpl;

import javax.inject.Singleton;

public final class ReadOnlyBiomeModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @SideOnly(Side.CLIENT)
    @Singleton
    ReadOnlyBiomeSource readOnlyBiomeSource(final Injector injector) {
        if (Loader.isModLoaded("terraincontrol")) {
            SpongeImpl.getLogger().warn("SpTcl: terraincontrol biome source used");
            return injector.getInstance(TerrainControlReadOnlyBiomeSource.class);
        }
        SpongeImpl.getLogger().warn("SpTcl: terraincontrol not loaded? " + Loader.instance().getIndexedModList().keySet());
        return new VanillaReadOnlyBiomeSource();
    }
}
