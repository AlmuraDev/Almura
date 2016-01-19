/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.CollisionProperty;

import java.util.Set;

public class CollisionNode extends ToggleableNode<Set<CollisionProperty>> {

    private final Set<CollisionProperty> value;

    public CollisionNode(boolean isEnabled, Set<CollisionProperty> value) {
        super(isEnabled);
        this.value = value;
    }

    @Override
    public Set<CollisionProperty> getValue() {
        return value;
    }
}
