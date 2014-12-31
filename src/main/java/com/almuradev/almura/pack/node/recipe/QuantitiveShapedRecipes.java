/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

public class QuantitiveShapedRecipes extends ShapedRecipes {

    public QuantitiveShapedRecipes(int width, int length, ItemStack[] stacks, ItemStack result) {
        super(width, length, stacks, result);
    }

    //TODO Check each Minecraft update
    @Override
    public boolean checkMatch(InventoryCrafting craftingInventory, int width, int length, boolean flag) {
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 3; ++l) {
                int i1 = k - width;
                int j1 = l - length;
                ItemStack itemstack = null;

                if (i1 >= 0 && j1 >= 0 && i1 < this.recipeWidth && j1 < this.recipeHeight) {
                    if (flag) {
                        itemstack = this.recipeItems[this.recipeWidth - i1 - 1 + j1 * this.recipeWidth];
                    } else {
                        itemstack = this.recipeItems[i1 + j1 * this.recipeWidth];
                    }
                }

                final ItemStack itemstack1 = craftingInventory.getStackInRowAndColumn(k, l);

                if (itemstack1 != null || itemstack != null) {
                    if (itemstack1 == null || itemstack == null) {
                        return false;
                    }

                    if (itemstack.getItem() != itemstack1.getItem()) {
                        return false;
                    }

                    if (itemstack1.stackSize < itemstack.stackSize) {
                        return false;
                    }

                    if (itemstack.getItemDamage() != 32767 && itemstack.getItemDamage() != itemstack1.getItemDamage()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

}
