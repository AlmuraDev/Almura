/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.HydrationProperty;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class HydrationNode extends ToggleableNode<List<HydrationProperty>> {

    private final List<HydrationProperty> value = Lists.newArrayList();

    public HydrationNode(boolean isEnabled, HydrationProperty... value) {
        super(isEnabled);
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public List<HydrationProperty> getValue() {
        return value;
    }
}
