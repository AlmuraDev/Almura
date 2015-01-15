/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.recipe.crafting;

import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.node.recipe.IMinecraftRecipe;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import java.util.Arrays;

public class MinecraftShapedRecipes extends ShapedRecipes implements IMinecraftRecipe {

    private final Pack pack;
    private final String name;
    private final int id;

    public MinecraftShapedRecipes(Pack pack, String name, int id, int width, int length, ItemStack[] stacks, ItemStack result) {
        super(width, length, stacks, result);
        this.pack = pack;
        this.name = name;
        this.id = id;
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
        return "MinecraftShapedRecipe{pack= " + pack.getName() + ", name= " + name + ", id= " + id + ", items= " + Arrays.toString(recipeItems)
               + "}";
    }
}
