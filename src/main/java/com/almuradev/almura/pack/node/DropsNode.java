/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.DropProperty;
import com.almuradev.almura.pack.node.property.ItemProperty;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class DropsNode implements INode<List<DropProperty>> {

    private final List<DropProperty> value = Lists.newArrayList();

    public DropsNode(DropProperty... value) {
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public List<DropProperty> getValue() {
        return value;
    }
}
