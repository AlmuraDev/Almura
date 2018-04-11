/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.perms;

import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.feature.title.ServerTitleManager;
import com.almuradev.core.event.Witness;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.event.user.track.UserTrackEvent;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.UUID;

import javax.inject.Inject;

public final class PermsFeature implements Witness {

  private static final String LUCK_PERMS_DEFAULT_GROUP = "default";

  private final Server server;
  private final ServerNotificationManager notificationManager;
  private final ServerTitleManager titleManager;

  private LuckPermsApi api;

  @Inject
  public PermsFeature(final Server server, final ServerNotificationManager notificationManager, final ServerTitleManager titleManager) {
    this.server = server;
    this.notificationManager = notificationManager;
    this.titleManager = titleManager;
  }

  @Listener
  public void onServiceChange(ChangeServiceProviderEvent event) {
    if (event.getNewProviderRegistration().getService().equals(LuckPermsApi.class)) {
      this.api = (LuckPermsApi) event.getNewProviderRegistration().getProvider();
      this.api.getEventBus().subscribe(UserTrackEvent.class, e -> {

        final UUID targetUniqueId = e.getUser().getUuid();
        final Player target = this.server.getPlayer(targetUniqueId).orElse(null);
        final String toGroup = e.getGroupTo().orElse(null);

        if (toGroup != null) {
          final String fancyGroupName = WordUtils.capitalize(toGroup);
          if (e.getAction().name().equalsIgnoreCase("promotion")) {
            for (final Player onlinePlayer : this.server.getOnlinePlayers()) {
              if (!onlinePlayer.getUniqueId().equals(targetUniqueId)) {
                this.notificationManager.sendPopupNotification(onlinePlayer, Text.of("Player Promotion!"), Text.of(target.getDisplayNameData()
                  .displayName(), TextColors.WHITE, " has been promoted to: ", TextColors.GOLD, e.getGroupTo().get().toUpperCase()), 5);
              } else {
                this.notificationManager.sendPopupNotification(onlinePlayer, Text.of("Player Promotion!"), Text.of("You have been demoted to: ",
                  TextColors.GOLD, fancyGroupName), 5);
              }
            }
          }

          if (e.getAction().name().equalsIgnoreCase("demotion")) {
            for (final Player onlinePlayer : this.server.getOnlinePlayers()) {
              if (!onlinePlayer.getUniqueId().equals(targetUniqueId)) {
                this.notificationManager.sendPopupNotification(onlinePlayer, Text.of("Player Demotion!"), Text.of(target.getDisplayNameData()
                  .displayName(), TextColors.WHITE, " has been demoted to: ", TextColors.GOLD, e.getGroupTo().get().toUpperCase()), 5);
              } else {
                this.notificationManager.sendPopupNotification(onlinePlayer, Text.of("Player Demotion!"), Text.of("You have been demoted to: ",
                  TextColors.GOLD, fancyGroupName), 5);
              }
            }
          }
        }

        this.titleManager.recalculateSelectedTitles();
        this.titleManager.refreshSelectedTitles();
      });
    }
  }

  @Listener(order = Order.LAST)
  public void playerMove(final MoveEntityEvent.Teleport event, @Getter("getTargetEntity") final Player player) {
    if (this.api != null && differentExtent(event.getFromTransform(), event.getToTransform())) {
      final User user = this.api.getUser(player.getUniqueId());
      if (user != null) {
      if (user.getPrimaryGroup().equalsIgnoreCase(LUCK_PERMS_DEFAULT_GROUP)) {
          // ToDo:  not ready to be implemented but is functional.
          // api.getUser(player.getUniqueId()).setPrimaryGroup("citizen");
        }
      }
    }
  }

  private static boolean differentExtent(final Transform<World> from, final Transform<World> to) {
    return !from.getExtent().getUniqueId().equals(to.getExtent().getUniqueId());
  }
}
