/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.tabs;

import com.almuradev.almura.Almura;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class Tabs {

    private static final Item ITEM_BUILDING = new AlmuraTabItem("building");
    private static final Item ITEM_ROOFING = new AlmuraTabItem("roofing");
    private static final Item ITEM_FLOORING = new AlmuraTabItem("flooring");
    private static final Item ITEM_LIGHTING = new AlmuraTabItem("lighting");
    private static final Item ITEM_FURNITURE = new AlmuraTabItem("furniture");
    private static final Item ITEM_DECORATION = new AlmuraTabItem("decoration");
    private static final Item ITEM_STORAGE = new AlmuraTabItem("storage");
    private static final Item ITEM_BARRELS = new AlmuraTabItem("barrels");
    private static final Item ITEM_FLOWERS = new AlmuraTabItem("flowers");
    private static final Item ITEM_COMPONENTS = new AlmuraTabItem("components");
    private static final Item ITEM_CROPS = new AlmuraTabItem("crops");
    private static final Item ITEM_SEEDS = new AlmuraTabItem("seeds");
    private static final Item ITEM_FLAGS = new AlmuraTabItem("flags");
    private static final Item ITEM_SIGNS = new AlmuraTabItem("signs");
    private static final Item ITEM_LETTERS = new AlmuraTabItem("letters");
    private static final Item ITEM_ORES = new AlmuraTabItem("ores");
    private static final Item ITEM_FOODS = new AlmuraTabItem("foods");
    private static final Item ITEM_DRINKS = new AlmuraTabItem("drinks");
    private static final Item ITEM_INGREDIENTS = new AlmuraTabItem("ingredients");
    private static final Item ITEM_TOOL = new AlmuraTabItem("tool");

    public static CreativeTabs TAB_BUILDING = new AlmuraCreativeTabs("building", "Almura Building", ITEM_BUILDING);
    public static CreativeTabs TAB_ROOFING = new AlmuraCreativeTabs("roofing", "Almura Roofing", ITEM_ROOFING);
    public static CreativeTabs TAB_FLOORING = new AlmuraCreativeTabs("flooring", "Almura Flooring", ITEM_FLOORING);
    public static CreativeTabs TAB_LIGHTING = new AlmuraCreativeTabs("lighting", "Almura Lighting", ITEM_LIGHTING);
    public static CreativeTabs TAB_FURNITURE = new AlmuraCreativeTabs("furniture", "Almura Furniture", ITEM_FURNITURE);
    public static CreativeTabs TAB_DECORATION = new AlmuraCreativeTabs("decoration", "Almura Decoration", ITEM_DECORATION);
    public static CreativeTabs TAB_STORAGE = new AlmuraCreativeTabs("storage", "Almura Storage", ITEM_STORAGE);
    public static CreativeTabs TAB_BARRELS = new AlmuraCreativeTabs("barrels", "Almura Barrels", ITEM_BARRELS);
    public static CreativeTabs TAB_FLOWERS = new AlmuraCreativeTabs("flowers", "Almura Flowers", ITEM_FLOWERS);
    public static CreativeTabs TAB_COMPONENTS = new AlmuraCreativeTabs("components", "Almura Components", ITEM_COMPONENTS);
    public static CreativeTabs TAB_CROPS = new AlmuraCreativeTabs("crops", "Almura Crops", ITEM_CROPS);
    public static CreativeTabs TAB_SEEDS = new AlmuraCreativeTabs("seeds", "Almura Seeds", ITEM_SEEDS);
    public static CreativeTabs TAB_FLAGS = new AlmuraCreativeTabs("flags", "Almura Flags", ITEM_FLAGS);
    public static CreativeTabs TAB_SIGNS = new AlmuraCreativeTabs("signs", "Almura Signs", ITEM_SIGNS);
    public static CreativeTabs TAB_LETTERS = new AlmuraCreativeTabs("letters", "Almura Letters", ITEM_LETTERS);
    public static CreativeTabs TAB_ORES = new AlmuraCreativeTabs("ores", "Almura Ores", ITEM_ORES);
    public static CreativeTabs TAB_FOOD = new AlmuraCreativeTabs("foods", "Almura Food", ITEM_FOODS);
    public static CreativeTabs TAB_DRINK = new AlmuraCreativeTabs("drinks", "Almura Drinks", ITEM_DRINKS);
    public static CreativeTabs TAB_INGREDIENTS = new AlmuraCreativeTabs("ingredients", "Almura Ingredients", ITEM_INGREDIENTS);
    public static CreativeTabs TAB_TOOLS = new AlmuraCreativeTabs("tool", "Almura Tools", ITEM_TOOL);

    public static void fakeStaticLoad() {
    }

    public static CreativeTabs getTabByName(String name) {
        for (CreativeTabs tab : CreativeTabs.creativeTabArray) {
            if (tab instanceof AlmuraCreativeTabs) {
                if (tab.tabLabel.equals(Almura.MOD_ID + "_" + name)) {
                    return tab;
                }
            } else {
                if (tab.tabLabel.equals(name)) {
                    return tab;
                }

            }
        }

        return null;
    }
}
