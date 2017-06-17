/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.block.BuildableBlockType;
import com.almuradev.almura.block.rotatable.HorizontalType;
import com.almuradev.almura.content.loader.stage.LoadCreativeTabsStage;
import com.almuradev.almura.content.loader.stage.LoadMaterialsStage;
import com.almuradev.almura.creativetab.CreativeTab;
import com.almuradev.almura.block.builder.AbstractBlockTypeBuilder;
import com.almuradev.almura.block.builder.rotatable.HorizontalTypeBuilderImpl;
import com.almuradev.almura.configuration.AbstractConfiguration;
import com.almuradev.almura.configuration.MappedConfigurationAdapter;
import com.almuradev.almura.content.loader.AssetLoader;
import com.almuradev.almura.creativetab.CreativeTabBuilderImpl;
import com.almuradev.almura.util.NetworkUtil;
import com.almuradev.almura.network.play.SServerInformationMessage;
import com.almuradev.almura.network.play.SWorldInformationMessage;
import com.almuradev.almura.registry.CreativeTabRegistryModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelRegistrationException;

import java.io.IOException;

/**
 * The common platform of Almura. All code meant to run on the client and both dedicated and integrated server go here.
 */
public abstract class CommonProxy {

    protected ChannelBinding.IndexedMessageChannel network;
    protected AssetLoader assetLoader;

    protected void onGameConstruction(GameConstructionEvent event) {
        if (!Sponge.getGame().getChannelRegistrar().isChannelAvailable("AM|FOR")) {
            throw new ChannelRegistrationException("Some other mod/plugin has registered Almura's networking channel [AM|FOR]");
        }

        this.loadConfig();

        this.network = Sponge.getGame().getChannelRegistrar().createChannel(Almura.instance.container, "AM|FOR");
        this.assetLoader = new AssetLoader();

        this.registerFileSystem();
        this.registerMessages();
        this.registerModules();
        this.registerBuilders();
        this.registerLoaderStages();
        this.registerListeners();
    }

    protected void onGamePreInitialization(GamePreInitializationEvent event) {
        try {
            this.assetLoader.buildAssets(Constants.FileSystem.PATH_ASSETS_ALMURA_30_PACKS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract MappedConfigurationAdapter<? extends AbstractConfiguration> getPlatformConfigAdapter();

    public ChannelBinding.IndexedMessageChannel getNetwork() {
        return this.network;
    }

    protected void loadConfig() {
    }

    protected void registerFileSystem() {
    }

    protected void registerMessages() {
        this.network.registerMessage(SWorldInformationMessage.class, 0);
        this.network.registerMessage(SServerInformationMessage.class, 1);
    }

    protected void registerModules() {
        Sponge.getRegistry().registerModule(CreativeTab.class, CreativeTabRegistryModule.getInstance());
    }

    protected void registerBuilders() {
        Sponge.getRegistry().registerBuilderSupplier(CreativeTab.Builder.class, CreativeTabBuilderImpl::new);
        Sponge.getRegistry().registerBuilderSupplier(BuildableBlockType.Builder.class, AbstractBlockTypeBuilder.BuilderImpl::new);
        Sponge.getRegistry().registerBuilderSupplier(HorizontalType.Builder.class, HorizontalTypeBuilderImpl::new);
    }

    protected void registerLoaderStages() {
        this.assetLoader.registerLoaderStage(LoadCreativeTabsStage.instance);
        this.assetLoader.registerLoaderStage(LoadMaterialsStage.instance);
    }

    protected void registerListeners() {
        Sponge.getEventManager().registerListeners(Almura.instance.container, this);
    }

    @Listener(order = Order.LAST)
    public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
        NetworkUtil.sendWorldHUDData(event.getTargetEntity(), event.getTargetEntity().getTransform());

        SServerInformationMessage message = new SServerInformationMessage(Sponge.getServer().getOnlinePlayers().size(),
                Sponge.getServer().getMaxPlayers());

        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            this.network.sendTo(player, message);
        }
        this.network.sendTo(event.getTargetEntity(), message);
    }

    @Listener(order = Order.LAST)
    public void onClientConnectLeave(ClientConnectionEvent.Disconnect event) {
        NetworkUtil.sendWorldHUDData(event.getTargetEntity(), event.getTargetEntity().getTransform());
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (player == event.getTargetEntity()) {
                continue;
            }
            this.network.sendTo(player, new SServerInformationMessage(Sponge.getServer().getOnlinePlayers().size(),
                    Sponge.getServer().getMaxPlayers()));
        }
    }

    @Listener(order = Order.LAST)
    public void onMoveEntity(MoveEntityEvent.Teleport event, @Getter("getTargetEntity") Player player) {
        if (!event.getFromTransform().getExtent().equals(event.getToTransform().getExtent())) {
            NetworkUtil.sendWorldHUDData(player, event.getToTransform());
        }
    }

    @Listener(order = Order.LAST)
    public void onRespawnPlayer(RespawnPlayerEvent event) {
        if (!event.getFromTransform().getExtent().equals(event.getToTransform().getExtent())) {
            NetworkUtil.sendWorldHUDData(event.getTargetEntity(), event.getToTransform());
        }
    }
}
