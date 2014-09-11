package com.almuradev.almuramod.items;

import com.almuradev.almuramod.AlmuraMod;

import net.minecraft.item.Item;
import net.minecraft.creativetab.CreativeTabs;

public class Items {
      
    // Fake items for Creative Menu Tabs 
    public static final Item ALMURA_BUILDING = new AlmuraItem("almuraBuilding");
    public static final Item ALMURA_ROOFING = new AlmuraItem("almuraRoofing");
    public static final Item ALMURA_LIGHTING = new AlmuraItem("almuraLighting");
    public static final Item ALMURA_FURNITURE = new AlmuraItem("almuraFurniture");    
    public static final Item ALMURA_DECORATION = new AlmuraItem("almuraDecoration");
    public static final Item ALMURA_STORAGE = new AlmuraItem("almuraStorage");
    public static final Item ALMURA_BARRELS = new AlmuraItem("almuraBarrels");
    public static final Item ALMURA_FLOWERS = new AlmuraItem("almuraFlowers");
    public static final Item ALMURA_PLANTS = new AlmuraItem("almuraPlants");
    public static final Item ALMURA_CROPS = new AlmuraItem("almuraCrops");
    public static final Item ALMURA_FLAGS = new AlmuraItem("almuraFlags");
    public static final Item ALMURA_SIGNS = new AlmuraItem("almuraSigns");
    public static final Item ALMURA_LETTERS = new AlmuraItem("almuraLetters");
    public static final Item ALMURA_ORES = new AlmuraItem("almuraOres");
    public static final Item ALMURA_FOOD = new AlmuraItem("almuraFood");
    public static final Item ALMURA_DRINK = new AlmuraItem("almuraDrink");
    public static final Item ALMURA_INGREDIENTS = new AlmuraItem("almuraIngredient");
    public static final Item ALMURA_BOTTLES = new AlmuraItem("almuraBottles");
    public static final Item ALMURA_TOOLS = new AlmuraItem("almuraTools");
    
    // Almura Food
    public static final Item ALMURA_FOOD_DOCKTER_COOKIE = new AlmuraItem("almuraFoodDockterCookie", AlmuraMod.act_Food, 16);
    
    public static void fakeStaticLoad() {}
}
