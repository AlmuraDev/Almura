/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.hud.network.ClientboundPlayerCountPacket;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerCurrencyPacket;
import com.almuradev.almura.feature.hud.network.ClientboundWorldNamePacket;
import com.almuradev.almura.feature.nick.ServerNickManager;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.feature.title.ServerTitleManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import io.github.nucleuspowered.nucleus.api.events.NucleusFirstJoinEvent;
import net.kyori.membrane.facet.Activatable;
import net.minecraft.util.text.TextFormatting;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.math.BigDecimal;

public final class ServerHeadUpDisplayManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final Game game;
    private final PluginContainer container;
    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager manager;
    private final ServerTitleManager serverTitleManager;
    private final ServerNickManager serverNickManager;

    @Inject
    private ServerHeadUpDisplayManager(final Game game, final PluginContainer container, @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding
        .IndexedMessageChannel network, final ServerNotificationManager manager, final ServerTitleManager serverTitleManager, final
    ServerNickManager serverNickManager) {
        this.game = game;
        this.container = container;
        this.network = network;
        this.manager = manager;
        this.serverTitleManager = serverTitleManager;
        this.serverNickManager = serverNickManager;
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

        final ClientboundPlayerCountPacket packet = this.createPlayerCountPacket(true);

        for (final Player viewer : this.game.getServer().getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(player.getUniqueId())) {
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
    public void onEconomyTransaction(final EconomyTransactionEvent event) {
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

    @Listener
    public void onPlayerFirstJoin(NucleusFirstJoinEvent event, @Getter("getTargetEntity") final Player player) {
        for (final Player onlinePlayer : this.game.getServer().getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                this.manager.sendPopupNotification(player, Text.of("Welcome!"), Text.of("Welcome to Almura."), 5);
            } else {
                this.manager.sendPopupNotification(onlinePlayer, Text.of("New Player!!!"), Text.of("Please welcome " + player.getName() + " to Almura."), 5);
            }
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        checkNotNull(player);
        Task.builder()
            .delayTicks(20)
            .execute(t -> {
                final String displayName = serverNickManager.getNickname(player);
                final String playerTitle = serverTitleManager.getSelectedTitleForFormatted(player);

                for (final Player players : Sponge.getServer().getOnlinePlayers()) {
                    if (players.equals(player)) {
                        continue;
                    }
                    if (playerTitle.isEmpty()) {
                        this.manager.sendPopupNotification(players, Text.of(TextFormatting.BLUE + "Player Joined" + TextFormatting.WHITE), Text.of (TextFormatting.YELLOW + displayName + TextFormatting.WHITE + " has " + "joined the server"), 5);
                    } else {
                        this.manager.sendPopupNotification(players, Text.of(TextFormatting.BLUE + "Player Joined" + TextFormatting.WHITE), Text.of (displayName + " - " + playerTitle +"§f - has joined the server"), 5);
                    }
                }
            })
            .submit(this.container);
    }

    @Listener(order = Order.PRE)
    public void onPlayerQuit(final ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") final Player player) {
        checkNotNull(player);

        final String displayName = serverNickManager.getNickname(player);
        final String playerTitle = serverTitleManager.getSelectedTitleForFormatted(player);

        for (final Player players : Sponge.getServer().getOnlinePlayers()) {
            if (players.equals(player)) {
                continue;
            }
            if (playerTitle.isEmpty()) {
                this.manager.sendPopupNotification(players, Text.of(TextFormatting.DARK_AQUA + "Player Disconnected" + TextFormatting.WHITE), Text.of (TextFormatting.YELLOW + displayName + TextFormatting.WHITE + " has " + "left the server"), 5);
            } else {
                this.manager.sendPopupNotification(players, Text.of(TextFormatting.DARK_AQUA + "Player Disconnected" + TextFormatting.WHITE), Text.of (displayName + " - " + playerTitle +"§f - has left the server"), 5);
            }
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
        return new ClientboundWorldNamePacket(transform.getExtent().getName());
    }

    @Nullable
    private ClientboundPlayerCurrencyPacket createPlayerCurrencyPacket(final Player player) {
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

    private static boolean differentExtent(final Transform<World> from, final Transform<World> to) {
        return !from.getExtent().equals(to.getExtent());
    }
}
