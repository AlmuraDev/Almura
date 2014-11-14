/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.server.network.play.S00AdditionalWorldInfo;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.Map;

public class CommonProxy {

    public void onPreInitialization(FMLPreInitializationEvent event) {
        Almura.NETWORK.registerMessage(S00AdditionalWorldInfo.class, S00AdditionalWorldInfo.class, 0, Side.CLIENT);

        Tabs.fakeStaticLoad();

        ContentPack.loadAllContent();

        Almura.LANGUAGES.injectIntoForge();

        for (Languages entry : Languages.values()) {
            final Map<String, String> value = Almura.LANGUAGES.get(entry);
            if (!value.isEmpty()) {
                Almura.LOGGER.info("Registered [" + value.size() + "] entries for language [" + entry.name() + "]");
            }
        }
    }

    public void onPostInitialization(FMLPostInitializationEvent event) {
    }

    public void onCreate(Block block) {
    }

    public void onCreate(Item item) {
    }

    public void onPostCreate(Block block) {
        GameRegistry.registerBlock(block, block.getUnlocalizedName());
    }

    public void onPostCreate(Item item) {
        GameRegistry.registerItem(item, item.getUnlocalizedName());
    }
}
