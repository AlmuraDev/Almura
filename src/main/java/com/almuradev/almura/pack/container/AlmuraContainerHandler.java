/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.container;

import com.almuradev.almura.pack.node.ContainerNode;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class AlmuraContainerHandler implements IGuiHandler {

    public static final int PACK_CONTAINER_ID = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        final Block block = world.getBlock(x, y, z);

        if (ID == PACK_CONTAINER_ID && block instanceof PackContainerBlock) {
            final ContainerNode containerNode = ((PackContainerBlock) block).getNode(ContainerNode.class);
            if (containerNode != null) {
                final TileEntity te = world.getTileEntity(x, y, z);
                if (te != null && te instanceof PackContainerTileEntity) {
                    return new ContainerChest(player.inventory, (PackContainerTileEntity) te);
                }
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        final Block block = world.getBlock(x, y, z);

        if (ID == PACK_CONTAINER_ID && block instanceof PackContainerBlock) {
            final ContainerNode containerNode = ((PackContainerBlock) block).getNode(ContainerNode.class);
            if (containerNode != null) {
                final TileEntity te = world.getTileEntity(x, y, z);
                if (te != null && te instanceof PackContainerTileEntity) {
                    return new GuiChest(player.inventory, (PackContainerTileEntity) te);
                }
            }
        }

        return null;
    }
}
