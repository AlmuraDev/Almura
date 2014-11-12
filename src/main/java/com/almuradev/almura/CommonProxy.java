/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.server.network.play.S00AdditionalWorldInfo;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class CommonProxy {

    public void onPreInitialization(FMLPreInitializationEvent event) {
        if (Configuration.IS_DEBUG) {
            Almura.LOGGER.info("Debug-mode toggled ON");
        }

        Almura.NETWORK.registerMessage(S00AdditionalWorldInfo.class, S00AdditionalWorldInfo.class, 0, Side.CLIENT);

        Tabs.fakeStaticLoad();

        ContentPack.loadAllContent();

        Almura.LANGUAGES.injectIntoForge();
    }

    public void onPostInitialization(FMLPostInitializationEvent event) {
    }

    public void onNewBlockInstance(Block block) {
    }

    public void onNewItemInstance(Item item) {
    }
}
