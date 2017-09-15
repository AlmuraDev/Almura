/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
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
    }
}
