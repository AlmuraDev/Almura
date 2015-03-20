/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.item;

import com.almuradev.almura.pack.IItemBlockInformation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.List;

public class PackItemBlock extends ItemBlock {

    public PackItemBlock(Block p_i45328_1_) {
        super(p_i45328_1_);
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
        if (blockInstance instanceof IItemBlockInformation) {
            for (String str : ((IItemBlockInformation) blockInstance).getTooltip()) {
                p_77624_3_.add(str);
            }
        }
    }
}
