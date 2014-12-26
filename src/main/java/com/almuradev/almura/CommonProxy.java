/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.GameObjectMapper;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackCreator;
import com.almuradev.almura.pack.PackKeys;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.crop.PackSeeds;
import com.almuradev.almura.pack.node.SoilNode;
import com.almuradev.almura.server.network.play.S00AdditionalWorldInfo;
import com.almuradev.almura.server.network.play.bukkit.B00PlayerDisplayName;
import com.almuradev.almura.server.network.play.bukkit.B01PlayerCurrency;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;

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

        Tabs.fakeStaticLoad();

        Pack.loadAllContent();

        LanguageRegistry.injectIntoForge();

        for (Languages entry : Languages.values()) {
            final Map<String, String> value = LanguageRegistry.get(entry);
            if (!value.isEmpty()) {
                Almura.LOGGER.info("Registered [" + value.size() + "] entries for language [" + entry.name() + "]");
            }
        }
    }

    public void onPostInitialization(FMLPostInitializationEvent event) {
        GameObjectMapper.load();
        for (Map.Entry<String, Pack> entry : Pack.getPacks().entrySet()) {
            try {
                onPostCreate(entry.getValue());
            } catch (IOException | ConfigurationException e) {
                //TODO Better exception handling
                e.printStackTrace();
            }
        }
    }

    public void onCreate(Pack pack) {
        for (Block block : pack.getBlocks()) {
            if (block instanceof IPackObject) {
                GameRegistry.registerBlock(block, ((IPackObject) block).getPack().getName() + "\\" + ((IPackObject) block).getIdentifier());
                MinecraftForge.EVENT_BUS.register(block);
            }
        }

        for (Item item : pack.getItems()) {
            if (item instanceof IPackObject) {
                GameRegistry.registerItem(item, ((IPackObject) item).getPack().getName() + "\\" + ((IPackObject) item).getIdentifier());
                MinecraftForge.EVENT_BUS.register(item);
            }
        }

        Almura.LOGGER.info("Loaded -> " + pack);
    }

    private void onPostCreate(Pack pack) throws IOException, ConfigurationException {
        final List<Item> seedsToAdd = Lists.newArrayList();

        //Order is important
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
                        final String textureName = reader.getChild(PackKeys.TEXTURE.getKey()).getString(PackKeys.TEXTURE.getDefaultValue());
                        final PackSeeds
                                seed = PackCreator.createCropSeed(((PackCrops) block).getPack(), soilNode.getSoil(), (PackCrops) block, textureName, reader.getNode(PackKeys.NODE_SEED.getKey()));
                        if (GameRegistry.findItem(Almura.MOD_ID, seed.getPack().getName() + "\\" + seed.getIdentifier()) != null) {
                            Almura.LOGGER.error("Crop [" + ((PackCrops) block).getIdentifier() + "] in [" + ((PackCrops) block).getPack().getName() + "] is trying to add seed [" + seed.getIdentifier() + "] but it already exists. You may only have one seed per crop.");
                        } else {
                            GameRegistry.registerItem(seed, seed.getPack().getName() + "\\" + seed.getIdentifier());
                            seedsToAdd.add(seed);
                            MinecraftForge.EVENT_BUS.register(seed);
                        }
                    }
                }
                //Recipes
                ((INodeContainer) block).addNode(
                        PackCreator.createRecipeNode(((IPackObject) block).getPack(), ((IPackObject) block).getIdentifier(), block,
                                                     reader.getNode(PackKeys.NODE_RECIPES.getKey())));
                entry.close();
            }
        }

        for (Item item : seedsToAdd) {
            pack.addItem(item);
        }

        for (Item item : pack.getItems()) {
            if (item instanceof IPackObject && item instanceof INodeContainer) {
                final InputStream
                        entry =
                        Files.newInputStream(Paths.get(Filesystem.CONFIG_YML_PATH.toString(), ((IPackObject) item).getPack().getName(),
                                                       ((IPackObject) item).getIdentifier() + ".yml"));
                final YamlConfiguration reader = new YamlConfiguration(entry);
                reader.load();
                //Recipes
                ((INodeContainer) item).addNode(
                        PackCreator.createRecipeNode(((IPackObject) item).getPack(), ((IPackObject) item).getIdentifier(), item,
                                                     reader.getNode(PackKeys.NODE_RECIPES.getKey())));
                entry.close();
            }
        }
    }
}
