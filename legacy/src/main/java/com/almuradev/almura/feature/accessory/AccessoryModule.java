/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import com.almuradev.almura.feature.accessory.client.ClientAccessoryManager;
import com.almuradev.almura.feature.accessory.network.ClientboundAccessoryGuiPacket;
import com.almuradev.almura.feature.accessory.network.ClientboundPlayerAccessoriesPacket;
import com.almuradev.almura.feature.accessory.network.ServerboundAccessoryGuiRequestPacket;
import com.almuradev.almura.feature.accessory.network.ServerboundAccessorySelectPacket;
import com.almuradev.almura.feature.accessory.network.handler.ClientboundAccessoryGuiPacketHandler;
import com.almuradev.almura.feature.accessory.network.handler.ClientboundPlayerAccessoriesPacketHandler;
import com.almuradev.almura.feature.accessory.network.handler.ServerboundAccessoryGuiRequestPacketHandler;
import com.almuradev.almura.feature.accessory.network.handler.ServerboundAccessorySelectPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class AccessoryModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ServerboundAccessoryGuiRequestPacket.class, binder -> binder.handler(ServerboundAccessoryGuiRequestPacketHandler.class,
                        Platform.Type.SERVER))
                .bind(ServerboundAccessorySelectPacket.class, binder -> binder.handler(ServerboundAccessorySelectPacketHandler.class,
                        Platform.Type.SERVER))
                .bind(ClientboundAccessoryGuiPacket.class, binder -> binder.handler(ClientboundAccessoryGuiPacketHandler.class,
                        Platform.Type.CLIENT))
                .bind(ClientboundPlayerAccessoriesPacket.class, binder -> binder.handler(ClientboundPlayerAccessoriesPacketHandler.class,
                        Platform.Type.CLIENT));

        this.facet().add(ServerAccessoryManager.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                protected void configure() {
                    this.facet().add(ClientAccessoryManager.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
