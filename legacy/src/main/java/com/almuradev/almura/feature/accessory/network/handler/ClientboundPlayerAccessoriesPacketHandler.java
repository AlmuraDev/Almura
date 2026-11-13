/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.network.handler;

import com.almuradev.almura.feature.accessory.client.ClientAccessoryManager;
import com.almuradev.almura.feature.accessory.network.ClientboundPlayerAccessoriesPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundPlayerAccessoriesPacketHandler implements MessageHandler<ClientboundPlayerAccessoriesPacket> {

    private final ClientAccessoryManager manager;

    @Inject
    private ClientboundPlayerAccessoriesPacketHandler(final ClientAccessoryManager manager) {
        this.manager = manager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundPlayerAccessoriesPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
            this.manager.handleWorn(message.player, message.worn);
        }
    }
}
