/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.recipe.furnace;

import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.node.FuelNode;
import cpw.mods.fml.common.IFuelHandler;
import net.minecraft.item.ItemStack;

public class FuelHandler implements IFuelHandler {

    @Override
    public int getBurnTime(ItemStack fuel) {
        if (fuel.getItem() instanceof INodeContainer) {
            final FuelNode fuelNode = ((INodeContainer) fuel.getItem()).getNode(FuelNode.class);
            if (fuelNode != null) {
                return fuelNode.getValue();
            }
        }

        return 0;
    }
}
