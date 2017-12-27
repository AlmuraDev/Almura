/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.command.binder;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.kyori.membrane.facet.Enableable;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Set;

public final class CommandInstaller implements Enableable {

    private final Injector injector;
    private final PluginContainer container;
    private final CommandManager manager;
    private final Set<CommandEntry> commands;

    @Inject
    public CommandInstaller(final Injector injector, PluginContainer container, CommandManager manager, final Set<CommandEntry> commands) {
        this.injector = injector;
        this.container = container;
        this.manager = manager;
        this.commands = commands;
    }

    @Override
    public void enable() {
        final CommandSpec.Builder rootCommand = CommandSpec.builder()
                .permission("almura.command.help")
                .executor((src, arguments) -> CommandResult.success())
                .description(Text.of("Almura commands"));
        this.commands.forEach((entry) -> entry.install(this.injector, rootCommand));
        this.manager.register(container, rootCommand.build(), "almura", "am");
    }

    @Override
    public void disable() {
        // Commands cannot be removed
    }
}
