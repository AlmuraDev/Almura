/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.GameObjectProperty;

import java.util.Set;

public class FertilizerNode extends ToggleableNode<Set<GameObjectProperty>> {

    private final Set<GameObjectProperty> value;

    public FertilizerNode(boolean isEnabled, Set<GameObjectProperty> value) {
        super(isEnabled);
        this.value = value;
    }

    @Override
    public Set<GameObjectProperty> getValue() {
        return value;
    }
}
