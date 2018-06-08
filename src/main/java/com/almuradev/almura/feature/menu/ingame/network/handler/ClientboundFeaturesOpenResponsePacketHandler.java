/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.ingame.network.handler;

import com.almuradev.almura.feature.menu.ingame.FeaturesGUI;
import com.almuradev.almura.feature.menu.ingame.network.ClientboundFeaturesOpenResponsePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundFeaturesOpenResponsePacketHandler implements MessageHandler<ClientboundFeaturesOpenResponsePacket> {

    @Override
    public void handleMessage(ClientboundFeaturesOpenResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            final Minecraft client = Minecraft.getMinecraft();
            if (PacketUtil.checkThreadAndEnqueue(client, message, this, connection, side)) {

                final EntityPlayerSP player = client.player;
                final WorldClient world = client.world;
                final boolean isAdmin = message.admin;

                new FeaturesGUI(player, world, isAdmin).display();
            }
        }
    }
}
