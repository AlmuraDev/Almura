/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.network.handler;

import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.network.ClientboundPlayerNotificationPacket;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxButtons;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
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

        if (side.isClient()) {

            if (PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
                if (message.inWindow) {
                    UIMessageBox.showDialog(Minecraft.getMinecraft().currentScreen, message.title, message.message,
                        MessageBoxButtons.OK, null);
                } else {
                    this.manager.queuePopup(new PopupNotification(message.title, message.message, message.timeToLive));
                }
            }
        }
    }
}
