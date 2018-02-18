/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network;

import com.almuradev.almura.feature.title.ClientTitleManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.inject.Inject;

@SuppressWarnings("deprecation")
public final class ClientboundPlayerSelectedTitlePacketHandler implements MessageHandler<ClientboundPlayerSelectedTitlePacket> {

    private final ClientTitleManager manager;

    @Inject
    public ClientboundPlayerSelectedTitlePacketHandler(final ClientTitleManager manager) {
        this.manager = manager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundPlayerSelectedTitlePacket message, RemoteConnection connection, Platform.Type side) {
        if (message.add) {
            this.manager.putSelectedTitle(message.uniqueId, TextSerializers.LEGACY_FORMATTING_CODE.serialize(message.title));
        } else {
            this.manager.removeSelectedTitle(message.uniqueId);
        }
    }
}
