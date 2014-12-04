/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property;

import com.almuradev.almura.pack.crop.stage.property.source.BiomeSource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class BiomeProperty implements ToggleableProperty<List<BiomeSource>> {

    private final boolean enabled;
    private final List<BiomeSource> value = Lists.newArrayList();

    public BiomeProperty(boolean enabled, BiomeSource... value) {
        this.enabled = enabled;
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<BiomeSource> getValue() {
        return value;
    }
}
