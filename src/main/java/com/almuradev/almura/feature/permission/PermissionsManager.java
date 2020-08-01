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
import com.almuradev.almura.feature.title.Title;
import com.almuradev.almura.feature.title.network.ClientboundAvailableTitlesResponsePacket;
import com.almuradev.almura.feature.title.network.ClientboundSelectedTitlePacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.UchatUtil;
import com.almuradev.core.event.Witness;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.data.PlayerData;
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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
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
    private final Scheduler scheduler;
    private final PluginContainer container;

    @Inject
    public PermissionsManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final CommandManager
        commandManager, final ServerNotificationManager notificationManager, final ServerTitleManager titleManager, final Scheduler scheduler,
        PluginContainer container) {
        this.network = network;
        this.commandManager = commandManager;
        this.notificationManager = notificationManager;
        this.titleManager = titleManager;
        this.scheduler = scheduler;
        this.container = container;
    }

    private static boolean differentExtent(final Transform<World> from, final Transform<World> to) {
        return !from.getExtent().getUniqueId().equals(to.getExtent().getUniqueId());
    }

    @Listener
    public void onServiceChange(ChangeServiceProviderEvent event) {
        boolean debug = false;
        if (event.getNewProviderRegistration().getService().equals(NucleusNicknameService.class)) {
            this.nickApi = (NucleusNicknameService) event.getNewProviderRegistration().getProvider();
        } else if (event.getNewProviderRegistration().getService().equals(LuckPerms.class)) {
            this.permApi = (LuckPerms) event.getNewProviderRegistration().getProvider();
            this.permApi.getEventBus().subscribe(UserTrackEvent.class, e -> {
                final UUID targetUniqueId = e.getUser().getUniqueId();
                final Player target = Sponge.getServer().getPlayer(targetUniqueId).orElse(null);
                final String toGroup = e.getGroupTo().orElse(null);

                // Refresh Grief Prevention user options after changing groups.
                PlayerData playerData = GriefDefender.getCore().getPlayerData(target.getWorld().getUniqueId(), target.getUniqueId()).get();
                // Todo: is this fix still needed?
                //playerData.refreshPlayerOptions();

                if (targetUniqueId != null && toGroup != null && this.nickApi != null) {
                    Text nick = Text.of("");
                    // Note: target is null when player is offline.
                    if (target != null) {
                        nick = this.nickApi.getNickname(target).orElse(Text.of(target.getName()));
                    } else {
                        nick = Text.of(WordUtils.capitalize(e.getUser().getUsername().toLowerCase()));
                    }
                    final String fancyGroupName = WordUtils.capitalize(toGroup.toLowerCase());

                    if (debug)
                        System.out.println("Cause: " + e.getAction().name());

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
                        updateTitlesForPlayer(target);
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

    public void updateTitlesForPlayer(Player player) {
        System.out.println("Starting delayed task for updating player titles");
        this.scheduler
            .createTaskBuilder()
            .delayTicks(50)
            .execute(() -> {
                this.titleManager.calculateAvailableTitlesFor(player);
                // Just in-case the GUI is open currently, send this down. If it isn't, the data will be overriden by a request anyways..
                this.titleManager.getAvailableTitlesFor(player).ifPresent(availableTitles -> {
                    this.network.sendTo(player, new ClientboundAvailableTitlesResponsePacket(availableTitles));
                });

                String titleId = "";

                // The following are setup as fallback titles when a record doesn't exist in the DB for the player.
                if (player.hasPermission("almura.title.soldier")) titleId = "soldier";
                if (player.hasPermission("almura.title.survivor")) titleId = "survivor";
                if (player.hasPermission("almura.title.citizen")) titleId = "citizen";
                if (player.hasPermission("almura.title.explorer")) titleId = "explorer";
                if (player.hasPermission("almura.title.pioneer")) titleId = "pioneer";
                if (player.hasPermission("almura.title.architect")) titleId = "architect";
                if (player.hasPermission("almura.title.ancient")) titleId = "ancient";

                final String finalizedTitleId = titleId;

                final Title selectedTitle = titleManager.getTitle(finalizedTitleId).orElse(null);

                if (titleManager.verifySelectedTitle(player, selectedTitle)) {
                    titleManager.selectedTitles.put(player.getUniqueId(), selectedTitle);

                    // Send everyone joiner's selected title
                    this.network.sendToAll(new ClientboundSelectedTitlePacket(player.getUniqueId(), finalizedTitleId));
                }

                System.out.println("Should have updated titles for: " + player);
            })
            .submit(this.container);
    }
}
