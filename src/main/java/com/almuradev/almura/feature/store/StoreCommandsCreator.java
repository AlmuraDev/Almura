/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.google.inject.Inject;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

final class StoreCommandsCreator {

    @Inject private static ServerStoreManager storeManager;
    @Inject private static ServerNotificationManager notificationManager;

    static CommandSpec createCommand() {
        return CommandSpec.builder()
            .permission(Almura.ID + ".store.help")
            .executor((src, args) -> CommandResult.success())
            .child(createManageCommand(), "manage")
            .child(createOpenCommand(), "open")
            .build();
    }

    private static CommandSpec createManageCommand() {
        return CommandSpec.builder()
            .description(Text.of("Request to manage stores"))
            .arguments(GenericArguments.optional(GenericArguments.requiringPermissionWeak(GenericArguments.player(Text.of("player")), Almura.ID + ".store.admin")))
            .permission(Almura.ID + ".store.manage")
            .executor((src, args) -> {
                final Player player = args.<Player>getOne("player").orElse((Player) src);
                if (player == null) {
                    return CommandResult.empty();
                }

                storeManager.openStoreManage(player);

                return CommandResult.success();
            })
            .build();
    }

    private static CommandSpec createOpenCommand() {
        return CommandSpec.builder()
            .description(Text.of("Request to open a store"))
            .arguments(GenericArguments.seq(GenericArguments.optional(GenericArguments.requiringPermissionWeak(GenericArguments.player(Text.of("player")), Almura.ID + ".store.admin")), GenericArguments.string(Text.of("id"))))
            .permission(Almura.ID + ".store.open")
            .executor((src, args) -> {
                final Player player = args.<Player>getOne("player").orElse((Player) src);

                final String id = args.<String>getOne("id").orElse(null);
                if (id == null) {
                    return CommandResult.empty();
                }

                final Store store = storeManager.getStore(id).orElse(null);
                if (store == null) {
                    notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of(TextColors.RED, id,
                            TextColors.RESET, " does not exist!"), 5);
                    return CommandResult.success();
                }

                if (!player.hasPermission(store.getPermission())) {
                    notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission!"), 5);
                    return CommandResult.success();
                }

                if (store.isHidden() && !player.hasPermission(Almura.ID + ".store.admin")) {
                    notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission!"), 5);
                    return CommandResult.success();
                }

                storeManager.openStoreSpecific(player, store);

                return CommandResult.success();
            })
            .build();
    }
}
