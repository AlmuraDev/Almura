/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.configuration.AbstractConfiguration;
import com.almuradev.almura.configuration.ConfigurationAdapter;

/**
 * The dedicated server platform of Almura. All code meant to only run when this isn't on the client at all should go here.
 */
public final class ServerProxy extends CommonProxy {
    public static final String CLASSPATH = "com.almuradev.almura.server.ServerProxy";

    @Override
    public ConfigurationAdapter<? extends AbstractConfiguration> getConfigAdapter() {
        return null;
    }
}
