/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death;

import com.almuradev.almura.feature.hud.network.ClientboundPlayerCurrencyPacket;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.core.event.Witness;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.Random;

import javax.inject.Inject;

@SideOnly(Side.SERVER)
public final class DeathHandler implements Witness {

    @Inject private static ServerNotificationManager serverNotificationManager;

    @Listener(order = Order.LAST)
    public void onPlayerDeath(DestructEntityEvent.Death event, @Root DamageSource damageSource) {
        if (!(event.getTargetEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getTargetEntity();
        final Server server = Sponge.getServer();

        if (!areCoinsLoaded()) {
            return;
        }

        if (player.hasPermission("almura.admin")) {
            return;
        }

        final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
        if (service == null) {
            return;
        }

        if (service != null) {
            final double deathTax = getDropAmountMultiple();
            final Account account = service.getOrCreateAccount(player.getUniqueId()).orElse(null);
            BigDecimal balance = BigDecimal.ZERO;

            if (account != null) {
                final Currency currency = service.getDefaultCurrency();
                balance = account.getBalance(currency);

                final double dropAmount = balance.doubleValue() - (balance.doubleValue() * deathTax);
                final BigDecimal deduct = new BigDecimal(dropAmount);
                account.withdraw(currency, deduct, null);

                server.getOnlinePlayers().forEach(onlinePlayer -> {
                    if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                        // Display nothing, you have already been shamed...
                    } else {
                        serverNotificationManager.sendPopupNotification(onlinePlayer, Text.of(player.getName() + "has died!"), Text.of("Their death has cost them $" + dropAmount + ", such a waste..."), 5);
                    }
                });
            }
        }
    }

    protected double getDropAmountMultiple() {
        final Random random = new Random();
        final String raw = "25-75";
        final String[] parsed = raw.split("-");
        double lower, upper = 0;

        try {
            lower = Double.parseDouble(parsed[0]);
        } catch (Exception e) {
            lower = 0;
        }

        if (parsed.length == 2) {
            try {
                upper = Double.parseDouble(parsed[1]);
            } catch (Exception e) {
                upper = 0;
            }
        }

        return (lower + (upper - lower) * random.nextDouble()) / 100;
    }

    protected boolean areCoinsLoaded() {
        if (GameRegistry.makeItemStack("almura:normal/currency/platinumcoin", 0, 64, null) == null)
            return false;
        if (GameRegistry.makeItemStack("almura:normal/currency/goldcoin", 0, 64, null) == null)
            return false;
        if (GameRegistry.makeItemStack("almura:normal/currency/silvercoin", 0, 64, null) == null)
            return false;
        if (GameRegistry.makeItemStack("almura:normal/currency/coppercoin", 0, 64, null) == null)
            return false;
        return true;
    }

    protected double dropAmount(EntityPlayer player, double amount) {
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
                ItemStack coins = GameRegistry.makeItemStack("almura:normal/currency/platinumcoin", 0, 64, null);
                dropItems(player, coins);
                platinum-=64;
            } else {
                ItemStack coins = GameRegistry.makeItemStack("almura:normal/currency/platinumcoin", platinum, 64, null);
                dropItems(player, coins);
                break;
            }
        }

        while (gold > 0) {
            if (gold > 64) {
                ItemStack coins = GameRegistry.makeItemStack("almura:normal/currency/goldcoin", 0, 64, null);
                dropItems(player, coins);
                gold-=64;
            } else {
                ItemStack coins = GameRegistry.makeItemStack("almura:normal/currency/goldcoin", gold, 64, null);
                dropItems(player, coins);
                break;
            }
        }

        while (silver > 0) {
            if (silver > 64) {
                ItemStack coins = GameRegistry.makeItemStack("almura:normal/currency/silvercoin", 0, 64, null);
                dropItems(player, coins);
                silver-=64;
            } else {
                ItemStack coins = GameRegistry.makeItemStack("almura:normal/currency/silvercoin", silver, 64, null);
                dropItems(player, coins);
                break;
            }
        }

        while (copper > 0) {
            if (copper > 64) {
                ItemStack coins = GameRegistry.makeItemStack("almura:normal/currency/coppercoin", 0, 64, null);
                dropItems(player, coins);
                copper-=64;
            } else {
                ItemStack coins = GameRegistry.makeItemStack("almura:normal/currency/coppercoin", copper, 64, null);
                dropItems(player, coins);
                break;
            }
        }

        return remainingMoney;
    }

    protected void dropItems(EntityPlayer player, ItemStack itemStack) {
        EntityItem item = new EntityItem(player.world, player.posX, player.posY, player.posZ, itemStack);
        player.world.spawnEntity(item);
    }
}
