/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property;

import com.almuradev.almura.pack.crop.stage.property.source.FertilizerSource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class FertilizerProperty implements ToggleableProperty<List<FertilizerSource>> {
    private final boolean enabled;
    private final List<FertilizerSource> value = Lists.newArrayList();

    public FertilizerProperty(boolean enabled, FertilizerSource... value) {
        this.enabled = enabled;
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<FertilizerSource> getValue() {
        return value;
    }
}
