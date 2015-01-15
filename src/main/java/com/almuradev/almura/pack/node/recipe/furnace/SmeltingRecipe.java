/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.recipe.furnace;

import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.node.recipe.ISmeltingRecipe;
import net.minecraft.item.ItemStack;

public class SmeltingRecipe implements ISmeltingRecipe {

    private final Pack pack;
    private final String name;
    private final int id;
    private final ItemStack inputStack, outputStack;
    private final float smeltExperience;

    public SmeltingRecipe(Pack pack, String name, int id, ItemStack inputStack, ItemStack outputStack, float smeltExperience) {
        this.pack = pack;
        this.name = name;
        this.id = id;
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
    public boolean matches(ItemStack input, ItemStack output) {
        return ItemStack.areItemStacksEqual(input, inputStack) && ItemStack.areItemStacksEqual(output, outputStack);
    }

    @Override
    public Pack getPack() {
        return pack;
    }

    @Override
    public String getOwner() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "SmeltingRecipe{pack= " + pack.getName() + ", name= " + name + ", id= " + id + ", input=" + inputStack + ", output= " + outputStack
               + ", experience= " + smeltExperience + "}";
    }
}
