/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.guide.client.gui.SimplePageView;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundPageOpenResponsePacketHandler implements MessageHandler<ClientboundPageOpenResponsePacket> {

    private final ClientPageManager manager;

    @Inject
    public ClientboundPageOpenResponsePacketHandler(final ClientPageManager manager) {
        this.manager = manager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundPageOpenResponsePacket message, RemoteConnection connection, Platform.Type side) {
        this.manager.setPage(message.page);

        SimplePageView view = null;

        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        if (currentScreen instanceof UIMessageBox.MessageBoxDialogScreen && ((UIMessageBox.MessageBoxDialogScreen) currentScreen).getParent()
                instanceof SimplePageView) {
            view = (SimplePageView) ((UIMessageBox.MessageBoxDialogScreen) currentScreen).getParent();
        } else if (currentScreen instanceof SimplePageView) {
            view = (SimplePageView) currentScreen;
        }

        if (view != null) {
            view.refreshPage();
        }
    }
}
