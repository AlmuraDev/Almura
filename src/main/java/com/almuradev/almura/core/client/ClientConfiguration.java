/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.client;

import com.almuradev.almura.core.client.config.ClientCategory;
import com.almuradev.almura.core.client.config.DebugCategory;
import com.almuradev.almura.shared.config.MappedHoconConfiguration;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import com.google.inject.Provides;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.config.ConfigDir;

import java.nio.file.Path;

import javax.inject.Singleton;

@ConfigSerializable
@SideOnly(Side.CLIENT)
public class ClientConfiguration {

    @Setting public final ClientCategory client = new ClientCategory();
    @Setting public final DebugCategory debug = new DebugCategory();

    @SideOnly(Side.CLIENT)
    static final class Module extends AbstractModule {

        private static final String FILE_NAME = "client.conf";
        private static final String HEADER = "Almura configuration\n\nFor further assistance, join #almura on EsperNet.";

        @Override
        protected void configure() {
        }

        @Provides
        @Singleton
        MappedConfiguration<ClientConfiguration> configurationAdapter(@ConfigDir(sharedRoot = false) final Path root) {
            return new MappedHoconConfiguration<ClientConfiguration>(ClientConfiguration.class, root.resolve(FILE_NAME)) {
                @Override
                protected ConfigurationOptions createDefaultOptions() {
                    return super.createDefaultOptions().setHeader(HEADER);
                }
            };
        }
    }
}
