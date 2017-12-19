package com.almuradev.almura.feature.hud.network;

import com.almuradev.almura.feature.hud.HUDData;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import java.text.DecimalFormat;

public final class ClientboundPlayerCurrencyPacketHandler implements MessageHandler<ClientboundPlayerCurrencyPacket> {

    @Override
    public void handleMessage(ClientboundPlayerCurrencyPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            final DecimalFormat format = new DecimalFormat("###,###.##");

            HUDData.IS_ECONOMY_PRESENT = true;
            HUDData.PLAYER_CURRENCY = format.format(message.money);
        }
    }
}
