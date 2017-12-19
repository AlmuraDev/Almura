/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.network;

import com.almuradev.almura.feature.nick.NickManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;

import java.util.UUID;

import javax.inject.Inject;

public final class ClientboundNucleusNameChangeMappingPacketHandler implements MessageHandler<ClientboundNucleusNameChangeMappingPacket> {

    private final NickManager nickManager;

    @Inject
    private ClientboundNucleusNameChangeMappingPacketHandler(final NickManager nickManager) {
        this.nickManager = nickManager;
    }

    @Override
    public void handleMessage(final ClientboundNucleusNameChangeMappingPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient()) {
            final UUID entityUniqueId = message.uuid;
            final Text nickname = message.text;

            this.nickManager.put(entityUniqueId, nickname);

            final World world = Minecraft.getMinecraft().world;
            final EntityPlayer entity = world.getPlayerEntityByUUID(entityUniqueId);
            if (entity != null) {
                // Triggers Forge event, mod compat
                entity.refreshDisplayName();
            }
        }
    }
}
