/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.api.CreativeTab;
import com.almuradev.almura.api.block.BuildableBlockType;
import com.almuradev.almura.block.builder.AbstractBlockTypeBuilder;
import com.almuradev.almura.configuration.AbstractConfiguration;
import com.almuradev.almura.configuration.ConfigurationAdapter;
import com.almuradev.almura.network.play.SWorldInformationMessage;
import com.almuradev.almura.pack.PackManager;
import com.almuradev.almura.registry.CreativeTabRegistryModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelRegistrationException;
import org.spongepowered.api.world.World;

/**
 * The common platform of Almura. All code meant to run on the client and both dedicated and integrated server go here.
 */
public abstract class CommonProxy {

    protected ChannelBinding.IndexedMessageChannel network;

    protected void onGamePreInitialization(GamePreInitializationEvent event) {
        if (!Sponge.getGame().getChannelRegistrar().isChannelAvailable("AM|FOR")) {
            throw new ChannelRegistrationException("Some other mod/plugin has registered Almura's networking channel [AM|FOR]");
        }

        network = Sponge.getGame().getChannelRegistrar().createChannel(Almura.instance.container, "AM|FOR");
        registerMessages();

        Sponge.getRegistry().registerModule(CreativeTab.class, CreativeTabRegistryModule.getInstance());
        Sponge.getRegistry().registerBuilderSupplier(BuildableBlockType.Builder.class, AbstractBlockTypeBuilder.BuilderImpl::new);

        Sponge.getEventManager().registerListeners(Almura.instance.container, this);

        FileSystem.init();

        // TODO Fix CreativeTabs registering too late
        PackManager.loadPacks(FileSystem.PATH_CONFIG_PACKS);
    }

    @Listener(order = Order.LAST)
    public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
        sendWorldHUDData(event.getTargetEntity(), event.getTargetEntity().getTransform());
    }

    @Listener(order = Order.LAST)
    public void onMoveEntity(MoveEntityEvent.Teleport event, @Getter("getTargetEntity") Player player) {
        if (!event.getFromTransform().getExtent().equals(event.getToTransform().getExtent())) {
            sendWorldHUDData(player, event.getToTransform());
        }
    }

    @Listener(order = Order.LAST)
    public void onRespawnPlayer(RespawnPlayerEvent event) {
        if (!event.getFromTransform().getExtent().equals(event.getToTransform().getExtent())) {
            sendWorldHUDData(event.getTargetEntity(), event.getToTransform());
        }
    }

    protected void registerMessages() {
        this.network.registerMessage(SWorldInformationMessage.class, 0);
    }

    public abstract ConfigurationAdapter<? extends AbstractConfiguration> getConfigAdapter();

    private void sendWorldHUDData(Player player, Transform<World> toTransform) {
        String clientWorldName = toTransform.getExtent().getName();

        switch (clientWorldName) {
            case "DIM-1":
                clientWorldName = "The Nether";
                break;
            case "DIM1":
                clientWorldName = "The End";
                break;
        }
        this.network.sendTo(player, new SWorldInformationMessage(clientWorldName));
    }
}
