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

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

public class ClientboundNucleusNameMappingsPacketHandler implements MessageHandler<ClientboundNucleusNameMappingsPacket> {

    private final NickManager nickManager;

    @Inject
    private ClientboundNucleusNameMappingsPacketHandler(final NickManager nickManager) {
        this.nickManager = nickManager;
    }

    @Override
    public void handleMessage(final ClientboundNucleusNameMappingsPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient()) {
            final Map<UUID, Text> nicknames = message.nicknames;

            this.nickManager.putAll(nicknames);

            final World world = Minecraft.getMinecraft().world;
            if (world != null) {
                for (final EntityPlayer player : world.playerEntities) {
                    // Triggers Forge event, mod compat
                    player.refreshDisplayName();
                }
            }
        }
    }
}
