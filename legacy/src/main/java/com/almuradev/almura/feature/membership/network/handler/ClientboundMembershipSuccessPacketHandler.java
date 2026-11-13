/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.network.handler;

import com.almuradev.almura.feature.membership.client.gui.MembershipAppliedGui;
import com.almuradev.almura.feature.membership.network.ClientboundMembershipSuccessPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundMembershipSuccessPacketHandler implements MessageHandler<ClientboundMembershipSuccessPacket> {

    @Override
    public void handleMessage(ClientboundMembershipSuccessPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            final Minecraft client = Minecraft.getMinecraft();
            if (PacketUtil.checkThreadAndEnqueue(client, message, this, connection, side)) {

                final EntityPlayerSP player = client.player;

                new MembershipAppliedGui(null, message.membership).display();
            }
        }
    }
}
