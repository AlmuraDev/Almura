/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property.source;

import net.minecraft.entity.Entity;

public class EntitySource implements ISource<Class<? extends Entity>> {

    private final Class<? extends Entity> clazz;

    public EntitySource(Class<? extends Entity> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<? extends Entity> getSource() {
        return clazz;
    }
}
