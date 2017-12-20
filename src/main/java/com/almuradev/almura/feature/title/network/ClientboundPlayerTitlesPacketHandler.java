package com.almuradev.almura.feature.title.network;

import com.almuradev.almura.feature.title.TitleManager;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

public final class ClientboundPlayerTitlesPacketHandler implements MessageHandler<ClientboundPlayerTitlesPacket> {

    private final TitleManager manager;

    @Inject
    public ClientboundPlayerTitlesPacketHandler(final TitleManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleMessage(ClientboundPlayerTitlesPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            final Map<UUID, String> selectedTitles = new HashMap<>();

            for (Map.Entry<UUID, Text> titleEntry : message.titles.entrySet()) {
                selectedTitles.put(titleEntry.getKey(), TextSerializers.LEGACY_FORMATTING_CODE.serialize(titleEntry.getValue()));
            }

            this.manager.putSelectedTitles(selectedTitles);
        }
    }
}
