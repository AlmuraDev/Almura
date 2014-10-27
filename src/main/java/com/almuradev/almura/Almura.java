/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.blocks.Blocks;
import com.almuradev.almura.items.Items;
import com.almuradev.almura.lang.LanguageManager;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Almura.MOD_ID)
public class Almura {
    public static final String MOD_ID = "Almura";
    public static final Logger LOGGER = LogManager.getLogger("Almura");
    public static final LanguageManager LANGUAGES = new LanguageManager();

    @Mod.Instance
    public static Almura INSTANCE;

    @EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        if (Configuration.IS_DEBUG) {
            LOGGER.info("Debug-mode toggled ON");
        }
        Blocks.fakeStaticLoad();
        Items.fakeStaticLoad();
        Tabs.fakeStaticLoad();
        LANGUAGES.register();
    }
}
