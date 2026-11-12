/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.handbook.network;

import com.almuradev.almura.feature.complex.item.handbook.gui.SkillsHandbookScreen;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundSkillsHandbookPacketHandler implements MessageHandler<ClientboundSkillsHandbookPacket> {

    @Override
    public void handleMessage(final ClientboundSkillsHandbookPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient()) {
            new SkillsHandbookScreen(message).display();
        }
    }
}
