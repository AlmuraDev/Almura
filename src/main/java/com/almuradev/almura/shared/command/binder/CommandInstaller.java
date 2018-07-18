/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.command.binder;

import net.kyori.membrane.facet.Enableable;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Set;

import javax.inject.Inject;

public final class CommandInstaller implements Enableable {

    private final PluginContainer container;
    private final CommandManager manager;
    private final Set<RootCommandEntry> root;
    private final Set<ChildCommandEntry> children;

    @Inject
    private CommandInstaller(final PluginContainer container, final CommandManager manager, final Set<RootCommandEntry> root, final Set<ChildCommandEntry> children) {
        this.container = container;
        this.manager = manager;
        this.root = root;
        this.children = children;
    }

    @Override
    public void enable() {
        this.registerRoot();
        this.registerChildren();
    }

    private void registerRoot() {
        this.root.forEach((entry) -> this.manager.register(this.container, entry.callable, entry.aliases));
    }

    private void registerChildren() {
        final CommandSpec.Builder builder = CommandSpec.builder()
                .permission("almura.command.help")
                .executor((src, arguments) -> CommandResult.success())
                .description(Text.of("Almura commands"));
        this.children.forEach((entry) -> entry.insert(builder));
        this.manager.register(this.container, builder.build(), "almura", "am");
    }

    @Override
    public void disable() {
        // TODO(kashike): removal?
    }
}
