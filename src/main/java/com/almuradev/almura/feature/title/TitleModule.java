/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title;

import com.almuradev.almura.feature.title.client.gui.ManageTitlesGUI;
import com.almuradev.almura.feature.title.client.gui.TitleGUI;
import com.almuradev.almura.feature.title.network.ClientboundPlayerSelectedTitlePacket;
import com.almuradev.almura.feature.title.network.ClientboundPlayerSelectedTitlesPacket;
import com.almuradev.almura.feature.title.network.ClientboundPlayerTitlesResponsePacket;
import com.almuradev.almura.feature.title.network.ServerboundPlayerSetTitlePacket;
import com.almuradev.almura.feature.title.network.ServerboundPlayerTitlesRequestPacket;
import com.almuradev.almura.feature.title.network.handler.ClientboundPlayerSelectedTitlePacketHandler;
import com.almuradev.almura.feature.title.network.handler.ClientboundPlayerSelectedTitlesPacketHandler;
import com.almuradev.almura.feature.title.network.handler.ClientboundPlayerTitlesResponsePacketHandler;
import com.almuradev.almura.feature.title.network.handler.ServerboundPlayerSetTitlePacketHandler;
import com.almuradev.almura.feature.title.network.handler.ServerboundPlayerTitlesRequestPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.api.Platform;

public final class TitleModule extends AbstractModule implements CommonBinder {

    @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
    @Override
    protected void configure() {
        this.command().child(TitleCommands.generateTitleCommand(), "title");
        this.packet()
                .bind(ClientboundPlayerSelectedTitlePacket.class, binder -> binder.handler(ClientboundPlayerSelectedTitlePacketHandler.class, Platform.Type.CLIENT))
                .bind(ClientboundPlayerSelectedTitlesPacket.class, binder -> binder.handler(ClientboundPlayerSelectedTitlesPacketHandler.class, Platform.Type.CLIENT))
                .bind(ServerboundPlayerSetTitlePacket.class, binder -> binder.handler(ServerboundPlayerSetTitlePacketHandler.class, Platform.Type.SERVER))
                .bind(ServerboundPlayerTitlesRequestPacket.class, binder -> binder.handler(ServerboundPlayerTitlesRequestPacketHandler.class, Platform.Type.SERVER))
                .bind(ClientboundPlayerTitlesResponsePacket.class, binder -> binder.handler(ClientboundPlayerTitlesResponsePacketHandler.class, Platform.Type.CLIENT));
        this.facet().add(ServerTitleManager.class);
        this.requestStaticInjection(TitleCommands.class);
        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
                protected void configure() {
                    this.facet().add(ClientTitleManager.class);
                    this.requestStaticInjection(RenderPlayer.class);
                    this.requestStaticInjection(TitleGUI.class);
                    this.requestStaticInjection(ManageTitlesGUI.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
