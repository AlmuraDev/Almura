/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.tree.TreePrefab;

public class TreeNode implements INode<RangeProperty<Double>> {
    private final TreePrefab treePrefab;
    private final RangeProperty<Double> rangeProperty;

    public TreeNode(TreePrefab treePrefab, RangeProperty<Double> rangeProperty) {
        this.treePrefab = treePrefab;
        this.rangeProperty = rangeProperty;
    }

    @Override
    public RangeProperty<Double> getValue() {
        return rangeProperty;
    }

    public TreePrefab getTreePrefab() {
        return treePrefab;
    }
}
