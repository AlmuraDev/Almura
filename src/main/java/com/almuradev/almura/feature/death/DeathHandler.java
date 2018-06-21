/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death;

import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.core.event.Witness;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.Random;

import javax.inject.Inject;

@SideOnly(Side.SERVER)
public final class DeathHandler implements Witness {

    private static final Random RANDOM = new Random();
    private static final DoubleRange RANGE = DoubleRange.range(25, 75);

    private boolean checkedItemsLoaded = false;
    private ItemType platinumCoin, goldCoin, silverCoin, copperCoin;

    private final ServerNotificationManager serverNotificationManager;

    @Inject
    public DeathHandler(final ServerNotificationManager serverNotificationManager) {
        this.serverNotificationManager = serverNotificationManager;
    }

    @Listener(order = Order.LAST)
    public void onPlayerDeath(final DestructEntityEvent.Death event, @Root final DamageSource damageSource, @Getter("getTargetEntity") final Player player) {
        if (player.hasPermission("almura.admin")) {
            return;
        }

        this.cacheItemTypes();

        if (!this.areCoinsLoaded()) {
            return;
        }

        final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
        if (service == null) {
            return;
        }

        final Server server = Sponge.getServer();

        final double deathTax = RANGE.random(RANDOM);
        final Account account = service.getOrCreateAccount(player.getUniqueId()).orElse(null);
        BigDecimal balance;

        if (account != null) {
            final Currency currency = service.getDefaultCurrency();
            balance = account.getBalance(currency);

            final double dropAmount = balance.doubleValue() - (balance.doubleValue() * deathTax);
            final BigDecimal deduct = new BigDecimal(dropAmount);
            account.withdraw(currency, deduct, Sponge.getCauseStackManager().getCurrentCause());
            server.getOnlinePlayers().forEach(onlinePlayer -> {
                if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                    // Display nothing, you have already been shamed...
                } else {
                    // TODO Dockter you can do better here, have a list of witty phrases to troll players with
                    serverNotificationManager.sendPopupNotification(onlinePlayer, Text.of(player.getName() + "has died!"), Text.of("Their death has cost them $" + dropAmount + ", such a waste..."), 5);
                }
            });
        }
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

    private double dropAmount(Player player, double amount) {
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
