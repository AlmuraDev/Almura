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

public final class ExchangeCommandsCreator {

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

    static CommandSpec createManageCommand() {
        return CommandSpec.builder()
            .description(Text.of("Request to manage exchanges"))
            .arguments(GenericArguments.playerOrSource(Text.of("player")))
            .permission(Almura.ID + ".exchange.manage")
            .executor((src, args) -> {
                final Player player = args.<Player>getOne("player").orElse(null);
                if (player == null) {
                    return CommandResult.empty();
                }

                exchangeManager.openExchangeManage(player);

                return CommandResult.success();
            })
            .build();
    }

    static CommandSpec createOpenCommand() {
        return CommandSpec.builder()
            .description(Text.of("Request to open an exchange"))
            .arguments(GenericArguments.seq(GenericArguments.playerOrSource(Text.of("player")), GenericArguments.string(Text.of("id"))))
            .permission(Almura.ID + ".exchange.open")
            .executor((src, args) -> {
                final Player player = args.<Player>getOne("player").orElse(null);
                if (player == null) {
                    return CommandResult.empty();
                }

                final String id = args.<String>getOne("id").orElse(null);
                if (id == null) {
                    return CommandResult.empty();
                }

                // TODO These can use notifications
                final Exchange axs = exchangeManager.getExchange(id).orElse(null);
                if (axs == null) {
                    src.sendMessage(Text.of("Exchange ", TextColors.RED, id, TextColors.RESET, " does not exist!"));
                    return CommandResult.success();
                }

                // TODO These can use notifications
                if (!player.hasPermission(axs.getPermission())) {
                    src.sendMessage(Text.of("You do not have permission!"));
                    return CommandResult.success();
                }

                exchangeManager.openExchangeSpecific(player, axs);

                return CommandResult.success();
            })
            .build();
    }
}
