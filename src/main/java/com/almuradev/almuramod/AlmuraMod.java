/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod;

import com.almuradev.almuramod.blocks.Blocks;
import com.almuradev.almuramod.items.Items;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

@Mod(modid = AlmuraMod.MOD_ID)
public class AlmuraMod {
    public static final String MOD_ID = "AlmuraMod";
    public static CreativeTabs CREATIVE_TAB;

    @Mod.EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        // Setup our Creative Tab
        CREATIVE_TAB = new CreativeTabs("almuraCreativeTab") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return net.minecraft.init.Items.cauldron;
            }
        };

        Blocks.fakeStaticLoad();
        Items.fakeStaticLoad();
    }
}
