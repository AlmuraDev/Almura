/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.economy.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.economy.EconomyAccounts;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerMoney;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.plugin.PluginContainer;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * The server's economy (Sponge's {@code EconomyService}, such as EconomyLite's) for Almura, as 3.5's HUD read it: each player is sent their
 * balance in the default currency when they join and whenever a transaction changes their account.
 * <p>
 * Only loaded on a server with the Sponge API (SpongeNeo); see {@link com.almuradev.almura.feature.economy.Economy}. Without an economy service nothing is sent and
 * the HUD shows no money, as on 3.5.
 */
public final class SpongeEconomy {

    private SpongeEconomy() {
    }

    /**
     * Hooks the economy up. Called only with the Sponge API present ({@link com.almuradev.almura.feature.economy.Economy}).
     */
    public static void install() {
        NeoForge.EVENT_BUS.register(new Hooks());
    }

    /**
     * @return the economy's accounts, or null without an economy service
     */
    public static @Nullable EconomyAccounts accounts() {
        if (!Sponge.isServerAvailable()) {
            return null;
        }
        return Sponge.server().serviceProvider().economyService().map(SpongeAccounts::new).orElse(null);
    }

    /**
     * Sends the player their balance, if the server has an economy.
     */
    static void send(final ServerPlayer player) {
        if (!Sponge.isServerAvailable()) {
            return;
        }
        final Optional<EconomyService> service = Sponge.server().serviceProvider().economyService();
        if (service.isEmpty()) {
            return;
        }
        final Currency currency = service.get().defaultCurrency();
        final BigDecimal balance = service.get().findOrCreateAccount(player.getUUID()).map(account -> account.balance(currency))
            .orElse(BigDecimal.ZERO);
        final String symbol = PlainTextComponentSerializer.plainText().serialize(currency.symbol());
        PacketDistributor.sendToPlayer(player, new ClientboundPlayerMoney(symbol, balance.doubleValue()));
    }

    /**
     * The NeoForge side: a player joining is sent their balance; once the server is up, Sponge's transactions are listened to.
     */
    public static final class Hooks {

        @SubscribeEvent
        public void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof final ServerPlayer player) {
                send(player);
            }
        }

        @SubscribeEvent
        public void onServerStarted(final ServerStartedEvent event) {
            final Optional<PluginContainer> almura = Sponge.pluginManager().plugin(Almura.ID);
            if (almura.isEmpty()) {
                Almura.LOGGER.warn("Sponge doesn't know Almura as a plugin; the HUD won't follow economy transactions");
                return;
            }
            Sponge.eventManager().registerListeners(almura.get(), new Transactions(), MethodHandles.lookup());
        }
    }

    /**
     * The Sponge side: a transaction on a player's account sends them their new balance, on the server's thread.
     */
    public static final class Transactions {

        @Listener
        public void onTransaction(final EconomyTransactionEvent event) {
            if (!(event.transactionResult().account() instanceof final UniqueAccount account)) {
                return;
            }
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return;
            }
            server.execute(() -> {
                final ServerPlayer player = server.getPlayerList().getPlayer(account.uniqueId());
                if (player != null) {
                    send(player);
                }
            });
        }
    }

    /**
     * The economy's accounts in its default currency, as 3.5's bank used them: a player's own, or a virtual one by name.
     */
    // Sponge's Account is named in full: inside this record Account is Almura's (EconomyAccounts.Account)
    private record SpongeAccounts(EconomyService service) implements EconomyAccounts {

        @Override
        public @Nullable BigDecimal balance(final Account account) {
            return this.find(account).map(found -> found.balance(this.service.defaultCurrency())).orElse(null);
        }

        @Override
        public boolean transfer(final Account from, final Account to, final BigDecimal amount) {
            final Optional<org.spongepowered.api.service.economy.account.Account> source = this.find(from);
            final Optional<org.spongepowered.api.service.economy.account.Account> target = this.find(to);
            return source.isPresent() && target.isPresent()
                && source.get().transfer(target.get(), this.service.defaultCurrency(), amount).result() == ResultType.SUCCESS;
        }

        @Override
        public boolean deposit(final Account to, final BigDecimal amount) {
            return this.find(to).map(found -> found.deposit(this.service.defaultCurrency(), amount).result() == ResultType.SUCCESS)
                .orElse(false);
        }

        private Optional<org.spongepowered.api.service.economy.account.Account> find(final Account account) {
            if (account.player() != null) {
                return this.service.findOrCreateAccount(account.player()).map(org.spongepowered.api.service.economy.account.Account.class::cast);
            }
            if (account.name() == null) {
                return Optional.empty();
            }
            return this.service.findOrCreateAccount(account.name());
        }
    }
}
