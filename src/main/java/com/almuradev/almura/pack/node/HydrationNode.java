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
import java.util.Set;

public class HydrationNode extends ToggleableNode<Set<HydrationProperty>> {

    private final Set<HydrationProperty> value;

    public HydrationNode(boolean isEnabled, Set<HydrationProperty> value) {
        super(isEnabled);
        this.value = value;
    }

    @Override
    public Set<HydrationProperty> getValue() {
        return value;
    }
}
