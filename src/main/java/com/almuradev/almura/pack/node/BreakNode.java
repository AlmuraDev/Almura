/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import java.util.Set;

public class BreakNode extends ToggleableNode<Set<ToolsNode>> {

    private final Set<ToolsNode> value;

    public BreakNode(boolean isEnabled, Set<ToolsNode> value) {
        super(isEnabled);
        this.value = value;
    }

    @Override
    public Set<ToolsNode> getValue() {
        return value;
    }
}
