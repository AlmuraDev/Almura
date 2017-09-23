/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.server;

import com.almuradev.almura.core.common.CommonModule;
import net.kyori.violet.AbstractModule;

/**
 * The root module for the server.
 */
public final class ServerModule extends AbstractModule {

    @Override
    protected void configure() {
        this.install(new CommonModule());
        // ServerConfiguration is installed in CommonModule
    }
}
