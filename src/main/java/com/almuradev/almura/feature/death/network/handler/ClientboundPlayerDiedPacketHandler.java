/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death.network.handler;

import com.almuradev.almura.feature.death.client.gui.PlayerDiedGUI;
import com.almuradev.almura.feature.death.network.ClientboundPlayerDiedPacket;
import com.almuradev.almura.feature.death.network.ServerboundReviveRequestPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.*;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.world.WorldManager;

import javax.inject.Inject;

public final class ClientboundPlayerDiedPacketHandler implements MessageHandler<ClientboundPlayerDiedPacket> {

    @Override
    public void handleMessage(ClientboundPlayerDiedPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            final Minecraft client = Minecraft.getMinecraft();
            if (PacketUtil.checkThreadAndEnqueue(client, message, this, connection, side)) {

                final EntityPlayerSP player = client.player;
                final WorldClient world = client.world;

                new PlayerDiedGUI(player, message.dropAmount);
            }
        }
    }
}
