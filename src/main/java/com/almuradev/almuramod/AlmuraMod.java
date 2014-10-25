/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod;

import com.almuradev.almuramod.blocks.Blocks;
import com.almuradev.almuramod.client.renderer.BaseOBJRenderer;
import com.almuradev.almuramod.entities.AlmuraTileEntity;
import com.almuradev.almuramod.gui.MessageBox;
import com.almuradev.almuramod.gui.TabTester;
import com.almuradev.almuramod.items.Items;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = AlmuraMod.MOD_ID)
public class AlmuraMod {
    public static final String MOD_ID = "Almura";
    public static final Logger LOGGER = LogManager.getLogger("Almura");
    public static CreativeTabs act_Building, act_Roofing, act_Lighting, act_Furniture, act_Decoration, act_Storage, act_Barrels, act_Flowers, act_Plants, act_Crops;
    public static CreativeTabs act_Flags, act_Signs, act_Letters, act_Ores, act_Food, act_Drink, act_Ingredients, act_Bottles, act_Tools;

    @Mod.EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        // Setup all Almura Creative Tabs.
        setupAlmuraTabs();

        // Force regirstion of Custom Blocks and Items.
        Blocks.fakeStaticLoad();
        Items.fakeStaticLoad();

        // Test
        GameRegistry.registerTileEntity(AlmuraTileEntity.class, "almuraTileEntity");
        setupRenderers();
    }

    @SideOnly(Side.CLIENT)
    private void setupRenderers() {
        FMLCommonHandler.instance().bus().register(this);
        ClientRegistry.registerKeyBinding(KeyBindings.MSG_BOX_TEST);
        ClientRegistry.registerKeyBinding(KeyBindings.TAB_TESTER);
//        ClientRegistry.bindTileEntitySpecialRenderer(AlmuraTileEntity.class, new BaseOBJRenderer("example", "example"));
    }
    
    private void setupAlmuraTabs() {
        
        act_Building = new CreativeTabs("act_Building") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almuramod.items.Items.ALMURA_BUILDING;
            }
        };
        
        act_Roofing = new CreativeTabs("act_Roofing") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_ROOFING;
            }
        };

        act_Lighting = new CreativeTabs("act_Lighting") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_LIGHTING;
            }
        };
        
        act_Furniture = new CreativeTabs("act_Furniture") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_FURNITURE;
            }
        };
        
        act_Decoration = new CreativeTabs("act_Decoration") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_DECORATION;
            }
        };
        
        act_Storage = new CreativeTabs("act_Storage") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_STORAGE;
            }
        };
        
        act_Barrels = new CreativeTabs("act_Barrels") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_BARRELS;
            }
        };
        
        act_Flowers = new CreativeTabs("act_Flowers") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_FLOWERS;
            }
        };
        
        act_Plants = new CreativeTabs("act_Plants") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_PLANTS;
            }
        };
        
        act_Crops = new CreativeTabs("act_Crops") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_CROPS;
            }
        };
        
        act_Flags = new CreativeTabs("act_Flags") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_FLAGS;
            }
        };
        
        act_Signs = new CreativeTabs("act_Signs") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_SIGNS;
            }
        };
        
        act_Letters = new CreativeTabs("act_Letters") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_LETTERS;
            }
        };
        
        act_Ores = new CreativeTabs("act_Ores") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_ORES;
            }
        };
        
        act_Food = new CreativeTabs("act_Food") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_FOOD;
            }
        };
        
        act_Drink = new CreativeTabs("act_Drink") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_DRINK;
            }
        };
        
        act_Ingredients = new CreativeTabs("act_Ingredients") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_INGREDIENTS;
            }
        };
        
        act_Bottles = new CreativeTabs("act_Bottles") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_BOTTLES;
            }
        };
        
        act_Tools = new CreativeTabs("act_Tools") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {                
                return com.almuradev.almuramod.items.Items.ALMURA_TOOLS;
            }
        };
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (KeyBindings.MSG_BOX_TEST.isPressed()) {
            final MessageBox box = new MessageBox("Almura - Test", "This is a test of our messagebox!");
            box.display();
        } else if (KeyBindings.TAB_TESTER.isPressed()) {
            final TabTester tab = new TabTester("Almura Tabs", "Tab 1", "Tab 2", "Tab 4", "Fooled ya.");
            tab.display();
        }
    }
}
