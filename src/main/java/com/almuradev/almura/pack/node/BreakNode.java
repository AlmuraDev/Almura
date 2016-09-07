/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
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

    public ToolsNode getToolByIdentifier(String modid, String identifier) {
        for (ToolsNode toolNode : value) {
            if (identifier.equals("none")) {
                if (toolNode instanceof ToolsNode.OffHand) {
                    return toolNode;
                } else {
                    continue;
                }
            }
            if (toolNode.getTool().modid.equals(modid) && toolNode.getTool().remapped.equals(identifier)) {
                return toolNode;
            }
        }
        return null;
    }
}
