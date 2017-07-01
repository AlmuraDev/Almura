/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.configuration.AbstractConfiguration;
import com.almuradev.almura.configuration.MappedConfigurationAdapter;
import com.almuradev.almura.content.AssetType;
import com.almuradev.almura.content.loader.AssetLoader;
import com.almuradev.almura.content.loader.AssetPipeline;
import com.almuradev.almura.content.loader.AssetRegistry;
import com.almuradev.almura.content.block.sound.BlockSoundGroup;
import com.almuradev.almura.content.block.BuildableBlockType;
import com.almuradev.almura.content.block.builder.AbstractBlockTypeBuilder;
import com.almuradev.almura.content.block.builder.rotatable.HorizontalTypeBuilderImpl;
import com.almuradev.almura.content.block.rotatable.HorizontalType;
import com.almuradev.almura.content.block.sound.BlockSoundGroupBuilder;
import com.almuradev.almura.content.item.BuildableItemType;
import com.almuradev.almura.content.item.builder.AbstractItemTypeBuilder;
import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.item.group.ItemGroupBuilderImpl;
import com.almuradev.almura.content.loader.LoaderPhase;
import com.almuradev.almura.content.loader.task.SetBlockAttributesTask;
import com.almuradev.almura.content.loader.task.SetBlockSoundGroupAttributesTask;
import com.almuradev.almura.content.loader.task.SetItemAttributesTask;
import com.almuradev.almura.content.loader.task.SetItemGroupAttributesTask;
import com.almuradev.almura.content.loader.task.SetMaterialAttributesTask;
import com.almuradev.almura.content.material.MapColor;
import com.almuradev.almura.content.material.Material;
import com.almuradev.almura.network.play.SServerInformationMessage;
import com.almuradev.almura.network.play.SWorldInformationMessage;
import com.almuradev.almura.registry.BlockSoundGroupRegistryModule;
import com.almuradev.almura.registry.BossBarColorRegistryModule;
import com.almuradev.almura.registry.ItemGroupRegistryModule;
import com.almuradev.almura.registry.MapColorRegistryModule;
import com.almuradev.almura.registry.MaterialRegistryModule;
import com.almuradev.almura.util.NetworkUtil;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
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
    protected AssetRegistry assetRegistry;
    protected AssetPipeline assetPipeline;
    protected AssetLoader assetLoader;

    protected void onGameConstruction(GameConstructionEvent event) {
        if (!Sponge.getGame().getChannelRegistrar().isChannelAvailable(Constants.Plugin.NETWORK_CHANNEL)) {
            throw new ChannelRegistrationException("Some other mod/plugin has registered Almura's networking channel [" + Constants.Plugin.NETWORK_CHANNEL + ']');
        }

        this.loadConfig();

        this.network = Sponge.getGame().getChannelRegistrar().createChannel(Almura.instance.container, "AM|FOR");
        this.assetRegistry = new AssetRegistry();
        this.assetPipeline = new AssetPipeline();
        this.assetLoader = new AssetLoader(this.assetRegistry);

        this.registerFileSystem();
        this.registerMessages();
        this.registerRegistryModules();
        this.registerPipelineStages();
        this.registerBuilders();
        this.registerListeners();

        try {
            this.assetRegistry.loadAssetFiles(Constants.FileSystem.PATH_ASSETS_ALMURA_30_PACKS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.assetRegistry.loadAssetContextuals();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.assetPipeline.process(LoaderPhase.CONSTRUCTION, this.assetRegistry);
        this.assetLoader.registerSpongeOnlyCatalogTypes();
    }

    protected void onGamePreInitialization(GamePreInitializationEvent event) {

    }

    protected void onGameInitialization(GameInitializationEvent event) {

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

    protected void registerRegistryModules() {
        final GameRegistry registry = Sponge.getRegistry();
        registry.registerModule(new BossBarColorRegistryModule());
        registry.registerModule(BlockSoundGroup.class, new BlockSoundGroupRegistryModule());
        registry.registerModule(ItemGroup.class, ItemGroupRegistryModule.getInstance());
        registry.registerModule(MapColor.class, new MapColorRegistryModule());
        registry.registerModule(Material.class, new MaterialRegistryModule());
    }

    private void registerPipelineStages() {
        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, AssetType.BLOCK_SOUNDGROUP, SetBlockSoundGroupAttributesTask.class);

        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, AssetType.ITEMGROUP, SetItemGroupAttributesTask.class);

        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, AssetType.BLOCK, SetMaterialAttributesTask.class);
        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, AssetType.BLOCK, SetBlockAttributesTask.class);

        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, AssetType.HORIZONTAL_BLOCK, SetMaterialAttributesTask.class);
        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, AssetType.HORIZONTAL_BLOCK, SetBlockAttributesTask.class);

        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, AssetType.ITEM, SetMaterialAttributesTask.class);
        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, AssetType.ITEM, SetItemAttributesTask.class);
    }

    protected void registerBuilders() {
        final GameRegistry registry = Sponge.getRegistry();
        registry.registerBuilderSupplier(BlockSoundGroup.Builder.class, BlockSoundGroupBuilder::new);
        registry.registerBuilderSupplier(ItemGroup.Builder.class, ItemGroupBuilderImpl::new);

        // Block
        registry.registerBuilderSupplier(BuildableBlockType.Builder.class, AbstractBlockTypeBuilder.BuilderImpl::new);
        registry.registerBuilderSupplier(HorizontalType.Builder.class, HorizontalTypeBuilderImpl::new);

        // Item
        registry.registerBuilderSupplier(BuildableItemType.Builder.class, AbstractItemTypeBuilder.BuilderImpl::new);
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
