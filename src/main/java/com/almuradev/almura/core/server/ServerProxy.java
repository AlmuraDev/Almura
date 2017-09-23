/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.server;

import com.almuradev.almura.CommonProxy;
import com.google.inject.Injector;

/**
 * The server bootstrap.
 */
public final class ServerProxy extends CommonProxy {

    @Override
    protected Injector createInjector(final Injector parent) {
        return parent.createChildInjector(new ServerModule());
    }
}
