/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death;

import com.almuradev.almura.feature.death.client.gui.PlayerDiedGUI;
import com.almuradev.almura.feature.death.network.ClientboundPlayerDiedPacket;
import com.almuradev.almura.feature.death.network.ServerboundReviveRequestPacket;
import com.almuradev.almura.feature.death.network.handler.ClientboundPlayerDiedPacketHandler;
import com.almuradev.almura.feature.death.network.handler.ServerboundReviveRequestPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class DeathModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ServerboundReviveRequestPacket.class, binder -> binder.handler(ServerboundReviveRequestPacketHandler.class, Platform.Type.SERVER));
        this.packet()
                .bind(ClientboundPlayerDiedPacket.class, binder -> binder.handler(ClientboundPlayerDiedPacketHandler.class, Platform.Type.CLIENT));

        this.facet().add(DeathHandler.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
                protected void configure() {
                    this.requestStaticInjection(PlayerDiedGUI.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
