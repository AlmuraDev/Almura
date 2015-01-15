/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(ShapelessRecipes.class)
public abstract class MixinShapelessRecipes {
    @Shadow private List recipeItems;

    @Overwrite
    @SuppressWarnings("unchecked")
    public boolean matches(InventoryCrafting craftingInventory, World world) {
        final ArrayList buffer = new ArrayList(recipeItems);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                final ItemStack slotStack = craftingInventory.getStackInRowAndColumn(j, i);

                if (slotStack != null) {
                    boolean isWithinGrid = false;

                    for (Object obj : buffer) {
                        ItemStack recipeStack = (ItemStack) obj;

                        if (slotStack.getItem() != recipeStack.getItem() || slotStack.stackSize < recipeStack.stackSize
                            || slotStack.getItemDamage() != 32767 && slotStack.getItemDamage() != recipeStack.getItemDamage()) {
                            continue;
                        }

                        isWithinGrid = true;
                        buffer.remove(recipeStack);
                        break;
                    }

                    if (!isWithinGrid) {
                        break;
                    }
                }
            }
        }

        return buffer.isEmpty();
    }
}
