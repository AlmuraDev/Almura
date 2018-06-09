/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.network.handler;

import com.almuradev.almura.feature.nick.ServerNickManager;
import com.almuradev.almura.feature.nick.network.ServerboundNucleusNameChangePacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.util.PacketUtil;
import io.github.nucleuspowered.nucleus.api.exceptions.NicknameException;
import io.github.nucleuspowered.nucleus.api.service.NucleusNicknameService;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.regex.Pattern;

import javax.inject.Inject;

public final class ServerboundNucleusNameChangePacketHandler implements MessageHandler<ServerboundNucleusNameChangePacket> {

    private final Game game;
    private final ServerNickManager nickManager;
    private final ServerNotificationManager notificationManager;

    @Inject
    public ServerboundNucleusNameChangePacketHandler(final Game game, final ServerNickManager nickManager, final ServerNotificationManager notificationManager) {
        this.game = game;
        this.nickManager = nickManager;
        this.notificationManager = notificationManager;
    }

    @Override
    public void handleMessage(final ServerboundNucleusNameChangePacket message, final RemoteConnection connection, final Platform.Type side) {

        if (side.isServer() && connection instanceof PlayerConnection && Sponge.isServerAvailable()) {
            final Server server = Sponge.getServer();
            final MinecraftServer threadListener = (MinecraftServer) server;
            final String regexPattern = "[a-zA-Z0-9_§]+";
            final Pattern nickNameRegex = Pattern.compile(regexPattern);

            if (PacketUtil.checkThreadAndEnqueue(threadListener, message, this, connection, side)) {
                final Text nickname = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message.nickname);
                final Player player = ((PlayerConnection) connection).getPlayer();

                // Perms Check to validate against modified client.
                if (!player.hasPermission("nucleus.nick.base")) {  // Nucleus Nickname Permission
                    this.notificationManager.sendPopupNotification(player, Text.of("Nickname Error!"), Text.of("Insufficient Permissions!"), 5);
                    return;
                }

                if (!nickNameRegex.matcher(nickname.toPlain()).matches()) {
                    this.notificationManager.sendPopupNotification(player, Text.of("Nickname Error!"), Text.of("Invalid Character in Nickname!"), 5);
                }

                // Check validity of client name
                if (nickname.isEmpty() || nickname.toPlain().length() <= 3 || nickname.toPlain().length() > 20) {
                    this.notificationManager.sendPopupNotification(player, Text.of("Nickname Error!"), Text.of("Validation of Nickname data failed!"), 5);
                    return;
                }

                this.nickManager.setForgeNickname((EntityPlayer) player, message.nickname);

                this.game.getServiceManager().provide(NucleusNicknameService.class).ifPresent((service) -> {

                    try {
                        if (player.getName().equalsIgnoreCase(message.nickname)) { // Detect if player name equals packet name, if it does, the client is attempting to remove the nickname.
                            this.nickManager.removeNickname(service, player);
                        } else {
                            this.nickManager.setNickname(service, player, nickname);
                        }
                    } catch (NicknameException e) {
                        e.printStackTrace();
                        return;
                    }
                    this.nickManager.sendNicknameUpdate(service, player);
                });

                server.getOnlinePlayers().forEach(onlinePlayer -> {
                    if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                        if (player.getName().equalsIgnoreCase(message.nickname)) {
                            this.notificationManager.sendPopupNotification(onlinePlayer, Text.of("Nickname Removed!"), Text.of("You are now known as ", message.nickname), 5);
                        } else {
                            this.notificationManager.sendPopupNotification(onlinePlayer, Text.of("New Nickname!"), Text.of("You are now known as ", message.nickname), 5);
                        }
                    } else {
                        if (player.getName().equalsIgnoreCase(message.nickname)) {
                            this.notificationManager.sendPopupNotification(onlinePlayer, Text.of("Nickname removed!"), Text.of(player.getName() + " has removed their nickname."), 5);
                        } else {
                            this.notificationManager.sendPopupNotification(onlinePlayer, Text.of("New Nickname!"), Text.of(player.getName() + " is now known as ", message.nickname), 5);
                        }
                    }
                });
            }
        }
    }
}
