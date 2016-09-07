/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraft.entity.Entity;

public class CollisionProperty extends EntityProperty {

    private final RangeProperty<Float> healthRange;

    public CollisionProperty(boolean isEnabled, Class<? extends Entity> clazz, RangeProperty<Float> healthRange) {
        super(isEnabled, clazz);
        this.healthRange = healthRange;
    }

    public RangeProperty<Float> getHealthRange() {
        return healthRange;
    }
}
