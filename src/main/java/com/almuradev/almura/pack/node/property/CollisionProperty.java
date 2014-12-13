/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraft.entity.Entity;

public class CollisionProperty extends EntityProperty {

    private final float healthChange;

    public CollisionProperty(Class<? extends Entity> clazz, float healthChange) {
        super(clazz);
        this.healthChange = healthChange;
    }

    public float getHealthChange() {
        return healthChange;
    }
}
