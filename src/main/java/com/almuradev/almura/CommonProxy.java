package com.almuradev.almura;

import com.almuradev.almura.pack.ContentPack;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class CommonProxy {

    public void onPreInitialization(FMLPreInitializationEvent event) {
        if (Configuration.IS_DEBUG) {
            Almura.LOGGER.info("Debug-mode toggled ON");
        }

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
