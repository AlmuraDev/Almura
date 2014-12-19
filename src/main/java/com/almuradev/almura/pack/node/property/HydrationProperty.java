/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraft.block.Block;

public class HydrationProperty extends BlockProperty {

    private final int neededPromixity;

    public HydrationProperty(Block block, int neededPromixity) {
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
