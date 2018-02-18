/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.text;

import com.almuradev.almura.feature.notification.ServerNotificationManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;

import javax.inject.Inject;

public final class GuideCommands {

    @Inject
    private static ServerPageManager manager;
    @Inject
    private static ServerNotificationManager notificationManager;

    public static CommandCallable generateGuideCommand() {
        return CommandSpec.builder()
                .permission("almura.guide.help")
                .description(Text.of("Guide Commands"))
                .executor((source, arguments) -> CommandResult.success())
                .child(generateGuideRefreshCommand(), "refresh")
                .child(generateGuideForceOpenCommand(), "force")
                .build();
    }

    private static CommandCallable generateGuideRefreshCommand() {
        return CommandSpec.builder()
                .permission("almura.guide.refresh")
                .description(Text.of("Refresh all guides"))
                .executor((source, arguments) -> {
                    if (source.hasPermission("almura.guide.refresh")) {
                        try {
                            manager.loadAndSyncPages();
                            source.sendMessage(Text.of("Pages reloaded"));
                        } catch (IOException e) {
                            throw new CommandException(Text.of("Failed to load pages!"), e);
                        }
                    }

                    return CommandResult.success();
                })
                .build();
    }

    private static CommandCallable generateGuideForceOpenCommand() {
        return CommandSpec.builder()
                .permission("almura.guide.force")
                .description(Text.of("Forces guide to open on specified player"))
                .arguments(
                        text(Text.of("target"), TextSerializers.FORMATTING_CODE, false),
                        optional(text(Text.of("guidename"), TextSerializers.FORMATTING_CODE, true)))
                .executor((source, arguments) -> {
                    final Text playerTarget = arguments.<Text>getOne("target").orElse(null);
                    final Text guideName = arguments.<Text>getOne("guidename").orElse(null);
                    try {
                        Player player = Sponge.getServer().getPlayer(playerTarget.toPlain()).get();  //ToDo: why does this throw an No Input exception if the player isn't found?
                        if (player.isOnline()) {
                            manager.openGuideForPlayer(player, 2, guideName == null ? null : guideName.toPlain());
                            notificationManager.sendPopupNotification(player, Text.of("Guide"), Text.of("Guide has been forced open by: ", TextColors.AQUA, source.getName()), 5);
                        }
                    } catch (Exception e) {
                        source.sendMessage(Text.of("Could not force open guide for: ", TextColors.AQUA, playerTarget, TextColors.WHITE, "."));
                    }
                    return CommandResult.success();
                })
                .build();
    }
}
