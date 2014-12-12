/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class BreakProperty extends ToggleableProperty<List<ToolsProperty>> {

    private final List<ToolsProperty> value = Lists.newArrayList();

    public BreakProperty(boolean isEnabled, ToolsProperty... value) {
        super(isEnabled);
        ArrayUtils.add(value, this.value);
    }

    @Override
    public List<ToolsProperty> getValue() {
        return value;
    }
}
