/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.lang.Languages;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class Tabs {

    public static CreativeTabs BUILDING, ROOFING, LIGHTING, FURNITURE, DECORATION, STORAGE, BARRELS, FLOWERS, PLANTS, CROPS,
            FLAGS, SIGNS, LETTERS, ORES, FOOD, DRINK, INGREDIENTS, BOTTLES, TOOLS, LEGACY;

    static {
        BUILDING = new AlmuraCreativeTabs("building", "Almura Building", com.almuradev.almura.items.Items.BUILDING);

        ROOFING = new AlmuraCreativeTabs("roofing", "Almura Roofing", com.almuradev.almura.items.Items.ROOFING);

        LIGHTING = new AlmuraCreativeTabs("lighting", "Almura Lighting", com.almuradev.almura.items.Items.LIGHTING);

        FURNITURE = new AlmuraCreativeTabs("furniture", "Almura Furniture", com.almuradev.almura.items.Items.FURNITURE);

        DECORATION = new AlmuraCreativeTabs("decoration", "Almura Decoration", com.almuradev.almura.items.Items.DECORATION);

        STORAGE = new AlmuraCreativeTabs("storage", "Almura Storage", com.almuradev.almura.items.Items.STORAGE);

        BARRELS = new AlmuraCreativeTabs("barrels", "Almura Barrels", com.almuradev.almura.items.Items.BARRELS);

        FLOWERS = new AlmuraCreativeTabs("flowers", "Almura Flowers", com.almuradev.almura.items.Items.FLOWERS);

        PLANTS = new AlmuraCreativeTabs("plants", "Almura Plants", com.almuradev.almura.items.Items.PLANTS);

        CROPS = new AlmuraCreativeTabs("crops", "Almura Crops", com.almuradev.almura.items.Items.CROPS);

        FLAGS = new AlmuraCreativeTabs("flags", "Almura Flags", com.almuradev.almura.items.Items.FLAGS);

        SIGNS = new AlmuraCreativeTabs("signs", "Almura Signs", com.almuradev.almura.items.Items.SIGNS);

        LETTERS = new AlmuraCreativeTabs("letters", "Almura Letters", com.almuradev.almura.items.Items.LETTERS);

        ORES = new AlmuraCreativeTabs("ores", "Almura Ores", com.almuradev.almura.items.Items.ORES);

        FOOD = new AlmuraCreativeTabs("food", "Almura Food", com.almuradev.almura.items.Items.FOOD);

        DRINK = new AlmuraCreativeTabs("drinks", "Almura Drinks", com.almuradev.almura.items.Items.DRINKS);

        INGREDIENTS = new AlmuraCreativeTabs("ingredients", "Almura Ingredients", com.almuradev.almura.items.Items.INGREDIENTS);

        BOTTLES = new AlmuraCreativeTabs("bottles", "Almura Bottles", com.almuradev.almura.items.Items.BOTTLES);

        TOOLS = new AlmuraCreativeTabs("tools", "Almura Tools", com.almuradev.almura.items.Items.TOOLS);

        LEGACY = new AlmuraCreativeTabs("legacy", "Almura SMP Content", com.almuradev.almura.items.Items.LEGACY);
    }

    public static void fakeStaticLoad() {
    }

    public static CreativeTabs getTabByName(String name) {
        for (CreativeTabs tab : CreativeTabs.creativeTabArray) {
            if (tab.getTabLabel().equals(name)) {
                return tab;
            }
        }

        return null;
    }

    public static class AlmuraCreativeTabs extends CreativeTabs {
        private final Item displayItem;

        public AlmuraCreativeTabs(String unlocalizedName, String displayName, Item displayItem) {
            super(unlocalizedName);
            Almura.LANGUAGES.put(Languages.ENGLISH_AMERICAN, getTabLabel(), displayName);
            this.displayItem = displayItem;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return displayItem == null ? Items.feather : displayItem;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public String getTranslatedTabLabel() {
            return Almura.MOD_ID.toLowerCase() + "itemGroup." + this.getTabLabel();
        }
    }
}
