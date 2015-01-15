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
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class MinecraftShapelessRecipes extends ShapelessRecipes implements IMinecraftRecipe {

    private final Pack pack;
    private final String name;
    private final int id;

    public MinecraftShapelessRecipes(Pack pack, String name, int id, ItemStack stack, List params) {
        super(stack, params);
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
        return "MinecraftShapelessRecipes{pack= " + pack.getName() + ", name= " + name + ", id= " + id + ", items= " + recipeItems + "}";
    }
}
