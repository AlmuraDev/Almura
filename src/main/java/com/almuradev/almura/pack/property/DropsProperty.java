/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property;

import com.almuradev.almura.pack.property.source.ItemSource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class DropsProperty implements IProperty<List<ItemSource>> {

    private final List<ItemSource> value = Lists.newArrayList();

    public DropsProperty(ItemSource... value) {
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public List<ItemSource> getValue() {
        return value;
    }
}
