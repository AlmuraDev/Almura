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

    @Override
    public String toString() {
        return "QuantitiveShapelessRecipes{" + "pack= " + pack.getName() + ", name= " + name + ", id= " + id + ", items= " + recipeItems + "}";
    }
}
