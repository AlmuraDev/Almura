/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property.source;

import net.minecraft.block.Block;

public class FertilizerSource extends BlockSource {

    private final int amount;

    public FertilizerSource(Block block, int amount) {
        super(block);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
