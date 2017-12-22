package com.almuradev.almura.feature.notification.network;

import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public class ClientboundPlayerNotificationPacketHandler implements MessageHandler<ClientboundPlayerNotificationPacket> {

    private final ClientNotificationManager manager;

    @Inject
    public ClientboundPlayerNotificationPacketHandler(final ClientNotificationManager manager) {
        this.manager = manager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundPlayerNotificationPacket message, RemoteConnection connection, Platform.Type side) {
        if (message.inWindow) {
            // TODO Dockter, show the damn window
        } else {
            this.manager.queuePopup(new PopupNotification(message.message, message.timeToLive));
        }
    }
}
