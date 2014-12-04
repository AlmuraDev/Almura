/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property;

import com.almuradev.almura.pack.crop.stage.property.source.CollisionSource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class CollisionProperty implements ToggleableProperty<List<CollisionSource>> {

    private final boolean enabled;
    private final List<CollisionSource> value = Lists.newArrayList();

    public CollisionProperty(boolean enabled, CollisionSource... value) {
        this.enabled = enabled;
        ArrayUtils.add(value, this.value);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<CollisionSource> getValue() {
        return value;
    }
}
