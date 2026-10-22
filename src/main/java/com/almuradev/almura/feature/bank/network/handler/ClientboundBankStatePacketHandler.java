/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.network.handler;

import com.almuradev.almura.feature.bank.client.ClientBankManager;
import com.almuradev.almura.feature.bank.network.ClientboundBankStatePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundBankStatePacketHandler implements MessageHandler<ClientboundBankStatePacket> {

    private final ClientBankManager bankManager;

    @Inject
    public ClientboundBankStatePacketHandler(final ClientBankManager bankManager) {
        this.bankManager = bankManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundBankStatePacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
            this.bankManager.handleState(message);
        }
    }
}
