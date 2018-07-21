/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death;

import com.almuradev.almura.feature.death.network.ClientboundPlayerDiedPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
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

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;

public final class DeathHandler implements Witness {

    private static final Random RANDOM = new Random();
    private static final DoubleRange RANGE = DoubleRange.range(25, 75);

    private boolean checkedItemsLoaded = false;
    private ItemType platinumCoin, goldCoin, silverCoin, copperCoin;

    private final ServerNotificationManager serverNotificationManager;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public DeathHandler(final ServerNotificationManager serverNotificationManager, final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.serverNotificationManager = serverNotificationManager;
        this.network = network;
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        if (player.health().get() == 0) { // Catch if the player is login into server and is currently dead.
            this.network.sendTo(player, new ClientboundPlayerDiedPacket());
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerDeath(final DestructEntityEvent.Death event, @Root final DamageSource damageSource, @Getter("getTargetEntity") final Player player) {
        this.cacheItemTypes();
        final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);

        if (service != null) {
            final Server server = Sponge.getServer();
            final double deathTax = RANGE.random(RANDOM);
            final Account account = service.getOrCreateAccount(player.getUniqueId()).orElse(null);
            BigDecimal balance;

            if (account != null && this.areCoinsLoaded()) {
                final Currency currency = service.getDefaultCurrency();

                double deathTaxAmount = 0;
                double droppedAmount = 0;
                boolean displayDrops = false;
                final DecimalFormat dFormat = new DecimalFormat("###,###,###,###.##");
                balance = account.getBalance(currency);

                if (balance.doubleValue() > 0) {
                    double dropAmount = balance.doubleValue() - (balance.doubleValue() * (deathTax / 100));

                    if (dropAmount > balance.doubleValue()) {
                        dropAmount = balance.doubleValue();
                    }

                    BigDecimal deduct = new BigDecimal(dropAmount);
                    account.withdraw(currency, deduct, Sponge.getCauseStackManager().getCurrentCause());

                    deathTaxAmount = this.dropAmountReturnChange(player, dropAmount);
                    droppedAmount = (dropAmount - deathTaxAmount);
                    displayDrops = true;
                }

                final double finalDroppedAmount = droppedAmount;
                final double finalDeathTaxAmount = deathTaxAmount;
                final boolean finalDisplayDrops = displayDrops;

                server.getOnlinePlayers().forEach(onlinePlayer -> {
                    if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                        this.network.sendTo(player, new ClientboundPlayerDiedPacket(finalDroppedAmount, finalDeathTaxAmount, finalDisplayDrops));
                    } else {
                        //ToDo: broke atm.
                        serverNotificationManager.sendPopupNotification(onlinePlayer, Text.of(player.getName() + " has died!"), Text.of("Dropped: " + TextFormatting.GOLD + "$" + dFormat.format(finalDroppedAmount) + TextFormatting.RESET + " and "
                                + "lost: "+ TextFormatting.RED + "$" + dFormat.format(finalDeathTaxAmount) + TextFormatting.RESET + " to death taxes."),5);
                    }
                });
                return;
            }
        }

        // Service or Account was null, fallback.
        this.network.sendTo(player, new ClientboundPlayerDiedPacket());
    }

    private void cacheItemTypes() {
        if (!this.checkedItemsLoaded) {
            final GameRegistry registry = Sponge.getRegistry();

            this.platinumCoin = registry.getType(ItemType.class, "almura:normal/currency/platinumcoin").orElse(null);
            this.goldCoin = registry.getType(ItemType.class, "almura:normal/currency/goldcoin").orElse(null);
            this.silverCoin = registry.getType(ItemType.class, "almura:normal/currency/silvercoin").orElse(null);
            this.copperCoin = registry.getType(ItemType.class, "almura:normal/currency/coppercoin").orElse(null);

            this.checkedItemsLoaded = true;
        }
    }

    private boolean areCoinsLoaded() {
        return platinumCoin != null && goldCoin != null && silverCoin != null && copperCoin != null;
    }

    private double dropAmountReturnChange(Player player, double amount) {
        double remainingMoney = amount;
        int platinum = (int) (remainingMoney / 1000000);
        remainingMoney -= platinum * 1000000;
        int gold = (int) (remainingMoney / 100000);
        remainingMoney -= gold * 100000;
        int silver = (int) (remainingMoney / 1000);
        remainingMoney -= silver * 1000;
        int copper = (int) (remainingMoney / 100);
        remainingMoney -= copper * 100;

        while (platinum > 0) {
            if (platinum > 64) {
                this.dropStack(player, ItemStack.of(this.platinumCoin, 64));
                platinum -= 64;
            } else {
                this.dropStack(player, ItemStack.of(this.platinumCoin, platinum));
                break;
            }
        }

        while (gold > 0) {
            if (gold > 64) {
                this.dropStack(player, ItemStack.of(this.goldCoin, 64));
                gold -= 64;
            } else {
                this.dropStack(player, ItemStack.of(this.goldCoin, gold));
                break;
            }
        }

        while (silver > 0) {
            if (silver > 64) {
                this.dropStack(player, ItemStack.of(this.silverCoin, 64));
                silver -= 64;
            } else {
                this.dropStack(player, ItemStack.of(this.silverCoin, silver));
                break;
            }
        }

        while (copper > 0) {
            if (copper > 64) {
                this.dropStack(player, ItemStack.of(this.copperCoin, 64));
                copper -= 64;
            } else {
                this.dropStack(player, ItemStack.of(this.copperCoin, copper));
                break;
            }
        }

        return remainingMoney;
    }

    private void dropStack(Player player, ItemStack itemStack) {
        final Item entity = (Item) player.getWorld().createEntity(EntityTypes.ITEM, player.getLocation().getPosition());
        entity.offer(Keys.REPRESENTED_ITEM, itemStack.createSnapshot());
        player.getWorld().spawnEntity(entity);
    }
}
