/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.ReplacementProperty;

import java.util.Set;

public class SpreadNode implements INode<Set<ReplacementProperty>> {
    private final boolean enabled;
    private final Set<ReplacementProperty> replacementProperties;

    public SpreadNode(boolean enabled, Set<ReplacementProperty> replacementProperties) {
        this.enabled = enabled;
        this.replacementProperties = replacementProperties;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Set<ReplacementProperty> getValue() {
        return replacementProperties;
    }
}
