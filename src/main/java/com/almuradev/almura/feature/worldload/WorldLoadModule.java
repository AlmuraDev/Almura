/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload;

import com.almuradev.almura.feature.worldload.client.ClientWorldLoadManager;
import com.almuradev.almura.feature.worldload.network.ClientboundWorldLoadPacket;
import com.almuradev.almura.feature.worldload.network.handler.ClientboundWorldLoadPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class WorldLoadModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet().bind(ClientboundWorldLoadPacket.class, binder -> binder.handler(ClientboundWorldLoadPacketHandler.class, Platform.Type.CLIENT));

        this.facet().add(ServerWorldLoadManager.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                protected void configure() {
                    this.facet().add(ClientWorldLoadManager.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
