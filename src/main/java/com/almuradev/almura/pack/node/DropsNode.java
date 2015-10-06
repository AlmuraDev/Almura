/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.DropProperty;

import java.util.Set;

public class DropsNode implements INode<Set<DropProperty>> {

    private final Set<DropProperty> value;

    public DropsNode(Set<DropProperty> value) {
        this.value = value;
    }

    @Override
    public Set<DropProperty> getValue() {
        return value;
    }
}
