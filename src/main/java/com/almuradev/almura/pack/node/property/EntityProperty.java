/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraft.entity.Entity;

public class EntityProperty implements IProperty<Boolean> {

    private final boolean isEnabled;
    private final Class<? extends Entity> clazz;

    public EntityProperty(boolean isEnabled, Class<? extends Entity> clazz) {
        this.isEnabled = isEnabled;
        this.clazz = clazz;
    }

    @Override
    public Boolean getSource() {
        return isEnabled;
    }

    public Class<? extends Entity> getEntityClass() {
        return clazz;
    }
}
