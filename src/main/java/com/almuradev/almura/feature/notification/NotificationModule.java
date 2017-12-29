/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification;

import com.almuradev.almura.feature.notification.network.ClientboundPlayerNotificationPacket;
import com.almuradev.almura.feature.notification.network.ClientboundPlayerNotificationPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class NotificationModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.command().child(NotificationCommands.generateNotificationCommand(), "notify");
        this.packet().bind(ClientboundPlayerNotificationPacket.class, binder -> binder.handler(ClientboundPlayerNotificationPacketHandler.class, Platform.Type.CLIENT));
        this.requestStaticInjection(NotificationCommands.class);
        this.facet().add(ServerNotificationManager.class);
        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                protected void configure() {
                    this.facet().add(ClientNotificationManager.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
