/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick;

import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIPlayerListPanel;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacketHandler;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacketHandler;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class NickModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ClientboundNucleusNameChangeMappingPacket.class, binder -> binder.handler(ClientboundNucleusNameChangeMappingPacketHandler.class, Platform.Type.CLIENT))
                .bind(ClientboundNucleusNameMappingsPacket.class, binder -> binder.handler(ClientboundNucleusNameMappingsPacketHandler.class, Platform.Type.CLIENT));
        this.facet()
                .add(ServerNickManager.class);
        this.on(Platform.Type.CLIENT, () -> this.requestStaticInjection(UIPlayerListPanel.class));
    }
}
