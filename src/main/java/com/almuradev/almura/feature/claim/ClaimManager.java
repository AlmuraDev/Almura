/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import me.ryanhamshire.griefprevention.api.GriefPreventionApi;
import me.ryanhamshire.griefprevention.api.event.*;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import javax.inject.Inject;

public final class ClaimManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final CommandManager commandManager;
    private final ServerNotificationManager notificationManager;

    private GriefPreventionApi griefPreventionApi;

    @Inject
    public ClaimManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final CommandManager
        commandManager, final ServerNotificationManager notificationManager) {
        this.network = network;
        this.commandManager = commandManager;
        this.notificationManager = notificationManager;
    }

    @Listener
    public void onServiceChange(ChangeServiceProviderEvent event) {
        if (event.getNewProviderRegistration().getService().equals(GriefPreventionApi.class)) {
            this.griefPreventionApi = (GriefPreventionApi) event.getNewProviderRegistration().getProvider();
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {

    }

    @Listener()
    public void onChangeClaim(final ChangeClaimEvent event) {
        System.out.println("ALMURA: Claim changed.");
    }

    @Listener()
    public void onCreateClaim(final CreateClaimEvent event) {
        System.out.println("ALMURA: Claim created.");
    }

    @Listener()
    public void onDeleteClaim(final DeleteClaimEvent event) {
        System.out.println("ALMURA: Claim deleted.");
    }

    @Listener()
    public void onTaxClaim(final TaxClaimEvent event) {

    }

    @Listener()
    public void onClaimFlagChange(final FlagClaimEvent event) {
        System.out.println("ALMURA: Claim flag changed.");
    }

}
