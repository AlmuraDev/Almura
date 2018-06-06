/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick;

import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIPlayerListPanel;
import com.almuradev.almura.feature.nick.client.gui.NicknameGUI;
import com.almuradev.almura.feature.nick.network.*;
import com.almuradev.almura.feature.nick.network.handler.*;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class NickModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ClientboundNucleusNameChangeMappingPacket.class, binder -> binder.handler(ClientboundNucleusNameChangeMappingPacketHandler.class, Platform.Type.CLIENT))
                .bind(ClientboundNucleusNameMappingsPacket.class, binder -> binder.handler(ClientboundNucleusNameMappingsPacketHandler.class, Platform.Type.CLIENT))
                .bind(ServerboundNicknameOpenRequestPacket.class, binder -> binder.handler(ServerboundNicknameOpenRequestPacketHandler.class, Platform.Type.SERVER))
                .bind(ClientboundNicknameOpenResponsePacket.class, binder -> binder.handler(ClientboundNicknameOpenResponsePacketHandler.class, Platform.Type.CLIENT))
                .bind(ServerboundNucleusNameChangePacket.class, binder -> binder.handler(ServerboundNucleusNameChangePacketHandler.class, Platform.Type.SERVER));
        this.facet().add(ServerNickManager.class);
        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                protected void configure() {
                    this.facet().add(ClientNickManager.class);
                    this.requestStaticInjection(UIPlayerListPanel.class);
                    this.requestStaticInjection(NicknameGUI.class);
                    this.requestStaticInjection(NicknameGUI.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
