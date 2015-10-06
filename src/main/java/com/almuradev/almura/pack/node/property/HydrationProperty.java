/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import com.almuradev.almura.pack.mapper.GameObject;
import net.minecraft.block.Block;

public class HydrationProperty extends GameObjectProperty {

    private final int neededProximity;

    public HydrationProperty(Block block, int neededProximity) {
        super(new GameObject("minecraft", block, "", 0));
        this.neededProximity = neededProximity;
    }

    public int getNeededProximity() {
        return neededProximity;
    }

    public boolean isInRange(int radius) {
        return radius <= neededProximity;
    }
}
