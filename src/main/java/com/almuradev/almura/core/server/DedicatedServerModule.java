/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.server;

import com.almuradev.almura.core.common.CommonModule;
import com.almuradev.almura.shared.plugin.Plugin;
import net.kyori.violet.AbstractModule;

/**
 * The root module for the server.
 */
public final class DedicatedServerModule extends AbstractModule {

    private final Plugin plugin;

    DedicatedServerModule(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        this.install(new CommonModule(this.plugin));
    }
}
