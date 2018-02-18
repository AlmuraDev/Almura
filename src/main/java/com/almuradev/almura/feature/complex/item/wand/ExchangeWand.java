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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import net.minecraft.world.World;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public final class ExchangeWand extends WandItem {

    public ExchangeWand() {
        super(new ResourceLocation(Almura.ID, "normal/tool/exchange_wand"), "exchange_wand");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if ((worldIn.isRemote)) {
            Player spongePlayer = (Player) playerIn;
            if (!spongePlayer.hasPermission("almura.item.exchange_wand")) {
                spongePlayer.sendMessage(Text.of(TextColors.WHITE + "Access denied, missing permission: ", TextColors.AQUA + "almura.item.exchange_wand" + TextColors.WHITE, "."));
                return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
            } else {
                playerIn.swingArm(handIn);
                new ExchangeGUI(playerIn, worldIn, playerIn.getPosition()).display();
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));  //Both Server & Client expect a returned value.
    }
}
