/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import com.almuradev.almura.pack.mapper.GameObject;

public class ReplacementProperty implements IProperty<RangeProperty<Double>> {
    private final GameObject replacementObj, withObj;
    private final RangeProperty<Double> chanceRange;

    public ReplacementProperty(GameObject replacementObj, GameObject withObj, RangeProperty<Double> chanceRange) {
        this.replacementObj = replacementObj;
        this.withObj = withObj;
        this.chanceRange = chanceRange;
    }

    @Override
    public RangeProperty<Double> getSource() {
        return chanceRange;
    }

    public GameObject getReplacementObj() {
        return replacementObj;
    }

    public GameObject getWithObj() {
        return withObj;
    }
}
