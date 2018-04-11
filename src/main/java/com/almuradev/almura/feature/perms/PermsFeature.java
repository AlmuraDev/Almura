/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.perms;

import com.almuradev.almura.feature.title.ServerTitleManager;
import com.almuradev.core.event.Witness;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.event.user.track.UserTrackEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.world.World;

import java.util.Optional;

import javax.inject.Inject;

public final class PermsFeature implements Witness {

  private final ServerTitleManager titleManager;

  @Inject
  public PermsFeature(final ServerTitleManager titleManager) {
    this.titleManager = titleManager;
  }

  @Listener
  public void onServiceChange(ChangeServiceProviderEvent event) {
    if (event.getNewProviderRegistration().getService().equals(LuckPermsApi.class)) {
      final LuckPermsApi api = (LuckPermsApi) event.getNewProviderRegistration().getProvider();
      api.getEventBus().subscribe(UserTrackEvent.class, e -> {
        this.titleManager.recalculateSelectedTitles();
        this.titleManager.refreshSelectedTitles();
      });
    }
  }

  @Listener(order = Order.LAST)
  public void playerMove(final MoveEntityEvent.Teleport event, @Getter("getTargetEntity") final Player player) throws IllegalAccessException {
    if (differentExtent(event.getFromTransform(), event.getToTransform())) {
      Optional<ProviderRegistration<LuckPermsApi>> provider = Sponge.getServiceManager().getRegistration(LuckPermsApi.class);
      if (provider.isPresent()) {
        final LuckPermsApi api = provider.get().getProvider();
        if (api.getUser(player.getUniqueId()).getPrimaryGroup().equalsIgnoreCase("default")) { //LP's default group for new users.
          // ToDo:  not ready to be implemented but is functional.
          // api.getUser(player.getUniqueId()).setPrimaryGroup("citizen");
        }
      }
    }
  }


  private static boolean differentExtent(final Transform<World> from, final Transform<World> to) {
    return !from.getExtent().equals(to.getExtent());
  }
}
