/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.FertilizerProperty;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class FertilizerNode extends ToggleableNode<List<FertilizerProperty>> {

    private final List<FertilizerProperty> value = Lists.newArrayList();

    public FertilizerNode(boolean isEnabled, FertilizerProperty... value) {
        super(isEnabled);
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public List<FertilizerProperty> getValue() {
        return value;
    }
}
