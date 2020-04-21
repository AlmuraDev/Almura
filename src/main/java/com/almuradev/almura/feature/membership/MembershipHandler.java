/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership;

import com.almuradev.almura.feature.membership.network.ClientboundMembershipGuiOpenPacket;
import com.almuradev.almura.feature.membership.network.ClientboundMembershipSuccessPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.feature.skills.SkillsHandler;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.luckperms.api.LuckPerms;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;

import javax.inject.Inject;

public final class MembershipHandler implements Witness {

    private final ServerNotificationManager serverNotificationManager;
    private final ChannelBinding.IndexedMessageChannel network;
    private final SkillsHandler skillsManager;
    private final CommandManager commandManager;

    @Inject
    public MembershipHandler(final ServerNotificationManager serverNotificationManager, final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network, final SkillsHandler skillHandler, final CommandManager commandManager) {
        this.serverNotificationManager = serverNotificationManager;
        this.network = network;
        this.skillsManager = skillHandler;
        this.commandManager = commandManager;
    }

    public void requestClientGui(Player player) {
        final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
        final LuckPerms permService = Sponge.getServiceManager().provide(LuckPerms.class).orElse(null);
        int currentMembershipLevel = -1;
        ///////////////////////////////////////////////
        //final String command = "lp user 380df991-f603-344c-a090-369bad2a924a parent set ancient";  // DEV User.
        //this.commandManager.process(Sponge.getServer().getConsole(), command);
        ///////////////////////////////////////////////

        if (player.hasPermission("almura.membership.gui.open")) {
            if (service != null && skillsManager != null && permService != null) {
                final Account account = service.getOrCreateAccount(player.getUniqueId()).orElse(null);
                BigDecimal balance;
                if (account != null) {
                    final Currency currency = service.getDefaultCurrency();
                    balance = account.getBalance(currency);

                    String currentGroup = permService.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup();
                    if (currentGroup.equalsIgnoreCase("default")) {
                        currentMembershipLevel = -1;
                        // should be impossible to reach this unless permissions system is screwed up.
                    } else if (currentGroup.equalsIgnoreCase("survivor")) {
                        currentMembershipLevel = 0;
                    } else if (currentGroup.equalsIgnoreCase("citizen")) {
                        currentMembershipLevel = 1;
                    } else if (currentGroup.equalsIgnoreCase("explorer")) {
                        currentMembershipLevel = 2;
                    } else if (currentGroup.equalsIgnoreCase("pioneer")) {
                        currentMembershipLevel = 3;
                    }

                    this.network.sendTo(player, new ClientboundMembershipGuiOpenPacket(player.hasPermission("almura.membership.admin"), skillsManager.getTotalSkillLevel(player), balance.doubleValue(), currentMembershipLevel));
                }
            } else {
                serverNotificationManager.sendWindowMessage(player, Text.of("Init Error"), Text.of("Economy Service offline!"));
            }
        } else {
            serverNotificationManager.sendPopupNotification(player, Text.of("Insufficient Permissions"), Text.of("Missing [almura.membership.gui.open]"), 5);
        }
    }

