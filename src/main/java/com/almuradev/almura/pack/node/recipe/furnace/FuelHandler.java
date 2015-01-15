/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.recipe.furnace;

import com.almuradev.almura.pack.node.recipe.IRecipe;
import com.almuradev.almura.pack.node.recipe.ISmeltingRecipe;
import com.almuradev.almura.pack.node.recipe.RecipeManager;
import com.google.common.base.Optional;
import cpw.mods.fml.common.IFuelHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class FuelHandler implements IFuelHandler {

    @Override
    public int getBurnTime(ItemStack fuel) {
        for (WorldServer world : DimensionManager.getWorlds()) {
            for (Object obj : world.loadedTileEntityList) {
                if (obj instanceof TileEntityFurnace) {
                    final TileEntityFurnace te = (TileEntityFurnace) obj;
                    final ItemStack furnaceFuelStack = te.getStackInSlot(1);

                    if (ItemStack.areItemStacksEqual(fuel, furnaceFuelStack)) {
                        final Optional<ISmeltingRecipe>
                                smeltingRecipe = RecipeManager.findRecipe(ISmeltingRecipe.class, te.getStackInSlot(0), te.getStackInSlot(2));

                        if (!smeltingRecipe.isPresent()) {
                            return 0;
                        }
                    }
                }
            }
        }
    }
}
