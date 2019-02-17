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
import com.almuradev.almura.feature.guide.network.ClientboundPageChangeResponsePacket;
import com.almuradev.almura.feature.guide.network.PageChangeType;
import com.almuradev.almura.feature.guide.network.ServerboundGuideOpenRequestPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
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
        if (side.isClient() && message.success) {

            final Minecraft client = Minecraft.getMinecraft();

            if (PacketUtil.checkThreadAndEnqueue(client, message, this, connection, side)) {
                final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
                if (currentScreen != null && currentScreen instanceof GuidePageViewScreen) {
                    if (message.changeType == PageChangeType.REMOVE) {
                        ((GuidePageViewScreen) currentScreen).close();
                        manager.network.sendToServer(new ServerboundGuideOpenRequestPacket());
                    } else {
                        ((GuidePageViewScreen) currentScreen).refreshPage();
                    }
                }
            }
        }
    }
}
