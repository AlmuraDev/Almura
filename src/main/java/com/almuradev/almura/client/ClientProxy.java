/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.network.NetworkHandlers;
import com.almuradev.almura.network.play.S00WorldInformation;
import org.spongepowered.api.Platform;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

public class ClientProxy extends CommonProxy {
    public static final String CLASSPATH = "com.almuradev.almura.client.ClientProxy";

    @Override
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        super.onGamePreInitialization(event);
    }

    @Override
    public void registerMessages() {
        super.registerMessages();
        this.network.addHandler(S00WorldInformation.class, Platform.Type.CLIENT, new NetworkHandlers.S00WorldInformationHandler());
    }
}
