/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.wand;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeGUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import net.minecraft.world.World;

public final class ExchangeWand extends WandItem {

    public ExchangeWand() {
        super(new ResourceLocation(Almura.ID, "normal/tool/exchange_wand"), "exchange_wand");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if ((playerIn instanceof EntityPlayerMP)) {

            new ExchangeGUI(playerIn, worldIn, playerIn.getPosition()).display();
        }

        return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));  //Both Server & Client expect a returned value.
    }
}
