/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property;

import com.almuradev.almura.pack.property.source.FertilizerSource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class FertilizerProperty extends ToggleableProperty<List<FertilizerSource>> {

    private final List<FertilizerSource> value = Lists.newArrayList();

    public FertilizerProperty(boolean isEnabled, FertilizerSource... value) {
        super(isEnabled);
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public List<FertilizerSource> getValue() {
        return value;
    }
}
