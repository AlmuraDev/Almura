/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.network.play.SWorldInformationMessage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelRegistrationException;

/**
 * The common platform of Almura. All code meant to run on the client and both dedicated and integrated server go here.
 */
public class CommonProxy {
    public ChannelBinding.IndexedMessageChannel network;

    protected void onGamePreInitialization(GamePreInitializationEvent event) {
        if (!Sponge.getGame().getChannelRegistrar().isChannelAvailable("AM|FOR")) {
            throw new ChannelRegistrationException("Some other mod/plugin has registered Almura's networking channel 'AM|FOR'");
        }

        network = Sponge.getGame().getChannelRegistrar().createChannel(Almura.instance.container, "AM|FOR");
        registerMessages();
        Sponge.getGame().getEventManager().registerListeners(Almura.instance.container, this);
    }

    protected void registerMessages() {
        this.network.registerMessage(SWorldInformationMessage.class, 0);
    }

    @Listener(order = Order.LAST)
    public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
        this.network.sendTo(event.getTargetEntity(), new SWorldInformationMessage(event.getTargetEntity().getWorld().getName()));
    }

    @Listener(order = Order.LAST)
    public void onDisplaceTeleportPlayer(DisplaceEntityEvent.Teleport.TargetPlayer event) {
        this.network.sendTo(event.getTargetEntity(), new SWorldInformationMessage(event.getToTransform().getExtent().getName()));
    }
}
