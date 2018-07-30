/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network.handler;

import com.almuradev.almura.feature.claim.ServerClaimManager;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiAbandonRequestPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.PacketUtil;
import me.ryanhamshire.griefprevention.GriefPrevention;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import net.minecraft.server.MinecraftServer;
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

import javax.inject.Inject;

public final class ServerboundClaimGuiAbandonRequestPacketHandler implements MessageHandler<ServerboundClaimGuiAbandonRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager notificationManager;
    private final ServerClaimManager claimManager;

    @Inject
    public ServerboundClaimGuiAbandonRequestPacketHandler(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final
    ServerNotificationManager notificationManager, final ServerClaimManager claimManager) {
        this.network = network;
        this.notificationManager = notificationManager;
        this.claimManager = claimManager;
    }

    @Override
    public void handleMessage(final ServerboundClaimGuiAbandonRequestPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
            .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {
            final Player player = ((PlayerConnection) connection).getPlayer();
            final World world = Sponge.getServer().getWorld(message.worldName).orElse(null);

            if (world == null) {
                this.notificationManager
                    .sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Unable to find world, changes not saved!"), 5);
                return;
            }

            final Location<World> location = new Location<World>(world, message.x, message.y, message.z);
            final Claim claim = GriefPrevention.getApi().getClaimManager(world).getClaimAt(location);
            if (claim == null) {
                this.notificationManager
                    .sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Unable to lookup Claim, changed not saved!"), 5);
            } else {
                final boolean isOwner = claim.getOwnerUniqueId().equals(player.getUniqueId());
                final boolean isAdmin = player.hasPermission("griefprevention.admin");

                if (isOwner || isAdmin) {
                    claim.getClaimManager().deleteClaim(claim);
                    this.claimManager.sendUpdatePacket(player, claim);
                    this.notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Claim Abandoned!"), 5);
                } else {
                    this.notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Insufficient Permissions!"), 5);
                }
            }
        }
    }
}
