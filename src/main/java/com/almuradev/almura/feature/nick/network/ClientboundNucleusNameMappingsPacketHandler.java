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
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

@SuppressWarnings("deprecation")
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

        if (world != null) {

            message.nicknames.forEach((uniqueId, nickname) -> {
                final EntityPlayer player = world.getPlayerEntityByUUID(uniqueId);

                if (player != null) {
                    final String newNick = ForgeEventFactory.getPlayerDisplayName(player, TextSerializers.LEGACY_FORMATTING_CODE.serialize(nickname));

                    this.nickManager.put(player.getUniqueID(), TextSerializers.LEGACY_FORMATTING_CODE.deserialize(newNick));

                    try {
                        this.nickManager.adjustPlayerNickname(player, newNick);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        Minecraft.getMinecraft().addScheduledTask(() -> {
            final EntityPlayerSP player = Minecraft.getMinecraft().player;
            if (player != null && player.connection != null) {

                message.nicknames.forEach((uniqueId, nickname) -> {
                    final NetworkPlayerInfo info = player.connection.getPlayerInfo(uniqueId);
                    if (info != null) {
                        info.setDisplayName(SpongeTexts.toComponent(nickname));
                    }
                });
            }
        });
    }
}
