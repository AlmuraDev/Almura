/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraft.entity.Entity;

public class EntityProperty implements IProperty<Class<? extends Entity>> {

    private final Class<? extends Entity> clazz;

    public EntityProperty(Class<? extends Entity> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<? extends Entity> getSource() {
        return clazz;
    }
}
