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
 * The dedicated server platform of Almura. All code meant to only run when this isn't on the client at all should go here.
 */
public final class ServerProxy extends CommonProxy {

    @Override
    protected Injector createInjector(final Injector parent) {
        return parent.createChildInjector(new ServerModule());
    }
}
