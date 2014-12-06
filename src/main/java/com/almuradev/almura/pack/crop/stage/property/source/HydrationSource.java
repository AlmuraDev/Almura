/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property.source;

import net.minecraft.block.Block;

public class HydrationSource extends BlockSource {

    private final int neededPromixity;

    public HydrationSource(Block block, int neededPromixity) {
        super(block);
        this.neededPromixity = neededPromixity;
    }

    public int getNeededPromixity() {
        return neededPromixity;
    }

    public boolean isInRange(int radius) {
        return radius <= neededPromixity;
    }
}
