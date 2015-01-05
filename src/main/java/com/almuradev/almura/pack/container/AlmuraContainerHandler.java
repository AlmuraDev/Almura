package com.almuradev.almura.pack.container;

import com.almuradev.almura.pack.node.ContainerNode;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.world.World;

public class AlmuraContainerHandler implements IGuiHandler {
    public static final int PACK_CONTAINER_ID = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        final Block block = world.getBlock(x, y, z);

        if (ID == PACK_CONTAINER_ID && block instanceof PackContainerBlock) {
            final ContainerNode containerNode = ((PackContainerBlock) block).getNode(ContainerNode.class);
            if (containerNode != null) {
                final PackContainerTileEntity te = (PackContainerTileEntity) world.getTileEntity(x, y, z);
                if (te != null) {
                    return new ContainerChest(player.inventory, te);
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
                final PackContainerTileEntity te = (PackContainerTileEntity) world.getTileEntity(x, y, z);
                if (te != null) {
                    return new GuiChest(player.inventory, te);
                }
            }
        }

        return null;
    }
}
