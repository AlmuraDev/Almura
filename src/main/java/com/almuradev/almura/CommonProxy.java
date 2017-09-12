/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.asm.mixin.interfaces.IMixinAlmuraBlock;
import com.almuradev.almura.configuration.AbstractConfiguration;
import com.almuradev.almura.configuration.MappedConfigurationAdapter;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetLoader;
import com.almuradev.almura.content.loader.AssetPipeline;
import com.almuradev.almura.content.loader.AssetRegistry;
import com.almuradev.almura.content.loader.LoaderPhase;
import com.almuradev.almura.content.type.block.component.action.breaks.BlockBreak;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.Drop;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.ExperienceDrop;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroupBuilder;
import com.almuradev.almura.content.type.block.component.sound.factory.SetBlockSoundGroupAttributesTask;
import com.almuradev.almura.content.type.block.factory.BlockItemGroupProvider;
import com.almuradev.almura.content.type.block.factory.SetBlockAttributesTask;
import com.almuradev.almura.content.type.block.type.AbstractBlockTypeBuilder;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.almura.content.type.block.type.horizontal.HorizontalType;
import com.almuradev.almura.content.type.block.type.horizontal.HorizontalTypeBuilderImpl;
import com.almuradev.almura.content.type.item.builder.AbstractItemTypeBuilder;
import com.almuradev.almura.content.type.item.factory.ItemItemGroupProvider;
import com.almuradev.almura.content.type.item.factory.SetItemAttributesTask;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.content.type.item.group.ItemGroupBuilderImpl;
import com.almuradev.almura.content.type.item.group.factory.SetItemGroupAttributesTask;
import com.almuradev.almura.content.type.item.type.BuildableItemType;
import com.almuradev.almura.content.type.material.MapColor;
import com.almuradev.almura.content.type.material.Material;
import com.almuradev.almura.network.NetworkConfig;
import com.almuradev.almura.network.play.SServerInformationMessage;
import com.almuradev.almura.network.play.SWorldInformationMessage;
import com.almuradev.almura.registry.BlockSoundGroupRegistryModule;
import com.almuradev.almura.registry.BossBarColorRegistryModule;
import com.almuradev.almura.registry.ItemGroupRegistryModule;
import com.almuradev.almura.registry.MapColorRegistryModule;
import com.almuradev.almura.registry.MaterialRegistryModule;
import com.almuradev.almura.util.NetworkUtil;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelRegistrationException;

import java.io.IOException;
import java.util.Random;

/**
 * The common platform of Almura. All code meant to run on the client and both dedicated and integrated server go here.
 */
public abstract class CommonProxy {

    protected ChannelBinding.IndexedMessageChannel network;
    protected AssetRegistry assetRegistry;
    protected AssetPipeline assetPipeline;
    protected AssetLoader assetLoader;

    protected void onGameConstruction(GameConstructionEvent event) {
        if (!Sponge.getGame().getChannelRegistrar().isChannelAvailable(NetworkConfig.CHANNEL)) {
            throw new ChannelRegistrationException("Some other mod/plugin has registered Almura's networking channel [" + NetworkConfig.CHANNEL + ']');
        }

        this.loadConfig();

        this.network = Sponge.getGame().getChannelRegistrar().createChannel(Almura.instance.container, NetworkConfig.CHANNEL);
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

        MinecraftForge.EVENT_BUS.register(this);
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

    private void registerFileSystem() {
    }

    protected void registerMessages() {
        this.network.registerMessage(SWorldInformationMessage.class, 0);
        this.network.registerMessage(SServerInformationMessage.class, 1);
    }

    private void registerRegistryModules() {
        final GameRegistry registry = Sponge.getRegistry();
        registry.registerModule(new BossBarColorRegistryModule());
        registry.registerModule(BlockSoundGroup.class, new BlockSoundGroupRegistryModule());
        registry.registerModule(ItemGroup.class, ItemGroupRegistryModule.getInstance());
        registry.registerModule(MapColor.class, new MapColorRegistryModule());
        registry.registerModule(Material.class, new MaterialRegistryModule());
    }

    private void registerPipelineStages() {
        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, SetBlockSoundGroupAttributesTask.class, Asset.Type.BLOCK_SOUNDGROUP);

        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, SetItemGroupAttributesTask.class, Asset.Type.ITEMGROUP);

        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, BlockItemGroupProvider.class, Asset.Type.BLOCK, Asset.Type.HORIZONTAL_BLOCK);
        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, SetBlockAttributesTask.class, Asset.Type.BLOCK, Asset.Type.HORIZONTAL_BLOCK);

        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, ItemItemGroupProvider.class, Asset.Type.ITEM);
        this.assetPipeline.registerStage(LoaderPhase.CONSTRUCTION, SetItemAttributesTask.class, Asset.Type.ITEM);
    }

    private void registerBuilders() {
        final GameRegistry registry = Sponge.getRegistry();
        registry.registerBuilderSupplier(BlockSoundGroup.Builder.class, BlockSoundGroupBuilder::new);
        registry.registerBuilderSupplier(ItemGroup.Builder.class, ItemGroupBuilderImpl::new);

        // Block
        registry.registerBuilderSupplier(BuildableBlockType.Builder.class, AbstractBlockTypeBuilder.BuilderImpl::new);
        registry.registerBuilderSupplier(HorizontalType.Builder.class, HorizontalTypeBuilderImpl::new);

        // Item
        registry.registerBuilderSupplier(BuildableItemType.Builder.class, AbstractItemTypeBuilder.BuilderImpl::new);
    }

    private void registerListeners() {
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

    // This is only necessary to get xp handled correctly for Player breaks in the Forge ecosystem
    // Make sure to attempt to go first so we can set xp so that mods/plugins can override us if necessary
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        final Block block = event.getWorld().getBlockState(event.getPos()).getBlock();

        if (!(block instanceof IMixinAlmuraBlock)) {
            return;
        }

        int exp = this.getXpToDrop((ItemType) event.getPlayer().getActiveItemStack().getItem(), (IMixinAlmuraBlock) block, event.getWorld()
                .rand);

        if (exp == -1) {
            // if no exp for this itemstack, fallback to empty hand and check again
            exp = this.getXpToDrop(ItemStack.empty().getType(), (IMixinAlmuraBlock) block, event.getWorld().rand);
        }

        if (exp != -1) {
            event.setExpToDrop(exp);
        }
    }

    private int getXpToDrop(ItemType usedType, IMixinAlmuraBlock block, Random random) {
        int exp = -1;

        for (BlockBreak kitkat : block.getBreaks()) {
            if (kitkat.doesItemMatch(usedType)) {
                for (Drop drop : kitkat.getDrops()) {
                    if (drop instanceof ExperienceDrop) {
                        final ExperienceDrop expDrop = (ExperienceDrop) drop;
                        exp += expDrop.flooredAmount(random);
                    }
                }
            }
        }

        return exp;
    }
}
