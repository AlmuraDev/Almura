/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.recipe.furnace;

import com.almuradev.almura.recipe.ISmeltRecipe;
import net.minecraft.item.ItemStack;

public class SmeltRecipes implements ISmeltRecipe {

    private final ItemStack inputStack, outputStack;
    private final float smeltExperience;

    public SmeltRecipes(ItemStack inputStack, ItemStack outputStack, float smeltExperience) {
        this.inputStack = inputStack;
        this.outputStack = outputStack;
        this.smeltExperience = smeltExperience;
    }

    @Override
    public ItemStack getInput() {
        return inputStack;
    }

    @Override
    public ItemStack getOutput() {
        return outputStack;
    }

    @Override
    public float getSmeltExperience() {
        return smeltExperience;
    }

    @Override
    public String toString() {
        return "SmeltRecipes {input=" + inputStack + ", output= " + outputStack + ", experience= " + smeltExperience + "}";
    }

    @Override
    public boolean checkMultiQuantity() {
        return false;
    }

    @Override
    public void setCheckMultiQuantity(boolean checkMultiQuantity) {
    }
}
