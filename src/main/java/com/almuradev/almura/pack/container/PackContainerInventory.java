package com.almuradev.almura.pack.container;

import com.almuradev.almura.pack.node.ContainerNode;
import net.minecraft.inventory.InventoryBasic;

public class PackContainerInventory extends InventoryBasic {
    private final ContainerNode containerNode;

    public PackContainerInventory(ContainerNode containerNode) {
        super(containerNode.getTitle(), containerNode.useDisplayNameOfContainerAsTitle(), containerNode.getSize());
        this.containerNode = containerNode;
    }

    @Override
    public int getInventoryStackLimit() {
        return containerNode.getMaxStackSize();
    }
}
