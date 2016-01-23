/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.network.play.S00WorldInformation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelRegistrationException;

public class CommonProxy {
    public ChannelBinding.IndexedMessageChannel network;

    protected void onGamePreInitialization(GamePreInitializationEvent event) {
        if (!Sponge.getGame().getChannelRegistrar().isChannelAvailable("AM|FOR")) {
            throw new ChannelRegistrationException("Some other mod/plugin has registered Almura's networking channel 'AM|FOR'");
        }

        network = Sponge.getGame().getChannelRegistrar().createChannel(Almura.getInstance().container, "AM|FOR");
        registerMessages();
    }

    protected void registerMessages() {
        this.network.registerMessage(S00WorldInformation.class, 0);
    }
}
