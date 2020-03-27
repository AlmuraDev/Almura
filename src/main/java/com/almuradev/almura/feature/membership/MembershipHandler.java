/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.death.network.ClientboundPlayerDiedPacket;
import com.almuradev.almura.feature.membership.network.ClientboundMembershipGuiOpenPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.UchatUtil;
import com.almuradev.core.event.Witness;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;

import javax.inject.Inject;

public final class MembershipHandler implements Witness {

    private final ServerNotificationManager serverNotificationManager;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public MembershipHandler(final ServerNotificationManager serverNotificationManager, final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.serverNotificationManager = serverNotificationManager;
        this.network = network;
    }

    public void requestClientGui(Player player) {
        this.network.sendTo(player, new ClientboundMembershipGuiOpenPacket());
    }

    // Start of methods
}
