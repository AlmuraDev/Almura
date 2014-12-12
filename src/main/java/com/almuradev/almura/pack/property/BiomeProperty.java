/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property;

import com.almuradev.almura.pack.property.source.BiomeSource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class BiomeProperty extends ToggleableProperty<List<BiomeSource>> {
    private final List<BiomeSource> value = Lists.newArrayList();

    public BiomeProperty(boolean isEnabled, BiomeSource... value) {
        super(isEnabled);
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public List<BiomeSource> getValue() {
        return value;
    }
}
