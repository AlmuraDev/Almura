/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

public class Achievements {

    public static Achievement almuraInstalled;
    public static Achievement foodCrafting;
    public static Achievement tacoFever;

    public static AchievementPage page;

    public static void loadAchievements() {

        almuraInstalled = new Achievement("achievement.almura_installed", "almura_installed", 0, 0, new ItemStack(GameRegistry.findItem(Almura.MOD_ID, "Decoration\\CEP_Priest_goddess")), null).setSpecial(); //Super Parent
        almuraInstalled.registerStat();

        //Setup == "lang file value", "generic_name", x-coordinate on page, y-coordinate on page, ItemStack Icon Image, Tree Parent

        foodCrafting = new Achievement("achievement.almura_food", "almura_food", 1, -3, new ItemStack(GameRegistry.findItem(Almura.MOD_ID, "Food\\pizza_deluxe")), almuraInstalled).registerStat(); //Parent
        tacoFever = new Achievement("achievement.almura_food_taco", "almura_food_taco", 3, -2, new ItemStack(GameRegistry.findItem(Almura.MOD_ID, "Food\\southern_taco_beef")), foodCrafting).registerStat(); //Childtac

        page = new AchievementPage("Almura", almuraInstalled, foodCrafting, tacoFever); //Register Everything.
    }

    public static void registerPage() {
        AchievementPage.registerAchievementPage(page);
    }
}
