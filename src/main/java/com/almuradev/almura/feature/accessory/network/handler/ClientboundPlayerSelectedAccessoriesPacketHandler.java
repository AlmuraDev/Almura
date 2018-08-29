/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.network.handler;

import com.almuradev.almura.feature.accessory.client.ClientAccessoryManager;
import com.almuradev.almura.feature.accessory.network.ClientboundPlayerSelectedAccessoriesPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundPlayerSelectedAccessoriesPacketHandler implements MessageHandler<ClientboundPlayerSelectedAccessoriesPacket> {

    private final ClientAccessoryManager accessoryManager;

    @Inject
    public ClientboundPlayerSelectedAccessoriesPacketHandler(final ClientAccessoryManager accessoryManager) {
        this.accessoryManager = accessoryManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundPlayerSelectedAccessoriesPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {

            this.accessoryManager.handleSelectedAccessories(message.uniqueId, message.accessories);
        }
    }
}
