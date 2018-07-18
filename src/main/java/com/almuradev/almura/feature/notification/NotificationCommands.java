/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification;

import static org.spongepowered.api.command.args.GenericArguments.bool;
import static org.spongepowered.api.command.args.GenericArguments.integer;
import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.playerOrSource;
import static org.spongepowered.api.command.args.GenericArguments.text;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;

public final class NotificationCommands {

    @Inject
    private static ServerNotificationManager manager;

    public static CommandSpec generateNotificationCommand() {
        return CommandSpec.builder()
                .permission("almura.command.notify")
                .arguments(
                        optional(playerOrSource(Text.of("target"))),
                        optional(integer(Text.of("seconds_to_live"))),
                        optional(bool(Text.of("is_window"))),
                        text(Text.of("title"), TextSerializers.FORMATTING_CODE, false),
                        text(Text.of("notification"), TextSerializers.FORMATTING_CODE, true))
                .executor((source, args) -> {
                    final int secondsToLive = args.<Integer>getOne("seconds_to_live").orElse(5);
                    final boolean isWindow = args.<Boolean>getOne("is_window").orElse(false);
                    final Text title = args.<Text>getOne("title").orElse(null);
                    final Text notification = args.<Text>getOne("notification").orElse(null);
                    Collection<Player> targets = args.getAll("target");
                    if (targets.isEmpty()) {
                        targets = Sponge.getServer().getOnlinePlayers();
                    }
                    if (!targets.isEmpty()) {
                        targets.forEach((player) -> {
                            if (isWindow) {
                                manager.sendWindowMessage(player, title, notification);
                            } else {
                                manager.sendPopupNotification(player, title, notification, secondsToLive);
                            }
                        });
                    }

                    return CommandResult.success();
                })
                .build();
    }
}
