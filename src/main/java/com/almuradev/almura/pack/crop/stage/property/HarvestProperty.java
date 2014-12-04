/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class HarvestProperty implements ToggleableProperty<List<ToolsProperty>> {

    private final boolean enabled;
    private final List<ToolsProperty> value = Lists.newArrayList();

    public HarvestProperty(boolean enabled, ToolsProperty... value) {
        this.enabled = enabled;
        ArrayUtils.add(value, this.value);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<ToolsProperty> getValue() {
        return value;
    }
}
