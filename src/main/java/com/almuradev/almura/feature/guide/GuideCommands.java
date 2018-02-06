/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.io.IOException;

import javax.inject.Inject;

public final class GuideCommands {

    @Inject
    private static ServerPageManager manager;

    public static CommandCallable generateGuideCommand() {
        return CommandSpec.builder()
                .permission("almura.guide.help")
                .description(Text.of("Guide Commands"))
                .executor((source, arguments) -> CommandResult.success())
                .child(generateGuideRefreshCommand(), "refresh")
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
}
