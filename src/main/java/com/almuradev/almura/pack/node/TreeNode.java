/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.tree.Tree;

public class TreeNode implements INode<RangeProperty<Double>> {
    private final Tree tree;
    private final RangeProperty<Double> rangeProperty;

    public TreeNode(Tree tree, RangeProperty<Double> rangeProperty) {
        this.tree = tree;
        this.rangeProperty = rangeProperty;
    }

    @Override
    public RangeProperty<Double> getValue() {
        return rangeProperty;
    }

    public Tree getTree() {
        return tree;
    }
}
