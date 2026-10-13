/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing;

import com.almuradev.almura.feature.machine.processing.client.ProcessingGui;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Opens a processing machine's container (server) and GUI (client); one is registered per {@link MachineType} under its GUI id.
 */
public final class ProcessingGuiHandler implements IGuiHandler {

    @Nullable
    @Override
    public Object getServerGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        return te instanceof ProcessingTileEntity ? new ProcessingContainer(player.inventory, (ProcessingTileEntity) te) : null;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public Object getClientGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        final TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof ProcessingTileEntity)) {
            return null;
        }
        return new ProcessingGui(new ProcessingContainer(player.inventory, (ProcessingTileEntity) te), player.inventory,
                title((ProcessingTileEntity) te, world.getBlockState(pos).getBlock().getLocalizedName()));
    }

    /**
     * The GUI's title: the machine's own GUI title if the lang file has one (gui.almura.machine.&lt;name&gt;.title, e.g. "Kitchen Slicer &amp;
     * Shredder"), otherwise the block's name.
     */
    @SideOnly(Side.CLIENT)
    private static String title(final ProcessingTileEntity machine, final String blockName) {
        final String key = "gui." + machine.type().id().getNamespace() + "." + machine.type().id().getPath().replace('/', '.') + ".title";
        return I18n.hasKey(key) ? I18n.format(key) : blockName;
    }
}
