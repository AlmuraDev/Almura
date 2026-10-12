/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.seedprocessor;

import com.almuradev.almura.feature.machine.seedprocessor.client.SeedProcessorGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public final class SeedProcessorGuiHandler implements IGuiHandler {

    @Nullable
    @Override
    public Object getServerGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        return te instanceof SeedProcessorTileEntity ? new SeedProcessorContainer(player.inventory, (SeedProcessorTileEntity) te) : null;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public Object getClientGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        final TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof SeedProcessorTileEntity)) {
            return null;
        }
        return new SeedProcessorGui(new SeedProcessorContainer(player.inventory, (SeedProcessorTileEntity) te), player.inventory,
                world.getBlockState(pos).getBlock().getLocalizedName());
    }
}
