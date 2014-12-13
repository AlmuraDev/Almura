/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraft.block.Block;

public class FertilizerProperty extends BlockProperty {

    private final int amount;

    public FertilizerProperty(Block block, int amount) {
        super(block);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
