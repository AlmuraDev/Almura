/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property;

import com.almuradev.almura.pack.property.source.CollisionSource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class CollisionProperty extends ToggleableProperty<List<CollisionSource>> {
    private final List<CollisionSource> value = Lists.newArrayList();

    public CollisionProperty(boolean isEnabled, CollisionSource... value) {
        super(isEnabled);
        ArrayUtils.add(value, this.value);
    }

    @Override
    public List<CollisionSource> getValue() {
        return value;
    }
}
