/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.almanac.network;

import com.almuradev.almura.feature.complex.item.almanac.gui.FarmersAlmanacCropList;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundCropListOpenPacketHandler implements MessageHandler<ClientboundCropListOpenPacket> {

    @Override
    public void handleMessage(final ClientboundCropListOpenPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient()) {
            new FarmersAlmanacCropList(message).display();
        }
    }
}
