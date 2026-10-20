/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.harvestsack;

import com.almuradev.almura.feature.harvestsack.client.GuiHarvestSack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Opens the sack held in the hand whose ordinal is passed as {@code x}.
 */
final class HarvestSackGuiHandler implements IGuiHandler {

    @Nullable
    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final EnumHand hand = hand(x);
        return hand == null || !(player.getHeldItem(hand).getItem() instanceof HarvestingSackItem) ? null : new ContainerHarvestSack(player, hand);
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final EnumHand hand = hand(x);
        return hand == null || !(player.getHeldItem(hand).getItem() instanceof HarvestingSackItem) ? null
                : new GuiHarvestSack(new ContainerHarvestSack(player, hand), player.inventory);
    }

    @Nullable
    private static EnumHand hand(final int ordinal) {
        return ordinal >= 0 && ordinal < EnumHand.values().length ? EnumHand.values()[ordinal] : null;
    }
}
