/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class ToolsProperty implements IProperty<List<DropsProperty>> {

    private final List<DropsProperty> value = Lists.newArrayList();

    public ToolsProperty(DropsProperty... value) {
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public List<DropsProperty> getValue() {
        return value;
    }

    public static class OffHand extends ToolsProperty {

        public OffHand(DropsProperty... value) {
            super(value);
        }
    }
}
