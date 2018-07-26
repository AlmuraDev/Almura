/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network.handler;

import com.almuradev.almura.feature.title.ClientTitleManager;
import com.almuradev.almura.feature.title.network.ClientboundSelectedTitleBulkPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundSelectedTitleBulkPacketHandler implements MessageHandler<ClientboundSelectedTitleBulkPacket> {

    private final ClientTitleManager titleManager;

    @Inject
    public ClientboundSelectedTitleBulkPacketHandler(final ClientTitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundSelectedTitleBulkPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {

            this.titleManager.putSelectedTitles(message.titles);
        }
    }
}
