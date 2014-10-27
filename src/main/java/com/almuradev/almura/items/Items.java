/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items;

import com.almuradev.almura.Tabs;
import net.minecraft.item.Item;

public class Items {

    // Fake items for Creative Menu Tabs 
    public static final Item BUILDING = new BasicItem("building");
    public static final Item ROOFING = new BasicItem("roofing");
    public static final Item LIGHTING = new BasicItem("lighting");
    public static final Item FURNITURE = new BasicItem("furniture");
    public static final Item DECORATION = new BasicItem("decoration");
    public static final Item STORAGE = new BasicItem("storage");
    public static final Item BARRELS = new BasicItem("barrels");
    public static final Item FLOWERS = new BasicItem("flowers");
    public static final Item PLANTS = new BasicItem("plants");
    public static final Item CROPS = new BasicItem("crops");
    public static final Item FLAGS = new BasicItem("flags");
    public static final Item SIGNS = new BasicItem("signs");
    public static final Item LETTERS = new BasicItem("letters");
    public static final Item ORES = new BasicItem("ores");
    public static final Item FOOD = new BasicItem("food");
    public static final Item DRINK = new BasicItem("drink");
    public static final Item INGREDIENTS = new BasicItem("ingredient");
    public static final Item BOTTLES = new BasicItem("bottles");
    public static final Item TOOLS = new BasicItem("tools");
    public static final Item LEGACY = new BasicItem("legacy");

    // Food
    public static final Item FOOD_DOCKTER_COOKIE = new BasicFood("dockter_cookie", true, Tabs.FOOD, 16, 5, 10f, false, true);

    public static void fakeStaticLoad() {
    }
}
