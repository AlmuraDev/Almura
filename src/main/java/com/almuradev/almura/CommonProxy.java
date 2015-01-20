/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
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
import com.almuradev.almura.pack.mapper.EntityMapper;
import com.almuradev.almura.pack.mapper.GameObjectMapper;
import com.almuradev.almura.pack.node.SoilNode;
import com.almuradev.almura.recipe.furnace.PackFuelHandler;
import com.almuradev.almura.server.network.play.S00AdditionalWorldInfo;
import com.almuradev.almura.server.network.play.bukkit.B00PlayerDisplayName;
import com.almuradev.almura.server.network.play.bukkit.B01PlayerCurrency;
import com.almuradev.almura.server.network.play.bukkit.B02AdditionalWorldInfo;
import com.almuradev.almura.tabs.Tabs;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class CommonProxy {

    public void onPreInitialization(FMLPreInitializationEvent event) {
        Almura.NETWORK_FORGE.registerMessage(S00AdditionalWorldInfo.class, S00AdditionalWorldInfo.class, 0, Side.CLIENT);
        Almura.NETWORK_BUKKIT.registerMessage(B00PlayerDisplayName.class, B00PlayerDisplayName.class, 0, Side.CLIENT);
        Almura.NETWORK_BUKKIT.registerMessage(B01PlayerCurrency.class, B01PlayerCurrency.class, 1, Side.CLIENT);
        Almura.NETWORK_BUKKIT.registerMessage(B02AdditionalWorldInfo.class, B02AdditionalWorldInfo.class, 2, Side.CLIENT);
        NetworkRegistry.INSTANCE.registerGuiHandler(Almura.INSTANCE, new AlmuraContainerHandler());
        GameRegistry.registerTileEntity(PackContainerTileEntity.class, Almura.MOD_ID + ":pack_container");
        GameRegistry.registerFuelHandler(new PackFuelHandler());
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        Tabs.fakeStaticLoad();

        Pack.loadAllContent();
    }

    public void onInitialization(FMLInitializationEvent event) {
        try {
            GameObjectMapper.load();
        } catch (ConfigurationException e) {
            Almura.LOGGER.error("Failed to load mappings file in the config folder.", e);
        }

        try {
            EntityMapper.load();
        } catch (ConfigurationException e) {
            Almura.LOGGER.error("Failed to load entity_mappings file in the config folder.", e);
        }

        for (Map.Entry<String, Pack> entry : Pack.getPacks().entrySet()) {
            try {
                onPostCreate(entry.getValue());
            } catch (IOException | ConfigurationException e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<String, Pack> entry : Pack.getPacks().entrySet()) {
            try {
                onLoadFinished(entry.getValue());
            } catch (IOException | ConfigurationException e) {
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

    //Stage 1 loader
    public void onCreate(Pack pack) {
        //Stage 1a -> Register blocks
        for (Block block : pack.getBlocks()) {
            if (block instanceof IPackObject) {
                GameRegistry.registerBlock(block, ((IPackObject) block).getPack().getName() + "\\" + ((IPackObject) block).getIdentifier());
            }
        }

        //Stage 1b -> Register items
        for (Item item : pack.getItems()) {
            if (item instanceof IPackObject) {
                GameRegistry.registerItem(item, ((IPackObject) item).getPack().getName() + "\\" + ((IPackObject) item).getIdentifier());
            }
        }

        Almura.LOGGER.info("Loaded -> " + pack);
    }

    //Stage 2 loader
    private void onPostCreate(Pack pack) throws IOException, ConfigurationException {
        final List<Item> seedsToAdd = Lists.newArrayList();

        //Stage 2a -> Collision, Seeds, Stage (Collision)
        for (Block block : pack.getBlocks()) {
            if (block instanceof IPackObject && block instanceof INodeContainer) {
                final InputStream
                        entry =
                        Files.newInputStream(Paths.get(Filesystem.CONFIG_YML_PATH.toString(), ((IPackObject) block).getPack().getName(),
                                                       ((IPackObject) block).getIdentifier() + ".yml"));
                final YamlConfiguration reader = new YamlConfiguration(entry);
                reader.load();

                if (block instanceof PackCrops) {
                    final SoilNode soilNode = PackCreator.createSoilNode(((IPackObject) block).getPack(), ((IPackObject) block).getIdentifier(),
                                                                         reader.getNode(PackKeys.NODE_SOIL.getKey()));
                    if (soilNode != null) {
                        //For seeds to work, they require a soil
                        ((INodeContainer) block).addNode(soilNode);
                        final String
                                textureName =
                                reader.getChild(PackKeys.TEXTURE.getKey()).getString(PackKeys.TEXTURE.getDefaultValue()).split(".png")[0];
                        final PackSeeds
                                seed =
                                PackCreator
                                        .createCropSeed(((PackCrops) block).getPack(), ((IPackObject) block).getIdentifier(),
                                                        (Block) soilNode.getSoil().minecraftObject, (PackCrops) block,
                                                        textureName,
                                                        reader.getNode(PackKeys.NODE_SEED.getKey()));
                        if (GameRegistry.findItem(Almura.MOD_ID, seed.getPack().getName() + "\\" + seed.getIdentifier()) != null) {
                            Almura.LOGGER.error("Crop [" + ((PackCrops) block).getIdentifier() + "] in [" + ((PackCrops) block).getPack().getName()
                                                + "] is trying to add seed [" + seed.getIdentifier()
                                                + "] but it already exists. You may only have one seed per crop.");
                        } else {
                            GameRegistry.registerItem(seed, seed.getPack().getName() + "\\" + seed.getIdentifier());
                            seedsToAdd.add(seed);
                        }

                        for (Stage stage : ((PackCrops) block).getStages().values()) {
                            final ConfigurationNode stageNode = reader.getChild(PackKeys.NODE_STAGES.getKey()).getNode("" + stage.getId());
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

                entry.close();
            }
        }

        //Stage2b -> Add seeds to pack
        for (Item item : seedsToAdd) {
            pack.addItem(item);
        }
    }

    //Stage 3 loader
    private void onLoadFinished(Pack pack) throws IOException, ConfigurationException {
        //Stage 3a -> Block Break, Recipes
        for (Block block : pack.getBlocks()) {
            if (block instanceof IPackObject && block instanceof INodeContainer) {
                final InputStream
                        entry =
                        Files.newInputStream(Paths.get(Filesystem.CONFIG_YML_PATH.toString(), ((IPackObject) block).getPack().getName(),
                                                       ((IPackObject) block).getIdentifier() + ".yml"));
                final YamlConfiguration reader = new YamlConfiguration(entry);
                reader.load();

                if (block instanceof PackCrops) {
                    for (Stage stage : ((PackCrops) block).getStages().values()) {
                        final ConfigurationNode stageNode = reader.getChild(PackKeys.NODE_STAGES.getKey()).getNode("" + stage.getId());
                        stage.addNode(PackCreator.createBreakNode(pack, stage.getIdentifier(), stageNode.getNode(PackKeys.NODE_BREAK.getKey())));
                        stage.addNode(PackCreator.createFertilizerNode(pack, stage.getIdentifier(), stage.getId(), stageNode.getNode(PackKeys.NODE_FERTILIZER.getKey())));
                    }
                } else {
                    //Break
                    ((INodeContainer) block).addNode(
                            PackCreator.createBreakNode(pack, ((IPackObject) block).getIdentifier(), reader.getNode(PackKeys.NODE_BREAK.getKey())));

                }

                //Recipes
                ((INodeContainer) block).addNode(
                        PackCreator.createRecipeNode(((IPackObject) block).getPack(), ((IPackObject) block).getIdentifier(), block,
                                                     reader.getNode(PackKeys.NODE_RECIPES.getKey())));

                entry.close();
            }
        }

        //Stage 3b -> Item Recipes
        for (Item item : pack.getItems()) {
            if (item instanceof IPackObject && item instanceof INodeContainer) {
                final InputStream entry;
                if (item instanceof PackSeeds) {
                    entry =
                            Files.newInputStream(Paths.get(Filesystem.CONFIG_YML_PATH.toString(), ((IPackObject) item).getPack().getName(),
                                                           ((PackCrops) ((ItemSeeds) item).field_150925_a).getIdentifier() + ".yml"));
                } else {
                    entry =
                            Files.newInputStream(Paths.get(Filesystem.CONFIG_YML_PATH.toString(), ((IPackObject) item).getPack().getName(),
                                                           ((IPackObject) item).getIdentifier() + ".yml"));
                }
                final YamlConfiguration reader = new YamlConfiguration(entry);
                reader.load();
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

                entry.close();
            }
        }
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.craftMatrix instanceof InventoryCrafting)) {
            return;
        }
        IRecipe recipe = null;

        for (Object obj : CraftingManager.getInstance().getRecipeList()) {
            if (obj instanceof com.almuradev.almura.recipe.IRecipe) {
                if (((com.almuradev.almura.recipe.IRecipe) obj).checkMultiQuantity() && ((IRecipe) obj).matches((InventoryCrafting) event.craftMatrix, event.player.worldObj)) {
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
                        || slotStack.getItemDamage() != 32767 && slotStack.getItemDamage() != recipeStack.getItemDamage()) {
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

    @SubscribeEvent
    public void onBonemeal(BonemealEvent event) {
        if (event.block instanceof PackCrops) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.world.isRemote && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            final TileEntity te = event.world.getTileEntity(event.x, event.y, event.z);
            if (te != null && te instanceof TileEntitySign) {
                if (event.entityPlayer instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) event.entityPlayer).playerNetServerHandler
                            .sendPacket(new S36PacketSignEditorOpen(te.xCoord, te.yCoord, te.zCoord));
                }
            }
        }
    }
}
