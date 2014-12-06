/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property;

import com.almuradev.almura.pack.crop.stage.property.source.HydrationSource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class HydrationProperty implements ToggleableProperty<List<HydrationSource>> {

    private final boolean enabled;
    private final List<HydrationSource> value = Lists.newArrayList();

    public HydrationProperty(boolean enabled, HydrationSource... value) {
        this.enabled = enabled;
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<HydrationSource> getValue() {
        return value;
    }
}
