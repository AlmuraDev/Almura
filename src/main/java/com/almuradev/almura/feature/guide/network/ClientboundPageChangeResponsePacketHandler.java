/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.guide.client.gui.SimplePageView;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxButtons;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundPageChangeResponsePacketHandler implements MessageHandler<ClientboundPageChangeResponsePacket> {

    private ClientPageManager manager;

    @Inject
    public ClientboundPageChangeResponsePacketHandler(final ClientPageManager manager) {
        this.manager = manager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundPageChangeResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            if (Almura.debug) {
                System.out.println("Debug: Received ClientBoundChangeResponsePacket for Guide.");
            }
            if (!message.success) {
                UIMessageBox.showDialog(Minecraft.getMinecraft().currentScreen, I18n.format("almura.guide.dialog.error.title"), I18n.format(message.message), MessageBoxButtons.OK);
            } else {
                final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
                if (currentScreen != null && currentScreen instanceof SimplePageView) {
                    ((SimplePageView) currentScreen).refreshPage();
                }

                UIMessageBox.showDialog(Minecraft.getMinecraft().currentScreen, I18n.format("almura.guide.dialog.success.title"), I18n.format(message.message), MessageBoxButtons.OK);
            }
        }
    }
}
