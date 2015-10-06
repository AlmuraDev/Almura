/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

public class FuelNode extends ToggleableNode<Integer> {

    private final int burnTime;

    public FuelNode(boolean isEnabled, int burnTime) {
        super(isEnabled);
        this.burnTime = burnTime;
    }

    @Override
    public Integer getValue() {
        return burnTime;
    }
}
