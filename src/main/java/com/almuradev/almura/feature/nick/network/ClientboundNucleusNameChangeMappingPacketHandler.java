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
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.text.SpongeTexts;

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
        boolean updateTabList = false;
        if (world != null) {
            final EntityPlayer entity = world.getPlayerEntityByUUID(entityUniqueId);
            if (entity != null) {
                // Triggers Forge event, mod compat
                entity.refreshDisplayName();
            } else {
                updateTabList = true;

            }
        } else {
            updateTabList = true;
        }

        if (updateTabList) {
            // If the entity isn't here then it likely means they are in another world and nick changed. The resolution of the NameFormat event
            // results in a refresh of the tab-list but that event won't be fired if their entity doesn't exist on our client. We still have to fix
            // the tab list
            if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.connection != null) {
                final NetworkPlayerInfo info = Minecraft.getMinecraft().player.connection.getPlayerInfo(entityUniqueId);
                if (info != null) {
                    info.setDisplayName(SpongeTexts.toComponent(nickname));
                }
            }
        }
    }
}
