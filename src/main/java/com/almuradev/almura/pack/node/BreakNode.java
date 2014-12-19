/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class BreakNode extends ToggleableNode<List<ToolsNode>> {

    private final List<ToolsNode> value = Lists.newArrayList();

    public BreakNode(boolean isEnabled, ToolsNode... value) {
        super(isEnabled);
        ArrayUtils.add(value, this.value);
    }

    @Override
    public List<ToolsNode> getValue() {
        return value;
    }
}
