/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import com.almuradev.almura.feature.biome.network.ClientboundBiomeChunkDataPacket;
import com.almuradev.almura.feature.biome.network.ClientboundBiomeChunkDataPacketHandler;
import com.almuradev.almura.feature.biome.network.ClientboundBiomeInformationPacket;
import com.almuradev.almura.feature.biome.network.ClientboundBiomeInformationPacketHandler;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.debug.InformationDebugPanel;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.Platform;

public final class BiomeModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ClientboundBiomeInformationPacket.class, binder -> binder.handler(ClientboundBiomeInformationPacketHandler.class,
                        Platform.Type.CLIENT))
                .bind(ClientboundBiomeChunkDataPacket.class, binder -> binder.handler(ClientboundBiomeChunkDataPacketHandler.class,
                        Platform.Type.CLIENT));
        this.facet().add(BiomeServerFeature.class);
        this.requestStaticInjection(PlayerChunkMapEntry.class);
        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {

                @Override
                protected void configure() {
                    this.facet().add(BiomeClientFeature.class);
                    this.requestStaticInjection(Biome.class);
                    this.requestStaticInjection(BiomeUtil.class);
                    this.requestStaticInjection(InformationDebugPanel.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}