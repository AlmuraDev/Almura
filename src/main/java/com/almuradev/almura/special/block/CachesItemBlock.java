/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.special.block;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.List;

public class CachesItemBlock extends ItemBlock {

    public CachesItemBlock(Block block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List tooltip, boolean p_77624_4_) {
        if (itemStack.getTagCompound() == null) {
            tooltip.add("Cache Limit: " + ((CachesBlock) this.blockInstance).getCacheLimit());
        }
    }
}
