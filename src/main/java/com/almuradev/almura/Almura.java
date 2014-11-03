/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.client.KeyListener;
import com.almuradev.almura.client.Keybindings;
import com.almuradev.almura.items.Items;
import com.almuradev.almura.lang.LanguageManager;
import com.almuradev.almura.smp.SMPBlock;
import com.almuradev.almura.smp.SMPPack;
import com.almuradev.almura.smp.client.renderer.ShapeRenderer;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Almura.MOD_ID)
public class Almura {

    public static final String MOD_ID = "Almura";
    public static final Logger LOGGER = LogManager.getLogger("Almura");
    public static final LanguageManager LANGUAGES = new LanguageManager();

    @SideOnly(Side.CLIENT)
    public static final ShapeRenderer SHAPE_RENDERER = new ShapeRenderer();

    @EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        if (Configuration.IS_DEBUG) {
            LOGGER.info("Debug-mode toggled ON");
        }
        Items.fakeStaticLoad();
        Tabs.fakeStaticLoad();

        //Load SMPs
        SMPPack.load();

        LANGUAGES.register();

        if (Configuration.IS_CLIENT) {
            onClientInitialization();
        }
    }

    @SideOnly(Side.CLIENT)
    private void onClientInitialization() {
        Keybindings.fakeStaticLoad();
        new KeyListener();
        SHAPE_RENDERER.registerFor(SMPBlock.class);
    }
}
