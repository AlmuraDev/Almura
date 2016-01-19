/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.DropProperty;
import com.almuradev.almura.pack.node.property.GameObjectProperty;

import java.util.Set;

public class DecayNode extends ToggleableNode<Set<DropProperty>> {

    private Set<DropProperty> dropProperties;
    private Set<GameObjectProperty> preventDecayProperties;

    public DecayNode(boolean isEnabled, Set<DropProperty> dropProperties, Set<GameObjectProperty> preventDecayProperties) {
        super(isEnabled);
        this.dropProperties = dropProperties;
        this.preventDecayProperties = preventDecayProperties;
    }

    @Override
    public Set<DropProperty> getValue() {
        return dropProperties;
    }

    public Set<GameObjectProperty> getPreventDecayProperties() {
        return preventDecayProperties;
    }
}
