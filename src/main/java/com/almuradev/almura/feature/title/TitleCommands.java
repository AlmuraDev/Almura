package com.almuradev.almura.feature.title;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;

public final class TitleCommands {

    @Inject
    private static TitleManager manager;

    public static CommandSpec generateRootCommand() {
        return CommandSpec.builder()
                .child(generateTitleCommand(), "title")
                .build();
    }

    private static CommandSpec generateTitleCommand() {
        return CommandSpec.builder()
                .permission("almura.command.title")
                .description(Text.of("Title Management"))
                .child(generateTitleRefreshCommand(), "refresh")
                .child(generateTitleReloadCommand(), "reload")
                .arguments(GenericArguments.playerOrSource(Text.of("player")))
                .executor((source, arguments) -> {
                    final Player player = arguments.<Player>getOne("player").orElse(null);

                    Text.Builder message;

                    final Set<Text> titles = manager.getTitlesFor(player);

                    String prefix = "You have";
                    if (!source.equals(player)) {
                        prefix = player.getName() + " has";
                    }
                    if (titles.isEmpty()) {
                        message = Text.builder(prefix + " no titles available.");
                    } else {
                        message = Text.builder(prefix + " the following titles:");
                        for (Text title : titles) {
                            message
                                    .append(Text.NEW_LINE)
                                    .append(Text.of(" • ", title));
                        }
                    }

                    source.sendMessage(message.build());

                    return CommandResult.success();
                })
                .build();
    }

    private static CommandSpec generateTitleReloadCommand() {
        return CommandSpec.builder()
                .permission("almura.command.title.reload")
                .description(Text.of("Reloads title configuration"))
                .executor((source, arguments) -> {
                    try {
                        manager.loadTitles();

                        source.sendMessage(Text.of(manager.getTitles().size() + " title(s) loaded."));
                    } catch (IOException e) {
                        throw new CommandException(Text.of("Failed to reload title configuration!", e));
                    }

                    return CommandResult.success();
                })
                .build();
    }

    private static CommandSpec generateTitleRefreshCommand() {
        return CommandSpec.builder()
                .permission("almura.command.title.refresh")
                .description(Text.of("Triggers a refresh of all titles"))
                .executor((source, arguments) -> {
                    manager.refreshTitles();
                    source.sendMessage(Text.of("Refreshed all titles"));
                    return CommandResult.success();
                })
                .build();
    }
}
