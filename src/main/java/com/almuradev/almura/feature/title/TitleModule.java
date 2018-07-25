/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title;

import com.almuradev.almura.feature.title.client.gui.ManageTitlesGUI;
import com.almuradev.almura.feature.title.client.gui.SelectTitleGUI;
import com.almuradev.almura.feature.title.network.ClientboundAvailableTitlesResponsePacket;
import com.almuradev.almura.feature.title.network.ClientboundSelectedTitlePacket;
import com.almuradev.almura.feature.title.network.ClientboundSelectedTitleBulkPacket;
import com.almuradev.almura.feature.title.network.ClientboundTitleGuiResponsePacket;
import com.almuradev.almura.feature.title.network.ClientboundTitlesRegistryPacket;
import com.almuradev.almura.feature.title.network.ServerboundModifyTitlePacket;
import com.almuradev.almura.feature.title.network.ServerboundSelectedTitlePacket;
import com.almuradev.almura.feature.title.network.ServerboundAvailableTitlesRequestPacket;
import com.almuradev.almura.feature.title.network.ServerboundTitleGuiRequestPacket;
import com.almuradev.almura.feature.title.network.handler.ClientboundAvailableTitlesResponsePacketHandler;
import com.almuradev.almura.feature.title.network.handler.ClientboundSelectedTitlePacketHandler;
import com.almuradev.almura.feature.title.network.handler.ClientboundSelectedTitleBulkPacketHandler;
import com.almuradev.almura.feature.title.network.handler.ClientboundTitleGuiResponsePacketHandler;
import com.almuradev.almura.feature.title.network.handler.ClientboundTitlesRegistryPacketHandler;
import com.almuradev.almura.feature.title.network.handler.ServerboundAvailableTitlesRequestPacketHandler;
import com.almuradev.almura.feature.title.network.handler.ServerboundModifyTitlePacketHandler;
import com.almuradev.almura.feature.title.network.handler.ServerboundSelectedTitlePacketHandler;
import com.almuradev.almura.feature.title.network.handler.ServerboundTitleGuiRequestPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.api.Platform;

public final class TitleModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
            .bind(ClientboundTitlesRegistryPacket.class,
                binder -> binder.handler(ClientboundTitlesRegistryPacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundTitleGuiRequestPacket.class,
                binder -> binder.handler(ServerboundTitleGuiRequestPacketHandler.class, Platform.Type.SERVER))

            .bind(ClientboundTitleGuiResponsePacket.class,
                binder -> binder.handler(ClientboundTitleGuiResponsePacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundModifyTitlePacket.class,
                binder -> binder.handler(ServerboundModifyTitlePacketHandler.class, Platform.Type.SERVER))

            .bind(ClientboundSelectedTitlePacket.class,
                binder -> binder.handler(ClientboundSelectedTitlePacketHandler.class, Platform.Type.CLIENT))

            .bind(ClientboundSelectedTitleBulkPacket.class,
                binder -> binder.handler(ClientboundSelectedTitleBulkPacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundAvailableTitlesRequestPacket.class,
                binder -> binder.handler(ServerboundAvailableTitlesRequestPacketHandler.class, Platform.Type.SERVER))

            .bind(ClientboundAvailableTitlesResponsePacket.class,
                binder -> binder.handler(ClientboundAvailableTitlesResponsePacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundSelectedTitlePacket.class,
                binder -> binder.handler(ServerboundSelectedTitlePacketHandler.class, Platform.Type.SERVER));

        this.facet().add(ServerTitleManager.class);
        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {

                @Override
                @SuppressWarnings("UnnecessaryStaticInjection")
                protected void configure() {
                    this.facet().add(ClientTitleManager.class);
                    this.requestStaticInjection(RenderPlayer.class);
                    this.requestStaticInjection(SelectTitleGUI.class);
                    this.requestStaticInjection(ManageTitlesGUI.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
