/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.network.handler;

import com.almuradev.almura.feature.nick.ClientNickManager;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.text.SpongeTexts;

import java.util.UUID;

import javax.inject.Inject;

@SuppressWarnings("deprecation")
public final class ClientboundNucleusNameChangeMappingPacketHandler implements MessageHandler<ClientboundNucleusNameChangeMappingPacket> {

    private final ClientNickManager nickManager;

    @Inject
    private ClientboundNucleusNameChangeMappingPacketHandler(final ClientNickManager nickManager) {
        this.nickManager = nickManager;
    }

    @Override
    public void handleMessage(final ClientboundNucleusNameChangeMappingPacket message, final RemoteConnection connection, final Platform.Type side) {

        if (side.isClient()) {

            final Minecraft client = Minecraft.getMinecraft();

            if (PacketUtil.checkThreadAndEnqueue(client, message, this, connection, side)) {

                client.addScheduledTask(() -> {
                    final UUID entityUniqueId = message.uuid;
                    final Text nickname = message.text;

                    this.nickManager.put(entityUniqueId, nickname);

                    final World world = client.world;

                    if (world != null) {
                        final EntityPlayer player = world.getPlayerEntityByUUID(entityUniqueId);

                        if (player != null) {
                            final String newNick =
                                ForgeEventFactory.getPlayerDisplayName(player, TextSerializers.LEGACY_FORMATTING_CODE.serialize(nickname));

                            this.nickManager.put(entityUniqueId, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(newNick));
                            this.nickManager.setForgeNickname(player, newNick);
                        }
                    }

                    final EntityPlayerSP player = client.player;
                    if (player != null && player.connection != null) {

                        final NetworkPlayerInfo info = player.connection.getPlayerInfo(entityUniqueId);
                        if (info != null) {
                            info.setDisplayName(SpongeTexts.toComponent(nickname));
                        }
                    }
                });
            }
        }
    }
}
