/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick;

import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacketHandler;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class NickModule extends AbstractModule implements ClientBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ClientboundNucleusNameChangeMappingPacket.class, binder -> {
                    binder.channel(3);
                    binder.handler(ClientboundNucleusNameChangeMappingPacketHandler.class, Platform.Type.CLIENT);
                })
                .bind(ClientboundNucleusNameMappingsPacket.class, binder -> {
                    binder.channel(4);
                    binder.handler(ClientboundNucleusNameMappingsPacketHandler.class, Platform.Type.CLIENT);
                });

        this.facet()
                .add(NickManager.class);
    }
}
