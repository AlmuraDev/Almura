/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.server;

import com.almuradev.almura.feature.notification.network.ClientboundPopupNotification;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;

/**
 * Popup notifications from the server, shown on the players' HUDs (in chat without it) and kept in their notification history, and the
 * command that sends them, as on 1.12:
 *
 * <pre>
 * /almura notify &lt;targets&gt; [seconds] &lt;title&gt; &lt;message&gt;
 * </pre>
 *
 * <p>The title is one word or a quoted phrase, the message the rest of the line; both take &amp; colour codes. Operators only, until
 * Almura's permissions are ported ({@code almura.command.notify} on 1.12).</p>
 */
public final class ServerNotifications {

    private static final int DEFAULT_SECONDS = 5;

    public static void sendPopup(final ServerPlayer player, final String title, final String message, final int secondsToLive) {
        PacketDistributor.sendToPlayer(player, new ClientboundPopupNotification(title, message, Math.max(0, secondsToLive)));
    }

    @SubscribeEvent
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("almura")
            .then(Commands.literal("notify")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("seconds", IntegerArgumentType.integer(0, 600))
                        .then(Commands.argument("title", StringArgumentType.string())
                            .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> notify(context, IntegerArgumentType.getInteger(context, "seconds"))))))
                    .then(Commands.argument("title", StringArgumentType.string())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                            .executes(context -> notify(context, DEFAULT_SECONDS)))))));
    }

    private static int notify(final CommandContext<CommandSourceStack> context, final int seconds) throws CommandSyntaxException {
        final Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        final String title = colours(StringArgumentType.getString(context, "title"));
        final String message = colours(StringArgumentType.getString(context, "message"));
        for (final ServerPlayer player : targets) {
            sendPopup(player, title, message, seconds);
        }
        context.getSource().sendSuccess(() -> Component.literal("Sent the notification to " + targets.size() + " player"
            + (targets.size() == 1 ? "" : "s") + "."), false);
        return targets.size();
    }

    // & colour codes as section signs, as 1.12's commands took them
    private static String colours(final String text) {
        return text.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }
}
