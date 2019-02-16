/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network.handler;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.guide.client.gui.GuidePageViewScreen;
import com.almuradev.almura.feature.guide.network.ClientboundPageListingsPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.malisis.core.client.gui.component.container.dialog.BasicMessageBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundPageListingsPacketHandler implements MessageHandler<ClientboundPageListingsPacket> {

    private final ClientPageManager manager;

    @Inject
    public ClientboundPageListingsPacketHandler(final ClientPageManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleMessage(ClientboundPageListingsPacket message, RemoteConnection connection, Platform.Type side) {

        if (side.isClient()) {

            final Minecraft client = Minecraft.getMinecraft();

            if (PacketUtil.checkThreadAndEnqueue(client, message, this, connection, side)) {
                this.manager.setPageEntries(message.pageEntries, message.switchToPageId);

                GuidePageViewScreen view = null;

                final GuiScreen currentScreen = client.currentScreen;

                if (currentScreen instanceof BasicMessageBox.MessageBoxDialogScreen && ((BasicMessageBox.MessageBoxDialogScreen) currentScreen).getParent()
                        instanceof GuidePageViewScreen) {
                    view = (GuidePageViewScreen) ((BasicMessageBox.MessageBoxDialogScreen) currentScreen).getParent();
                } else if (currentScreen instanceof GuidePageViewScreen) {
                    view = (GuidePageViewScreen) currentScreen;
                }

                if (view != null) {
                    view.refreshPageEntries(message.switchToPageId);
                }
            }
        }
    }
}
