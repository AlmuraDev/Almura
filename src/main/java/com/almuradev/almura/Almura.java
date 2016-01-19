/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.pack.Pack;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelRegistrationException;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;

@Plugin(id = Almura.PLUGIN_ID, name = Almura.PLUGIN_NAME, version = Almura.PLUGIN_VERSION)
public class Almura {

    public static final String PLUGIN_ID = "almura", PLUGIN_NAME = "Almura", PLUGIN_VERSION = "1.7.10-1694",
        GUI_VERSION = "b200", PACK_VERSION = "1.4";
    public static final Pack INTERNAL_PACK = new Pack("internal");

    private static Almura instance;

    public static Almura getInstance() {
        checkNotNull(instance, "Almura has not been loaded yet!");
        return instance;
    }

    @Inject public Logger logger;
    public ChannelBinding.IndexedMessageChannel network;

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        instance = this;
        if (!Sponge.getGame().getChannelRegistrar().isChannelAvailable("AM")) {
            throw new ChannelRegistrationException("Some other mod/plugin has registered Almura's networking channel 'AM'");
        }
        network = Sponge.getGame().getChannelRegistrar().createChannel(this, "AM");
    }
}
