/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.server.network.play.S00AdditionalWorldInfo;
import com.almuradev.almura.server.network.play.bukkit.B00PlayerDisplayName;
import com.almuradev.almura.server.network.play.bukkit.B01PlayerCurrency;
import com.flowpowered.cerealization.config.ConfigurationException;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.io.IOException;
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
                entry.getValue().onPostInitialization();
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
            }
        }

        for (Item item : pack.getItems()) {
            if (item instanceof IPackObject) {
                GameRegistry.registerItem(item, ((IPackObject) item).getPack().getName() + "\\" + ((IPackObject) item).getIdentifier());
            }
        }

        Almura.LOGGER.info("Loaded -> " + pack);
    }
}
