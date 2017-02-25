/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.mapper.GameObject;
import com.almuradev.almura.pack.node.property.RangeProperty;

public class ToolsNode implements INode<DropsNode> {

    private final GameObject tool;
    private final RangeProperty<Integer> experienceRange;
    private final RangeProperty<Float> exhaustionRange;
    private final DropsNode value;

    public ToolsNode(GameObject tool, RangeProperty<Integer> experienceRange, RangeProperty<Float> exhaustionRange, DropsNode value) {
        this.tool = tool;
        this.experienceRange = experienceRange;
        this.exhaustionRange = exhaustionRange;
        this.value = value;
    }

    public GameObject getTool() {
        return tool;
    }

    public RangeProperty<Integer> getExperienceRange() {
        return experienceRange;
    }

    public RangeProperty<Float> getExhaustionRange() {
        return exhaustionRange;
    }

    @Override
    public DropsNode getValue() {
        return value;
    }

    public static final class OffHand extends ToolsNode {

        public OffHand(RangeProperty<Integer> experienceRange, RangeProperty<Float> exhaustionRange, DropsNode value) {
            super(null, experienceRange, exhaustionRange, value);
        }

        @Override
        public final GameObject getTool() {
            return null;
        }
    }
}