    public void handleMembershipPurchase(Player player, int newMembershipLevel) {
        // 1 = Citizen, 2 = Explorer, 3 = Pioneer
        // Typically only thing that fails here is if someone is running a hacked client.  This method double checks that.
        if (!player.hasPermission("almura.membership.purchase")) {
            serverNotificationManager.sendWindowMessage(player, Text.of("Permission Denied"), Text.of("Missing: almura.membership.purchase"));
            return;
        }

        final EconomyService econService = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
        final LuckPerms permService = Sponge.getServiceManager().provide(LuckPerms.class).orElse(null);

        if (econService != null && permService != null) {
            final Account account = econService.getOrCreateAccount(player.getUniqueId()).orElse(null);
            if (account != null) {
                BigDecimal balance;
                final Currency currency = econService.getDefaultCurrency();
                balance = account.getBalance(currency);
                double fee = 0;
                if (newMembershipLevel == 1) {
                    fee = 2500000;
                    if (!(balance.doubleValue() >= fee)) {
                        serverNotificationManager.sendWindowMessage(player, Text.of("Insufficient Funds"), Text.of("Insufficient Funds to purchase Citizen Membership."));
                        return;
                    }
                }
                if (newMembershipLevel == 2) {
                    fee = 5000000;
                    if (!(balance.doubleValue() >= fee)) {
                        serverNotificationManager.sendWindowMessage(player, Text.of("Insufficient Funds"), Text.of("Insufficient Funds to purchase Explorer Membership."));
                        return;
                    }
                }
                if (newMembershipLevel == 3) {
                    fee = 10000000;
                    if (!(balance.doubleValue() >= fee)) {
                        serverNotificationManager.sendWindowMessage(player, Text.of("Insufficient Funds"), Text.of("Insufficient Funds to purchase Pioneer Membership."));
                        return;
                    }
                }

                String currentGroup = permService.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup();
                String newMembership = "";
                if (newMembershipLevel == 1)
                    newMembership = "Citizen";
                if (newMembershipLevel == 2)
                    newMembership = "Explorer";
                if (newMembershipLevel == 3)
                    newMembership = "Pioneer";
                BigDecimal deduct = new BigDecimal(fee);
                final String command = "lp user " + player.getName() + " promote members";

                if (newMembershipLevel == 1 && currentGroup.equalsIgnoreCase("survivor")) {
                    this.commandManager.process(Sponge.getServer().getConsole(), command);
                    account.withdraw(currency, deduct, Sponge.getCauseStackManager().getCurrentCause());
                    this.network.sendTo(player, new ClientboundMembershipSuccessPacket(newMembership));
                    return;
                }
                if (newMembershipLevel == 2 && (currentGroup.equalsIgnoreCase("survivor") || currentGroup.equalsIgnoreCase("citizen"))) {
                    if (currentGroup.equalsIgnoreCase("survivor")) {
                        this.commandManager.process(Sponge.getServer().getConsole(), command);
                    }

                    this.commandManager.process(Sponge.getServer().getConsole(), command);
                    account.withdraw(currency, deduct, Sponge.getCauseStackManager().getCurrentCause());
                    this.network.sendTo(player, new ClientboundMembershipSuccessPacket(newMembership));
                    return;
                }
                if (newMembershipLevel == 3 && (currentGroup.equalsIgnoreCase("survivor") || currentGroup.equalsIgnoreCase("citizen") || currentGroup.equalsIgnoreCase("explorer"))) {
                    if (currentGroup.equalsIgnoreCase("survivor")) {
                        this.commandManager.process(Sponge.getServer().getConsole(), command);
                        this.commandManager.process(Sponge.getServer().getConsole(), command);
                    }
                    if (currentGroup.equalsIgnoreCase("citizen")) {
                        this.commandManager.process(Sponge.getServer().getConsole(), command);
                    }

                    this.commandManager.process(Sponge.getServer().getConsole(), command);
                    account.withdraw(currency, deduct, Sponge.getCauseStackManager().getCurrentCause());
                    this.network.sendTo(player, new ClientboundMembershipSuccessPacket(newMembership));
                    return;
                }

                // This should be impossible, leaving this debug line.
                System.out.println("Current Group: " + currentGroup + " Failed to upgrade, no path found");
            }
        } else {
            serverNotificationManager.sendWindowMessage(player, Text.of("Error"), Text.of("Economy or Permission Service offline!"));
        }
    }

    public void handleMembershipSkills(Player player, int newMembershipLevel) {
        if (skillsManager == null) {
            return;
        }

        int currentSkillLevel = skillsManager.getTotalSkillLevel(player);
        final LuckPerms permService = Sponge.getServiceManager().provide(LuckPerms.class).orElse(null);

        if (permService == null) {
            return;
        }
        final String currentGroup = permService.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup();
        final String command = "lp user " + player.getName() + " promote members";

        if (newMembershipLevel == 1 && currentSkillLevel > 250 && currentGroup.equalsIgnoreCase("survivor")) {
            this.commandManager.process(Sponge.getServer().getConsole(), command);
            serverNotificationManager.sendPopupNotification(player, Text.of("Membership Upgrade"), Text.of("You've been upgraded to: Citizen!"), 5);
            return;
        }
        if (newMembershipLevel == 2 && currentSkillLevel > 375 && (currentGroup.equalsIgnoreCase("survivor") || currentGroup.equalsIgnoreCase("citizen"))) {
            if (currentGroup.equalsIgnoreCase("survivor")) {
                this.commandManager.process(Sponge.getServer().getConsole(), command);
            }

            this.commandManager.process(Sponge.getServer().getConsole(), command);
            serverNotificationManager.sendPopupNotification(player, Text.of("Membership Upgrade"), Text.of("You've been upgraded to: Explorer!"), 5);
            return;
        }
        if (newMembershipLevel == 3 && currentSkillLevel > 400 && (currentGroup.equalsIgnoreCase("survivor") || currentGroup.equalsIgnoreCase("citizen") || currentGroup.equalsIgnoreCase("explorer"))) {
            if (currentGroup.equalsIgnoreCase("survivor")) {
                this.commandManager.process(Sponge.getServer().getConsole(), command);
                this.commandManager.process(Sponge.getServer().getConsole(), command);
            }
            if (currentGroup.equalsIgnoreCase("citizen")) {
                this.commandManager.process(Sponge.getServer().getConsole(), command);
            }

            this.commandManager.process(Sponge.getServer().getConsole(), command);
            serverNotificationManager.sendPopupNotification(player, Text.of("Membership Upgrade"), Text.of("You've been upgraded to: Pioneer!"), 5);
            return;
        }
        System.out.println("Exception: skills level check failed.  Player not upgraded. " + player.getName() + " Skill level: " + currentSkillLevel);
    }
}
