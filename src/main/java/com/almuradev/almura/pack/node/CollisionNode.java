/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.CollisionProperty;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class CollisionNode extends ToggleableNode<List<CollisionProperty>> {
    private final List<CollisionProperty> value = Lists.newArrayList();

    public CollisionNode(boolean isEnabled, CollisionProperty... value) {
        super(isEnabled);
        ArrayUtils.add(value, this.value);
    }

    @Override
    public List<CollisionProperty> getValue() {
        return value;
    }
}
