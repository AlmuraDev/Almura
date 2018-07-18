/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network.handler;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.guide.client.gui.SimplePageView;
import com.almuradev.almura.feature.guide.network.ClientboundPageListingsPacket;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import com.almuradev.almura.shared.util.PacketUtil;
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

                SimplePageView view = null;

                final GuiScreen currentScreen = client.currentScreen;

                if (currentScreen instanceof UIMessageBox.MessageBoxDialogScreen && ((UIMessageBox.MessageBoxDialogScreen) currentScreen).getParent()
                        instanceof SimplePageView) {
                    view = (SimplePageView) ((UIMessageBox.MessageBoxDialogScreen) currentScreen).getParent();
                } else if (currentScreen instanceof SimplePageView) {
                    view = (SimplePageView) currentScreen;
                }

                if (view != null) {
                    view.refreshPageEntries(message.switchToPageId);
                }
            }
        }
    }
}
