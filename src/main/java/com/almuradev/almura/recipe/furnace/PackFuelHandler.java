/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.recipe.furnace;

import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.node.FuelNode;
import cpw.mods.fml.common.IFuelHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class PackFuelHandler implements IFuelHandler {

    @Override
    public int getBurnTime(ItemStack fuel) {
        INodeContainer nodeContainer = null;

        if (fuel.getItem() instanceof ItemBlock) {
            final ItemBlock itemBlock = (ItemBlock) fuel.getItem();
            final Block block = itemBlock.blockInstance;
            if (block instanceof INodeContainer) {
                nodeContainer = (INodeContainer) block;
            }
        } else if (fuel.getItem() instanceof INodeContainer) {
            nodeContainer = (INodeContainer) fuel.getItem();
        }

        if (nodeContainer != null) {
            final FuelNode fuelNode = nodeContainer.getNode(FuelNode.class);
            if (fuelNode != null && fuelNode.isEnabled()) {
                return fuelNode.getValue();
            }
        }

        return 0;
    }
}
