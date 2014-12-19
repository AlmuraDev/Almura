/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.BiomeProperty;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class BiomeNode extends ToggleableNode<List<BiomeProperty>> {
    private final List<BiomeProperty> value = Lists.newArrayList();

    public BiomeNode(boolean isEnabled, BiomeProperty... value) {
        super(isEnabled);
        ArrayUtils.addAll(value, this.value);
    }

    @Override
    public List<BiomeProperty> getValue() {
        return value;
    }
}
