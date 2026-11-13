/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.almanac.network;

import com.almuradev.almura.feature.complex.item.almanac.gui.IngameFarmersAlmanac;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public class ClientboundWorldPositionInformationPacketHandler implements MessageHandler<ClientboundWorldPositionInformationPacket> {

    @Override
    public void handleMessage(ClientboundWorldPositionInformationPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            new IngameFarmersAlmanac(message).display();
        }
    }
}
