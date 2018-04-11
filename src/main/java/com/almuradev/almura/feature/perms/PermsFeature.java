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
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;

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
        System.out.println("Event: " + e.getAction().name());

        this.titleManager.recalculateSelectedTitles();
        this.titleManager.refreshSelectedTitles();
      });
    }
  }
}
