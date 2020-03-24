/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.permission;

import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.feature.title.ServerTitleManager;
import com.almuradev.almura.feature.title.network.ClientboundAvailableTitlesResponsePacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.UchatUtil;
import com.almuradev.core.event.Witness;
import io.github.nucleuspowered.nucleus.api.service.NucleusNicknameService;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.user.track.UserTrackEvent;
import net.luckperms.api.model.user.User;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.UUID;

import javax.inject.Inject;

public final class PermissionsManager implements Witness {

    private static final String LUCK_PERMS_DEFAULT_GROUP = "default";

    private final ChannelBinding.IndexedMessageChannel network;
    private final CommandManager commandManager;
    private final ServerNotificationManager notificationManager;
    private final ServerTitleManager titleManager;

    private LuckPerms permApi;
    private NucleusNicknameService nickApi;

    @Inject
    public PermissionsManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final CommandManager
        commandManager, final ServerNotificationManager notificationManager, final ServerTitleManager titleManager) {
        this.network = network;
        this.commandManager = commandManager;
        this.notificationManager = notificationManager;
        this.titleManager = titleManager;
    }

    private static boolean differentExtent(final Transform<World> from, final Transform<World> to) {
        return !from.getExtent().getUniqueId().equals(to.getExtent().getUniqueId());
    }

    @Listener
    public void onServiceChange(ChangeServiceProviderEvent event) {
        if (event.getNewProviderRegistration().getService().equals(NucleusNicknameService.class)) {
            this.nickApi = (NucleusNicknameService) event.getNewProviderRegistration().getProvider();
        } else if (event.getNewProviderRegistration().getService().equals(LuckPerms.class)) {
            this.permApi = (LuckPerms) event.getNewProviderRegistration().getProvider();

            this.permApi.getEventBus().subscribe(UserTrackEvent.class, e -> {
                final UUID targetUniqueId = e.getUser().getUniqueId();
                final Player target = Sponge.getServer().getPlayer(targetUniqueId).orElse(null);
                final String toGroup = e.getGroupTo().orElse(null);

                if (targetUniqueId != null && toGroup != null && this.nickApi != null) {
                    Text nick = Text.of("");
                    // Note: target is null when player is offline.
                    if (target != null) {
                        nick = this.nickApi.getNickname(target).orElse(Text.of(target.getName()));
                    } else {
                        nick = Text.of(WordUtils.capitalize(e.getUser().getUsername().toLowerCase()));
                    }
                    final String fancyGroupName = WordUtils.capitalize(toGroup.toLowerCase());
                    if (e.getAction().name().equalsIgnoreCase("promotion")) {
                        // Broadcast Promotion to Discord
                        UchatUtil.relayMessageToDiscord(":military_medal:", Text.of(TextColors.AQUA, nick.toPlain(), TextColors.WHITE, " has been promoted to: ", TextColors.GOLD, fancyGroupName).toPlain(), true);

                        for (final Player onlinePlayer : Sponge.getServer().getOnlinePlayers()) {
                            if (!onlinePlayer.getUniqueId().equals(targetUniqueId)) {
                                this.notificationManager
                                    .sendPopupNotification(onlinePlayer, Text.of("Player Promotion!"), Text.of(TextColors.AQUA, nick.toPlain(),
                                        TextColors.WHITE, " has been promoted to: ", TextColors.GOLD, fancyGroupName), 5);
                            } else {
                                this.notificationManager
                                    .sendPopupNotification(onlinePlayer, Text.of("Player Promotion!"), Text.of("You have been demoted to: ",
                                        TextColors.GOLD, fancyGroupName), 5);
                            }
                        }
                    }

                    if (e.getAction().name().equalsIgnoreCase("demotion")) {
                        // Broadcast demotion to Discord
                        UchatUtil.relayMessageToDiscord(":violin:", Text.of(TextColors.AQUA, nick.toPlain(), TextColors.WHITE, " has been demoted to: ", TextColors.GOLD, fancyGroupName).toPlain(), true);

                        for (final Player onlinePlayer : Sponge.getServer().getOnlinePlayers()) {
                            if (!onlinePlayer.getUniqueId().equals(targetUniqueId)) {
                                this.notificationManager
                                    .sendPopupNotification(onlinePlayer, Text.of("Player Demotion!"), Text.of(TextColors.AQUA, nick.toPlain(),
                                        TextColors.WHITE, " has been demoted to: ", TextColors.GOLD, fancyGroupName), 5);
                            } else {
                                this.notificationManager
                                    .sendPopupNotification(onlinePlayer, Text.of("Player Demotion!"), Text.of("You have been demoted to: ",
                                        TextColors.GOLD, fancyGroupName), 5);
                            }
                        }
                    }

                    if (target != null) {
                        this.titleManager.calculateAvailableTitlesFor(target);

                        // Just in-case the GUI is open currently, send this down. If it isn't, the data will be overriden by a request anyways..
                        this.titleManager.getAvailableTitlesFor(target).ifPresent(availableTitles -> this.network.sendTo(target, new ClientboundAvailableTitlesResponsePacket(availableTitles)));

                        this.titleManager.verifySelectedTitle(target, null);
                    }
                }
            });
        }
    }

    @Listener(order = Order.LAST)
    public void onMovePlayerTeleport(final MoveEntityEvent.Teleport event, @Getter("getTargetEntity") final Player player) {
        if (this.permApi != null && differentExtent(event.getFromTransform(), event.getToTransform())) {
            final User user = this.permApi.getUserManager().getUser(player.getUniqueId());
            // The purpose of this method is to upgrade people out of the default permissions group once they teleport off of Asgard (starting world).
            if (user != null && user.getPrimaryGroup().equalsIgnoreCase(LUCK_PERMS_DEFAULT_GROUP)) {
                final String command = "lp user " + player.getName() + " promote members";
                this.commandManager.process(Sponge.getServer().getConsole(), command);
            }
        }
    }
}
