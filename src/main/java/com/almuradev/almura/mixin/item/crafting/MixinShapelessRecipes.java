/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.item.crafting;

import com.almuradev.almura.recipe.IShapelessRecipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(ShapelessRecipes.class)
public abstract class MixinShapelessRecipes implements IShapelessRecipe {

    @SuppressWarnings("rawtypes")
	@Shadow
    private List recipeItems;

    private boolean checkMultiQuantity = false;

    @Overwrite
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean matches(InventoryCrafting craftingInventory, World world) {
        final ArrayList buffer = new ArrayList(recipeItems);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                final ItemStack slotStack = craftingInventory.getStackInRowAndColumn(j, i);

                if (slotStack != null) {
                    boolean isWithinGrid = false;

                    final Iterator iter = buffer.iterator();

                    while (iter.hasNext()) {
                        final ItemStack recipeStack = (ItemStack) iter.next();

                        if (slotStack.getItem() != recipeStack.getItem() || (checkMultiQuantity && (slotStack.stackSize < recipeStack.stackSize))
                                || slotStack.getMetadata() != 32767 && slotStack.getMetadata() != recipeStack.getMetadata()) {
                            continue;
                        }

                        isWithinGrid = true;
                        iter.remove();
                        break;
                    }

                    if (!isWithinGrid) {
                        return false;
                    }
                }
            }
        }

        return buffer.isEmpty();
    }

    @Override
    public boolean checkMultiQuantity() {
        return checkMultiQuantity;
    }

    @Override
    public void setCheckMultiQuantity(boolean checkMultiQuantity) {
        this.checkMultiQuantity = checkMultiQuantity;
    }

    @Override
    public String toString() {
        return "ShapelessRecipes {items= " + recipeItems + "}";
    }
}
