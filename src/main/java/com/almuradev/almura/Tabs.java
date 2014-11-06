/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class Tabs {

    public static CreativeTabs BUILDING, ROOFING, LIGHTING, FURNITURE, DECORATION, STORAGE, BARRELS, FLOWERS, PLANTS, CROPS,
            FLAGS, SIGNS, LETTERS, ORES, FOOD, DRINK, INGREDIENTS, TOOLS;

    static {
        BUILDING = new CreativeTabs("building") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.BUILDING;
            }
        };
        
        ROOFING = new CreativeTabs("roofing") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.ROOFING;
            }
        };

        LIGHTING = new CreativeTabs("lighting") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.LIGHTING;
            }
        };

        FURNITURE = new CreativeTabs("furniture") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.FURNITURE;
            }
        };

        DECORATION = new CreativeTabs("decoration") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.DECORATION;
            }
        };

        STORAGE = new CreativeTabs("storage") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.STORAGE;
            }
        };

        BARRELS = new CreativeTabs("barrels") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.BARRELS;
            }
        };

        FLOWERS = new CreativeTabs("flowers") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.FLOWERS;
            }
        };

        PLANTS = new CreativeTabs("plants") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.PLANTS;
            }
        };

        CROPS = new CreativeTabs("crops") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.CROPS;
            }
        };

        FLAGS = new CreativeTabs("flags") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.FLAGS;
            }
        };

        SIGNS = new CreativeTabs("signs") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.SIGNS;
            }
        };

        LETTERS = new CreativeTabs("letters") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.LETTERS;
            }
        };

        ORES = new CreativeTabs("ores") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.ORES;
            }
        };

        FOOD = new CreativeTabs("food") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.FOOD;
            }
        };

        DRINK = new CreativeTabs("drink") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.DRINK;
            }
        };

        INGREDIENTS = new CreativeTabs("ingredients") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.INGREDIENTS;
            }
        };

        TOOLS = new CreativeTabs("tools") {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return com.almuradev.almura.items.Items.TOOLS;
            }
        }; 
    }

    public static void fakeStaticLoad() {
    }
}
