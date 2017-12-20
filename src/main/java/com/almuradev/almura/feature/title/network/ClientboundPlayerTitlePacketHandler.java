package com.almuradev.almura.feature.title.network;

import com.almuradev.almura.feature.title.TitleManager;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.inject.Inject;

public final class ClientboundPlayerTitlePacketHandler implements MessageHandler<ClientboundPlayerTitlePacket> {

    private final TitleManager manager;

    @Inject
    public ClientboundPlayerTitlePacketHandler(final TitleManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleMessage(ClientboundPlayerTitlePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            if (message.add) {
                this.manager.putSelectedTitle(message.uniqueId, TextSerializers.LEGACY_FORMATTING_CODE.serialize(message.title));
            } else {
                this.manager.removeSelectedTitle(message.uniqueId);
            }
        }
    }
}
