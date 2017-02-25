/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.BiomeProperty;

import java.util.Set;

public class BiomeNode extends ToggleableNode<Set<BiomeProperty>> {

    private final Set<BiomeProperty> value;

    public BiomeNode(boolean isEnabled, Set<BiomeProperty> value) {
        super(isEnabled);
        this.value = value;
    }

    @Override
    public Set<BiomeProperty> getValue() {
        return value;
    }
}
