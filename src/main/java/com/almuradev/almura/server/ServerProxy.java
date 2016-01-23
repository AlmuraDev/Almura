/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server;

import com.almuradev.almura.CommonProxy;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

public final class ServerProxy extends CommonProxy {
    public static final String CLASSPATH = "com.almuradev.almura.server.ServerProxy";

    @Override
    protected void onGamePreInitialization(GamePreInitializationEvent event) {
        super.onGamePreInitialization(event);
    }
}
