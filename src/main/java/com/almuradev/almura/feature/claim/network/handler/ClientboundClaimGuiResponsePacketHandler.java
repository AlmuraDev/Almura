/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network.handler;

import com.almuradev.almura.feature.claim.gui.ManageClaimGUI;
import com.almuradev.almura.feature.claim.network.ClientboundClaimGuiResponsePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundClaimGuiResponsePacketHandler implements MessageHandler<ClientboundClaimGuiResponsePacket> {

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundClaimGuiResponsePacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
            new ManageClaimGUI(message.isOwner, message.isTrusted, message.isAdmin).display();
        }
    }
}

