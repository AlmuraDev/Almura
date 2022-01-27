/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.google.inject.Inject;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

final class ExchangeCommandsCreator {

    @Inject private static ServerExchangeManager exchangeManager;
    @Inject private static ServerNotificationManager notificationManager;

    static CommandSpec createCommand() {
        return CommandSpec.builder()
            .permission(Almura.ID + ".exchange.help")
            .executor((src, args) -> CommandResult.success())
            .child(createManageCommand(), "manage")
            .child(createOpenCommand(), "open")
            .build();
    }

    private static CommandSpec createManageCommand() {
        return CommandSpec.builder()
            .description(Text.of("Request to manage exchanges"))
            .arguments(GenericArguments.optionalWeak(
                GenericArguments.requiringPermissionWeak(GenericArguments.player(Text.of("player")), Almura.ID + ".exchange.admin"))
            )
            .permission(Almura.ID + ".exchange.manage")
            .executor((src, args) -> {
                final Player player = args.<Player>getOne("player").orElse((Player) src);
                if (player == null) {
                    return CommandResult.empty();
                }

                exchangeManager.openExchangeManage(player);

                return CommandResult.success();
            })
            .build();
    }

    private static CommandSpec createOpenCommand() {
        return CommandSpec.builder()
            .description(Text.of("Request to open an exchange"))
            .arguments(GenericArguments.seq(
                GenericArguments.optionalWeak(
                    GenericArguments.requiringPermissionWeak(GenericArguments.player(Text.of("player")), Almura.ID + ".exchange.admin")),
                GenericArguments.string(Text.of("id"))
            ))
            .permission(Almura.ID + ".exchange.open")
            .executor((src, args) -> {
                final Player player = args.<Player>getOne("player").orElse((Player) src);

                final String id = args.<String>getOne("id").orElse(null);
                if (id == null) {
                    return CommandResult.empty();
                }

                final Exchange axs = exchangeManager.getExchange(id).orElse(null);
                if (axs == null) {
                    notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Exchange"), Text.of(TextColors.RED, id,
                            TextColors.RESET, " does not exist!"), 5);
                    return CommandResult.success();
                }

                if (!player.hasPermission(axs.getPermission())) {
                    notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Exchange"), Text.of("You do not have permission!"), 5);
                    return CommandResult.success();
                }

                if (axs.isHidden() && !player.hasPermission(Almura.ID + ".exchange.admin")) {
                    notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Exchange"), Text.of("You do not have permission!"), 5);
                    return CommandResult.success();
                }

                exchangeManager.openExchangeSpecific(player, axs);

                return CommandResult.success();
            })
            .build();
    }
}
