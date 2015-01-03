/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.recipe;

import com.almuradev.almura.pack.Pack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class QuantitiveShapelessRecipes extends ShapelessRecipes {

    private final Pack pack;
    private final String name;
    private final int id;

    public QuantitiveShapelessRecipes(Pack pack, String name, int id, ItemStack stack, List params) {
        super(stack, params);
        this.pack = pack;
        this.name = name;
        this.id = id;
    }

    //TODO Check each Minecraft update
    @Override
    @SuppressWarnings("unchecked")
    public boolean matches(InventoryCrafting craftingInventory, World world) {
        ArrayList arraylist = new ArrayList(this.recipeItems);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                ItemStack itemstack = craftingInventory.getStackInRowAndColumn(j, i);

                if (itemstack != null) {
                    boolean flag = false;

                    for (Object anArraylist : arraylist) {
                        ItemStack itemstack1 = (ItemStack) anArraylist;

                        if (itemstack.getItem() == itemstack1.getItem() && itemstack.stackSize >= itemstack1.stackSize && (
                                itemstack1.getItemDamage() == 32767 || itemstack.getItemDamage() == itemstack1.getItemDamage())) {
                            flag = true;
                            arraylist.remove(itemstack1);
                            break;
                        }
                    }

                    if (!flag) {
                        return false;
                    }
                }
            }
        }

        return arraylist.isEmpty();
    }

    @Override
    public String toString() {
        return "QuantitiveShapelessRecipes{" + "pack= " + pack.getName() + ", name= " + name + ", id= " + id + ", items= " + recipeItems + "}";
    }
}
