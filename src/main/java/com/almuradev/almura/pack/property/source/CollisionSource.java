/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property.source;

import net.minecraft.entity.Entity;

public class CollisionSource extends EntitySource {

    private final float healthChange;

    public CollisionSource(Class<? extends Entity> clazz, float healthChange) {
        super(clazz);
        this.healthChange = healthChange;
    }

    public float getHealthChange() {
        return healthChange;
    }
}
