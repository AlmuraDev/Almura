/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network.handler;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.claim.ServerClaimManager;
import com.almuradev.almura.feature.claim.network.ClientboundClaimGuiResponsePacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiSaveRequestPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.PacketUtil;
import me.ryanhamshire.griefprevention.GriefPrevention;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.world.WorldManager;

import javax.inject.Inject;

public final class ServerboundClaimGuiSaveRequestPacketHandler implements MessageHandler<ServerboundClaimGuiSaveRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager notificationManager;
    private final ServerClaimManager serverClaimManager;

    @Inject
    public ServerboundClaimGuiSaveRequestPacketHandler(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final
    ServerNotificationManager notificationManager, final ServerClaimManager serverClaimManager) {
        this.network = network;
        this.notificationManager = notificationManager;
        this.serverClaimManager = serverClaimManager;
    }

    @Override
    public void handleMessage(final ServerboundClaimGuiSaveRequestPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
                .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {
            final Player player = ((PlayerConnection) connection).getPlayer();
            final WorldServer worldServer = WorldManager.getWorld(message.worldName).orElse(null);

            if (worldServer == null) {
                notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Unable to find world, changes not saved!"), 5);
                return;
            }

            final World world = (World)worldServer;
            final Location<World> location = new Location<>(world, message.x, message.y, message.z);
            if (location == null) {
                notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Invalid location sent to server.  Changes not saved!"), 5);
            } else {
                final Claim claim = GriefPrevention.getApi().getClaimManager(player.getWorld()).getClaimAt(location);
                if (claim != null) { // if GP is loaded, claim should never be null.
                    final boolean isOwner = (claim.getOwnerUniqueId().equals(player.getUniqueId()));
                    final boolean isAdmin = player.hasPermission("griefprevention.admin");

                    if (isOwner || isAdmin) {
                        claim.getData().setName(Text.of(message.claimName));
                        claim.getData().setGreeting(Text.of(message.claimGreeting));
                        claim.getData().setFarewell(Text.of(message.claimFarewell));
                        serverClaimManager.buildUpdatePacket(player, claim);
                        notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Changed Saved!"), 5);
                    } else {
                        notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Insufficient Permissions!"), 5);
                    }
                } else {
                    notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Unable to lookup Claim, changed not saved!"), 5);
                }
            }
        }
    }
}
