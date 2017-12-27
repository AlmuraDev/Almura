/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.network;

import com.almuradev.almura.feature.nick.ClientNickManager;
import com.almuradev.almura.feature.nick.ServerNickManager;
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

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

public class ClientboundNucleusNameMappingsPacketHandler implements MessageHandler<ClientboundNucleusNameMappingsPacket> {

    private final ClientNickManager nickManager;

    @Inject
    private ClientboundNucleusNameMappingsPacketHandler(final ClientNickManager nickManager) {
        this.nickManager = nickManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundNucleusNameMappingsPacket message, final RemoteConnection connection, final Platform.Type side) {
        final Map<UUID, Text> nicknames = message.nicknames;

        this.nickManager.putAll(nicknames);

        final World world = Minecraft.getMinecraft().world;
        boolean updateTabList = nicknames.isEmpty();
        if (world != null) {
            for (Map.Entry<UUID, Text> entry : nicknames.entrySet()) {
                final EntityPlayer player = world.getPlayerEntityByUUID(entry.getKey());
                if (player != null) {
                    player.refreshDisplayName();
                } else {
                    updateTabList = true;
                }
            }
        } else {
            updateTabList = true;
        }

        if (updateTabList) {
            // If the world isn't here then it likely means we logged in too fast. The resolution of the NameFormat event
            // results in a refresh of the tab-list but that event won't be fired if the client has no world. We still have to fix
            // the tab list
            if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.connection != null) {
                nicknames.forEach((key, value) -> {
                    final NetworkPlayerInfo info = Minecraft.getMinecraft().player.connection.getPlayerInfo(key);
                    if (info != null) {
                        info.setDisplayName(SpongeTexts.toComponent(value));
                    }
                });
            }
        }
    }
}
