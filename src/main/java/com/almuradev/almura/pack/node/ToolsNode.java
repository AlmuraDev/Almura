/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.PackKeys;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class ToolsNode implements INode<List<DropsNode>> {
    private final Item tool;
    private final RangeProperty<Float> exhaustion;
    private final List<DropsNode> value = Lists.newArrayList();

    public ToolsNode(Item tool, RangeProperty<Float> exhaustion, DropsNode... value) {
        this.tool = tool;
        this.exhaustion = exhaustion;
        ArrayUtils.addAll(value, this.value);
    }

    public Item getTool() {
        return tool;
    }

    public RangeProperty<Float> getExhaustion() {
        return exhaustion;
    }

    @Override
    public List<DropsNode> getValue() {
        return value;
    }

    public static final class OffHand extends ToolsNode {

        public OffHand(DropsNode... value) {
            super(null, new RangeProperty<>(Float.class, true, PackKeys.EXHAUSTION_CHANGE.getDefaultValue(), PackKeys.EXHAUSTION_CHANGE.getDefaultValue()), value);
        }

        @Override
        public Item getTool() {
            throw new UnsupportedOperationException("OffHand node has no tool!");
        }
    }
}
