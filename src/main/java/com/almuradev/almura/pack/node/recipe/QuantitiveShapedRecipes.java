/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.recipe;

import com.almuradev.almura.pack.Pack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import java.util.Arrays;

public class QuantitiveShapedRecipes extends ShapedRecipes {

    private final Pack pack;
    private final String name;
    private final int id;

    public QuantitiveShapedRecipes(Pack pack, String name, int id, int width, int length, ItemStack[] stacks, ItemStack result) {
        super(width, length, stacks, result);
        this.pack = pack;
        this.name = name;
        this.id = id;
    }

    //TODO Check each Minecraft update
    @Override
    public boolean checkMatch(InventoryCrafting craftingInventory, int width, int length, boolean flag) {
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 3; ++l) {
                int i1 = k - width;
                int j1 = l - length;
                ItemStack recipeStack = null;

                if (i1 >= 0 && j1 >= 0 && i1 < this.recipeWidth && j1 < this.recipeHeight) {
                    if (flag) {
                        recipeStack = this.recipeItems[this.recipeWidth - i1 - 1 + j1 * this.recipeWidth];
                    } else {
                        recipeStack = this.recipeItems[i1 + j1 * this.recipeWidth];
                    }
                }

                final ItemStack slotStack = craftingInventory.getStackInRowAndColumn(k, l);

                if (slotStack != null || recipeStack != null) {
                    if (slotStack == null || recipeStack == null) {
                        return false;
                    }

                    if (slotStack.getItem() != recipeStack.getItem() || slotStack.stackSize < recipeStack.stackSize
                        || slotStack.getItemDamage() != 32767 && slotStack.getItemDamage() != recipeStack.getItemDamage()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "QuantitiveShapedRecipes{" + "pack= " + pack.getName() + ", name= " + name + ", id= " + id + ", items= " + Arrays.toString(recipeItems)
               + "}";
    }
}
