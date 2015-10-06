/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.container.StateProperty;
import com.google.common.base.Optional;

import java.util.Set;

public class ContainerNode implements INode<Set<StateProperty>> {

    private final String title;
    private final int size;
    private final int maxStackSize;
    private final Set<StateProperty> value;

    public ContainerNode(String title, int size, int maxStackSize, Set<StateProperty> value) {
        this.title = title;
        this.size = size;
        this.maxStackSize = maxStackSize;
        this.value = value;
    }

    @Override
    public Set<StateProperty> getValue() {
        return value;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public Optional<StateProperty> getByIdentifier(String identifier) {
        for (StateProperty prop : value) {
            if (prop.getIdentifier().equals(identifier)) {
                return Optional.of(prop);
            }
        }

        return Optional.absent();
    }
}
