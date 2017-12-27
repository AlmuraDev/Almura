/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.network;

import com.almuradev.almura.feature.nick.ClientNickManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;

import java.util.UUID;

import javax.inject.Inject;

public final class ClientboundNucleusNameChangeMappingPacketHandler implements MessageHandler<ClientboundNucleusNameChangeMappingPacket> {

    private final ClientNickManager nickManager;

    @Inject
    private ClientboundNucleusNameChangeMappingPacketHandler(final ClientNickManager nickManager) {
        this.nickManager = nickManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundNucleusNameChangeMappingPacket message, final RemoteConnection connection, final Platform.Type side) {
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
