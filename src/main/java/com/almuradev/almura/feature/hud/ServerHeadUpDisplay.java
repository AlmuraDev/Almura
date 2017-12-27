/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.core.server.ServerConfiguration;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerCountPacket;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerCurrencyPacket;
import com.almuradev.almura.feature.hud.network.ClientboundWorldNamePacket;
import com.almuradev.almura.shared.event.Witness;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import net.kyori.membrane.facet.Activatable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class ServerHeadUpDisplay extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final Game game;
    private final PluginContainer container;
    private final ChannelBinding.IndexedMessageChannel network;
    private final MappedConfiguration<ServerConfiguration> config;

    @Inject
    private ServerHeadUpDisplay(final Game game, final PluginContainer container, @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding
            .IndexedMessageChannel network, final MappedConfiguration<ServerConfiguration> config) {
        this.game = game;
        this.container = container;
        this.network = network;
        this.config = config;
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener(order = Order.LAST)
    public void clientJoin(final ClientConnectionEvent.Join event) {
        final Player player = event.getTargetEntity();

        this.network.sendTo(player, this.createWorldNamePacket(player.getTransform()));

        final ClientboundPlayerCountPacket packet = this.createPlayerCountPacket(false);

        for (final Player viewer : this.game.getServer().getOnlinePlayers()) {
            this.network.sendTo(viewer, packet);
        }

        this.network.sendTo(player, packet);

        final ClientboundPlayerCurrencyPacket currPacket = this.createPlayerCurrencyPacket(player);
        if (currPacket != null) {
            this.network.sendTo(player, currPacket);
        }
    }

    @Listener(order = Order.LAST)
    public void clientDisconnect(final ClientConnectionEvent.Disconnect event) {
        final Player player = event.getTargetEntity();

        this.network.sendTo(player, this.createWorldNamePacket(player.getTransform()));

        final ClientboundPlayerCountPacket packet = this.createPlayerCountPacket(true);
        for (final Player viewer : this.game.getServer().getOnlinePlayers()) {
            if (viewer == player) {
                continue;
            }
            this.network.sendTo(viewer, packet);
        }
    }

    @Listener(order = Order.LAST)
    public void playerMove(final MoveEntityEvent.Teleport event, @Getter("getTargetEntity") final Player player) {
        if (differentExtent(event.getFromTransform(), event.getToTransform())) {
            this.network.sendTo(player, this.createWorldNamePacket(event.getToTransform()));
        }
    }

    @Listener(order = Order.LAST)
    public void respawnPlayer(final RespawnPlayerEvent event) {
        if (differentExtent(event.getFromTransform(), event.getToTransform())) {
            this.network.sendTo(event.getTargetEntity(), this.createWorldNamePacket(event.getToTransform()));
        }
    }

    @Listener(order = Order.LAST)
    public void onEconomyTransaction(EconomyTransactionEvent event) {
        final TransactionResult result = event.getTransactionResult();
        final Account account = result.getAccount();
        if (account instanceof UniqueAccount) {
            Sponge.getServer().getPlayer(((UniqueAccount) account).getUniqueId()).ifPresent(player -> Task.builder().delayTicks(1).execute(() -> {
                final ClientboundPlayerCurrencyPacket packet = this.createPlayerCurrencyPacket(player);
                if (packet != null) {
                    this.network.sendTo(player, packet);
                }
            }).submit(this.container));
        }
    }

    private ClientboundPlayerCountPacket createPlayerCountPacket(final boolean disconnect) {
        final Server server = this.game.getServer();
        // Subtract one from the online player count when a player is disconnecting
        // due to the player not being removed from the online players list until
        // after the disconnect event has been posted.
        final int online = server.getOnlinePlayers().size() - (disconnect ? 1 : 0);
        return new ClientboundPlayerCountPacket(online, server.getMaxPlayers());
    }

    private ClientboundWorldNamePacket createWorldNamePacket(final Transform<World> transform) {
        return new ClientboundWorldNamePacket(this.getWorldName(transform));
    }

    @Nullable
    private ClientboundPlayerCurrencyPacket createPlayerCurrencyPacket(Player player) {
        final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
        if (service != null) {
            final Account account = service.getOrCreateAccount(player.getUniqueId()).orElse(null);
            BigDecimal balance = BigDecimal.ZERO;

            if (account != null) {
                final Currency currency = service.getDefaultCurrency();
                balance = account.getBalance(currency);
            }

            return new ClientboundPlayerCurrencyPacket(balance);
        }

        return null;
    }

    private String getWorldName(final Transform<World> transform) {
        final String name = transform.getExtent().getName();
        return this.config.get().world.friendlyNames.getOrDefault(name, name);
    }

    private static boolean differentExtent(final Transform<World> from, final Transform<World> to) {
        return !from.getExtent().equals(to.getExtent());
    }
}
