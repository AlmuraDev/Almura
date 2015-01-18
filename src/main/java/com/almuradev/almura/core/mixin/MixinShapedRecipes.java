/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin;

import com.almuradev.almura.recipe.IShapedRecipe;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;

@Mixin(ShapedRecipes.class)
public abstract class MixinShapedRecipes implements IShapedRecipe {
    @Shadow private int recipeWidth;
    @Shadow private int recipeHeight;
    @Shadow private ItemStack[] recipeItems;

    @Overwrite
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
        return "ShapedRecipes {items= " + Arrays.toString(recipeItems) + "}";
    }
}
