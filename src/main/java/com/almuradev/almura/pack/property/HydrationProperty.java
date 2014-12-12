/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property;

import com.almuradev.almura.pack.property.source.HydrationSource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class HydrationProperty extends ToggleableProperty<List<HydrationSource>> {

    private final List<HydrationSource> value = Lists.newArrayList();

    public HydrationProperty(boolean isEnabled, HydrationSource... value) {
        super(isEnabled);
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public List<HydrationSource> getValue() {
        return value;
    }
}
