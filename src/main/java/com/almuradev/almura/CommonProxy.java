/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.bridge.IC2Bridge;
import com.almuradev.almura.client.network.play.C00PageInformation;
import com.almuradev.almura.content.Page;
import com.almuradev.almura.content.PageRegistry;
import com.almuradev.almura.content.PageUtil;
import com.almuradev.almura.items.Items;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.IItemBlockInformation;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackCreator;
import com.almuradev.almura.pack.PackKeys;
import com.almuradev.almura.pack.container.AlmuraContainerHandler;
import com.almuradev.almura.pack.container.PackContainerTileEntity;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.crop.PackSeeds;
import com.almuradev.almura.pack.crop.Stage;
import com.almuradev.almura.pack.item.PackItemBlock;
import com.almuradev.almura.pack.mapper.EntityMapper;
import com.almuradev.almura.pack.mapper.GameObjectMapper;
import com.almuradev.almura.pack.node.FertilizerNode;
import com.almuradev.almura.pack.node.SoilNode;
import com.almuradev.almura.pack.node.TreeNode;
import com.almuradev.almura.pack.node.property.GameObjectProperty;
import com.almuradev.almura.pack.tree.PackSapling;
import com.almuradev.almura.recipe.furnace.PackFuelHandler;
import com.almuradev.almura.server.network.play.S00AdditionalWorldInformation;
import com.almuradev.almura.server.network.play.S00PageInformation;
import com.almuradev.almura.server.network.play.S01OpenBlockInformationGui;
import com.almuradev.almura.server.network.play.S01PageDelete;
import com.almuradev.almura.server.network.play.S02OpenBlockWireframeGui;
import com.almuradev.almura.server.network.play.S02PageOpen;
import com.almuradev.almura.tabs.Tabs;
import com.almuradev.almura.util.FileSystem;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.network.play.server.S36PacketSignEditorOpen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CommonProxy {
    public static final SimpleNetworkWrapper NETWORK_FORGE = new SimpleNetworkWrapper("AM|FOR");

    public void onPreInitialization(FMLPreInitializationEvent event) {
        CommonProxy.NETWORK_FORGE.registerMessage(S00AdditionalWorldInformation.class, S00AdditionalWorldInformation.class, 0, Side.CLIENT);
        CommonProxy.NETWORK_FORGE.registerMessage(S01OpenBlockInformationGui.class, S01OpenBlockInformationGui.class, 1, Side.CLIENT);
        CommonProxy.NETWORK_FORGE.registerMessage(S02OpenBlockWireframeGui.class, S02OpenBlockWireframeGui.class, 2, Side.CLIENT);
        CommonProxy.NETWORK_FORGE.registerMessage(S00PageInformation.class, S00PageInformation.class, 3, Side.CLIENT);
        CommonProxy.NETWORK_FORGE.registerMessage(C00PageInformation.class, C00PageInformation.class, 4, Side.SERVER);
        CommonProxy.NETWORK_FORGE.registerMessage(S01PageDelete.class, S01PageDelete.class, 5, Side.CLIENT);
        CommonProxy.NETWORK_FORGE.registerMessage(S01PageDelete.class, S01PageDelete.class, 6, Side.SERVER);
        CommonProxy.NETWORK_FORGE.registerMessage(S02PageOpen.class, S02PageOpen.class, 7, Side.CLIENT);
        NetworkRegistry.INSTANCE.registerGuiHandler(Almura.INSTANCE, new AlmuraContainerHandler());
        GameRegistry.registerTileEntity(PackContainerTileEntity.class, Almura.MOD_ID + ":pack_container");
        GameRegistry.registerFuelHandler(new PackFuelHandler());
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        Tabs.fakeStaticLoad();
        Items.fakeStaticLoad();

        Pack.loadAllContent();
        if (Loader.isModLoaded("IC2")) {
            IC2Bridge.init();
        }
    }

    public void onInitialization(FMLInitializationEvent event) {
        try {
            GameObjectMapper.load();
        } catch (IOException e) {
            Almura.LOGGER.error("Failed to load mappings file in the config folder.", e);
        }

        try {
            EntityMapper.load();
        } catch (IOException e) {
            Almura.LOGGER.error("Failed to load entity_mappings file in the config folder.", e);
        }

        for (Map.Entry<String, Pack> entry : Pack.getPacks().entrySet()) {
            try {
                onPostCreate(entry.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<String, Pack> entry : Pack.getPacks().entrySet()) {
            try {
                onLoadFinished(entry.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LanguageRegistry.injectIntoForge();

        for (Languages entry : Languages.values()) {
            final Map<String, String> value = LanguageRegistry.get(entry);
            if (!value.isEmpty()) {
                Almura.LOGGER.info("Registered [" + value.size() + "] entries for language [" + entry.name() + "]");
            }
        }
    }

    public void onServerStarting(FMLServerStartingEvent event) {
        PageUtil.loadAll();
    }

    //Stage 1 loader
    public void onCreate(Pack pack) {
        //Stage 1a -> Register blocks
        for (Block block : pack.getBlocks()) {
            if (block instanceof IPackObject) {
                if (block instanceof IItemBlockInformation) {
                    GameRegistry.registerBlock(block, PackItemBlock.class,
                            ((IPackObject) block).getPack().getName() + "\\" + ((IPackObject) block).getIdentifier());
                } else {
                    GameRegistry.registerBlock(block, ((IPackObject) block).getPack().getName() + "\\" + ((IPackObject) block).getIdentifier());
                }
            }
        }

        //Stage 1b -> Register items
        for (Item item : pack.getItems()) {
            if (item instanceof IPackObject) {
                GameRegistry.registerItem(item, ((IPackObject) item).getPack().getName() + "\\" + ((IPackObject) item).getIdentifier());
            }
        }
    }

    //Stage 2 loader
    private void onPostCreate(Pack pack) throws IOException {
        final List<Item> seedsToAdd = Lists.newArrayList();

        //Stage 2a -> Collision, Seeds, Stage (Collision)
        for (Block block : pack.getBlocks()) {
            if (block instanceof IPackObject && block instanceof INodeContainer) {
                final ConfigurationNode reader = YAMLConfigurationLoader.builder()
                        .setFile(Paths.get(FileSystem.CONFIG_YML_PATH.toString(), ((IPackObject) block).getPack().getName(),
                                ((IPackObject) block).getIdentifier() + ".yml").toFile()).build().load();

                if (block instanceof PackCrops) {
                    final SoilNode soilNode = PackCreator.createSoilNode(((IPackObject) block).getPack(), ((IPackObject) block).getIdentifier(),
                            reader.getNode(PackKeys.NODE_SOIL.getKey()));
                    if (soilNode != null) {
                        //For seeds to work, they require a soil
                        ((INodeContainer) block).addNode(soilNode);
                        final String
                                textureName =
                                reader.getNode(PackKeys.TEXTURE.getKey()).getString(PackKeys.TEXTURE.getDefaultValue()).split(".png")[0];
                        final PackSeeds
                                seed =
                                PackCreator
                                        .createCropSeed(((PackCrops) block).getPack(), ((IPackObject) block).getIdentifier(),
                                                (Block) soilNode.getSoil().minecraftObject, (PackCrops) block,
                                                textureName,
                                                reader.getNode(PackKeys.NODE_SEED.getKey()));
                        if (GameRegistry.findItem(Almura.MOD_ID, seed.getPack().getName() + "\\" + seed.getIdentifier()) != null) {
                            Almura.LOGGER
                                    .error("Crop [" + ((PackCrops) block).getIdentifier() + "] in [" + ((PackCrops) block).getPack().getName()
                                            + "] is trying to add seed [" + seed.getIdentifier()
                                            + "] but it already exists. You may only have one seed per crop.");
                        } else {
                            GameRegistry.registerItem(seed, seed.getPack().getName() + "\\" + seed.getIdentifier());
                            seedsToAdd.add(seed);
                        }

                        for (Stage stage : ((PackCrops) block).getStages().values()) {
                            final ConfigurationNode stageNode = reader.getNode(PackKeys.NODE_STAGES.getKey()).getNode("" + stage.getId());
                            stage.addNode(PackCreator.createCollisionNode(pack, stage.getIdentifier(),
                                    stageNode.getNode(PackKeys.NODE_COLLISION.getKey())));
                        }
                    }
                } else {
                    //Collision
                    ((INodeContainer) block).addNode(
                            PackCreator.createCollisionNode(pack, ((IPackObject) block).getIdentifier(),
                                    reader.getNode(PackKeys.NODE_COLLISION.getKey())));

                }

                if (block instanceof PackSapling) {
                    final TreeNode treeNode = PackCreator.createTreeNode(pack, ((IPackObject) block).getIdentifier(), reader.getNode(PackKeys
                            .NODE_TREE.getKey()));
                    if (treeNode != null) {
                        ((INodeContainer) block).addNode(treeNode);
                    }
                }
            }
        }

        //Stage2b -> Add seeds to pack
        for (Item item : seedsToAdd) {
            pack.addItem(item);
        }
    }

    //Stage 3 loader
    private void onLoadFinished(Pack pack) throws IOException {
        //Stage 3a -> Block Break, Recipes
        for (Block block : pack.getBlocks()) {
            if (block instanceof IPackObject && block instanceof INodeContainer) {
                final ConfigurationNode reader = YAMLConfigurationLoader.builder()
                        .setFile(Paths.get(FileSystem.CONFIG_YML_PATH.toString(), ((IPackObject) block).getPack().getName(),
                                ((IPackObject) block).getIdentifier() + ".yml").toFile()).build().load();

                if (block instanceof PackCrops) {
                    for (Stage stage : ((PackCrops) block).getStages().values()) {
                        final ConfigurationNode stageNode = reader.getNode(PackKeys.NODE_STAGES.getKey(), stage.getId());
                        stage.addNode(PackCreator.createBreakNode(pack, stage.getIdentifier(), block, false, stageNode.getNode(PackKeys
                                .NODE_BREAK.getKey())));
                        stage.addNode(PackCreator.createStagedFertilizerNode(pack, stage.getIdentifier(), stage.getId(), stageNode.getNode(PackKeys
                                .NODE_FERTILIZER.getKey())));
                    }
                } else {
                    if (block instanceof PackSapling) {
                        ((PackSapling) block).addNode(PackCreator.createFertilizerNode(pack, ((PackSapling) block).getIdentifier(), reader
                                .getNode(PackKeys.NODE_FERTILIZER.getKey())));
                    }
                    //Break
                    ((INodeContainer) block).addNode(
                            PackCreator.createBreakNode(pack, ((IPackObject) block).getIdentifier(), block, true,
                                    reader.getNode(PackKeys.NODE_BREAK.getKey())));

                }

                //Recipes
                ((INodeContainer) block).addNode(
                        PackCreator.createRecipeNode(((IPackObject) block).getPack(), ((IPackObject) block).getIdentifier(), block,
                                reader.getNode(PackKeys.NODE_RECIPES.getKey())));
            }
        }

        //Stage 3b -> Item Recipes
        for (Item item : pack.getItems()) {
            if (item instanceof IPackObject && item instanceof INodeContainer) {
                final Path path;
                if (item instanceof PackSeeds) {
                    path = Paths.get(FileSystem.CONFIG_YML_PATH.toString(), ((IPackObject) item).getPack().getName(),
                            ((PackCrops) ((ItemSeeds) item).field_150925_a).getIdentifier() + ".yml");
                } else {
                    path = Paths.get(FileSystem.CONFIG_YML_PATH.toString(), ((IPackObject) item).getPack().getName(),
                            ((IPackObject) item).getIdentifier() + ".yml");
                }
                final ConfigurationNode reader = YAMLConfigurationLoader.builder().setFile(path.toFile()).build().load();

                if (item instanceof PackSeeds) {
                    //Recipes
                    ((INodeContainer) item).addNode(
                            PackCreator.createRecipeNode(((IPackObject) item).getPack(), ((IPackObject) item).getIdentifier(), item,
                                    reader.getNode(PackKeys.NODE_SEED.getKey(), PackKeys.NODE_RECIPES.getKey())));
                } else {
                    //Recipes
                    ((INodeContainer) item).addNode(
                            PackCreator.createRecipeNode(((IPackObject) item).getPack(), ((IPackObject) item).getIdentifier(), item,
                                    reader.getNode(PackKeys.NODE_RECIPES.getKey())));
                }
            }
        }
        
        Almura.LOGGER.info("Loaded -> " + pack);
    }

    @SubscribeEvent
    public void onItemCraftedEvent(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.craftMatrix instanceof InventoryCrafting)) {
            return;
        }
        IRecipe recipe = null;

        for (Object obj : CraftingManager.getInstance().getRecipeList()) {
            if (obj instanceof com.almuradev.almura.recipe.IRecipe) {
                if (((com.almuradev.almura.recipe.IRecipe) obj).checkMultiQuantity() && ((IRecipe) obj)
                        .matches((InventoryCrafting) event.craftMatrix, event.player.worldObj)) {
                    recipe = (IRecipe) obj;
                    break;
                }
            }
        }

        if (recipe == null) {
            return;
        }

        for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
            final ItemStack slotStack = event.craftMatrix.getStackInSlot(i);
            if (slotStack == null) {
                continue;
            }
            ItemStack recipeSlot = null;
            if (recipe instanceof ShapedRecipes) {
                if (((ShapedRecipes) recipe).recipeItems.length <= i) {
                    continue;
                }
                recipeSlot = ((ShapedRecipes) recipe).recipeItems[i];
            } else {
                for (Object obj : ((ShapelessRecipes) recipe).recipeItems) {
                    final ItemStack recipeStack = (ItemStack) obj;

                    if (slotStack.getItem() != recipeStack.getItem() || slotStack.stackSize < recipeStack.stackSize
                            || slotStack.getMetadata() != 32767 && slotStack.getMetadata() != recipeStack.getMetadata()) {
                        continue;
                    }

                    recipeSlot = recipeStack;
                    break;
                }
            }

            if (recipeSlot == null) {
                continue;
            }

            slotStack.stackSize -= recipeSlot.stackSize - 1;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBonemealEvent(BonemealEvent event) {
        if (event.world.isRemote) {
            return;
        }
        if (event.block instanceof INodeContainer) {
            if (event.block instanceof PackCrops) {
                final int blockMetadata = event.world.getBlockMetadata(event.x, event.y, event.z);
                final Stage stage = ((PackCrops) event.block).getStages().get(blockMetadata);
                if (!(stage != null && stage.hasNode(FertilizerNode.class))) {
                    event.setCanceled(true);
                } else {
                    final FertilizerNode stageFertilizerNode = stage.getNode(FertilizerNode.class);
                    boolean found = false;
                    for (GameObjectProperty prop : stageFertilizerNode.getValue()) {
                        final ItemStack heldStack = event.entityPlayer.getHeldItem();

                        if (heldStack != null && heldStack.getItem() != null) {
                            final Item heldItem = heldStack.getItem();

                            final Object minecraftObject = heldItem instanceof ItemBlock ? ((ItemBlock) heldItem).blockInstance : heldItem;
                            final Object propMinecraftObject = prop.getSource().minecraftObject instanceof ItemBlock ? ((ItemBlock) prop.getSource()
                                    .minecraftObject).blockInstance : prop.getSource().minecraftObject;

                            if (minecraftObject == propMinecraftObject && heldStack.getMetadata() == prop.getSource().data) {
                                found = true;
                                break;
                            }
                        }
                    }

                    if (!found) {
                        event.setCanceled(true);
                    }
                }
            } else if (!((INodeContainer) event.block).hasNode(FertilizerNode.class)) {
                event.setCanceled(true);
            } else {
                final FertilizerNode stageFertilizerNode = ((INodeContainer) event.block).getNode(FertilizerNode.class);
                boolean found = false;

                for (GameObjectProperty prop : stageFertilizerNode.getValue()) {
                    final ItemStack heldStack = event.entityPlayer.getHeldItem();

                    if (heldStack != null && heldStack.getItem() != null) {
                        final Item heldItem = heldStack.getItem();

                        final Object minecraftObject = heldItem instanceof ItemBlock ? ((ItemBlock) heldItem).blockInstance : heldItem;
                        final Object propMinecraftObject = prop.getSource().minecraftObject instanceof ItemBlock ? ((ItemBlock) prop.getSource()
                                .minecraftObject).blockInstance : prop.getSource().minecraftObject;

                        if (minecraftObject == propMinecraftObject && heldStack.getMetadata() == prop.getSource().data) {
                            found = true;
                        }
                    }
                }

                if (!found) {
                    event.setCanceled(true);
                }
            }
        }

        event.setResult(Event.Result.DEFAULT);
    }

    @SubscribeEvent
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!event.world.isRemote && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.entityPlayer.getHeldItem() == null
                && event.entityPlayer.isSneaking()) {
            final TileEntity te = event.world.getTileEntity(event.x, event.y, event.z);
            if (te != null && te instanceof TileEntitySign) {
                if (event.entityPlayer instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) event.entityPlayer).playerNetServerHandler
                            .sendPacket(new S36PacketSignEditorOpen(te.xCoord, te.yCoord, te.zCoord));
                }
            }
        }
    }

    public void handlePageInformation(MessageContext ctx, S00PageInformation message) {
    }

    public void handlePageInformation(MessageContext ctx, C00PageInformation message) {
        final Optional<Page> optPage = PageRegistry.getPage(message.identifier);
        final Page page;

        if (optPage.isPresent()) {
            page = optPage.get();
            page
                    .setIndex(message.index)
                    .setLastContributor(ctx.getServerHandler().playerEntity.getCommandSenderName())
                    .setLastModified(new Date())
                    .setTitle(message.title)
                    .setContents(PageUtil.replaceColorCodes("&", message.contents, true));
        } else {
            // String identifier, int index, String name, Date created, String author, Date lastModified, String lastContributor, String contents
            page = new Page(message.identifier, message.index, message.title, new Date(),
                    ctx.getServerHandler().playerEntity.getCommandSenderName(), new Date(),
                    ctx.getServerHandler().playerEntity.getCommandSenderName(), message.contents);
            PageRegistry.putPage(page);
        }

        if (canSavePages()) {
            savePage(message.identifier, page);
        }

        CommonProxy.NETWORK_FORGE.sendToAll(new S00PageInformation(page));
    }

    public void handlePageDelete(MessageContext ctx, S01PageDelete message) {
        PageRegistry.removePage(message.identifier);
        if (canSavePages()) {
            deletePage(message.identifier);
        }
    }

    public void handlePageOpen(MessageContext ctx, S02PageOpen message) {
    }

    public boolean canSavePages() {
        return false;
    }

    private void savePage(String identifier, Page page) {
        try {
            PageUtil.savePage(identifier, page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deletePage(String identifier) {
        try {
            PageUtil.deletePage(identifier);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
