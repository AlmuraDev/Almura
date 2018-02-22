/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.storage;

import com.almuradev.almura.shared.network.handler.DualItemHandlerGui;
import com.almuradev.almura.shared.inventory.DualItemHandlerContainer;
import com.almuradev.almura.shared.tileentity.MultiSlotTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public final class CapabilityDualItemHandlerGuiHandler implements IGuiHandler {

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        final TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te == null || !(te instanceof MultiSlotTileEntity)) {
            return null;
        }

        final IItemHandler teItemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (teItemHandler == null) {
            return null;
        }

        final IItemHandler playerItemHandler = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (playerItemHandler == null) {
            return null;
        }

        return new DualItemHandlerContainer(teItemHandler, playerItemHandler);
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        final Block block = world.getBlockState(pos).getBlock();

        final TileEntity te = world.getTileEntity(pos);
        if (te == null || !(te instanceof MultiSlotTileEntity)) {
            return null;
        }

        final IItemHandler teItemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (teItemHandler == null) {
            return null;
        }

        final IItemHandler playerItemHandler = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (playerItemHandler == null) {
            return null;
        }

        return new DualItemHandlerGui(teItemHandler, playerItemHandler, new TextComponentString(block.getLocalizedName()), new TextComponentTranslation
                ("container.inventory"));
    }
}
