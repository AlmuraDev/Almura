/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.normal;

import com.almuradev.content.type.item.ItemTooltip;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import javax.annotation.Nullable;

public final class NormalItemImpl extends Item implements NormalItem {
    private final ItemTooltip tooltip = new ItemTooltip.Impl(this);

    NormalItemImpl(final NormalItemBuilder builder) {
        builder.fill(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<String> list, final ITooltipFlag flag) {
        this.tooltip.render(list);
        if (stack.getUnlocalizedName().equalsIgnoreCase("ITEM.ALMURA.NORMAL.TOOL.GRINDER")) {
            //System.out.println("Set Max damage of Grinder");
            //this.setMaxDamage(10);  Cannot Implement yet, receipe question for Zidane.
            // ToDO:  Why is this being called 4 times on the client during startup.
            // ToDo:  RecipeMatcher is hell now days.
        }
    }
}
