/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.RangeProperty;

public class ConsumptionNode implements INode<Boolean> {

    private final boolean isEnabled;
    private final RangeProperty<Integer> foodRange;
    private final RangeProperty<Float> saturationRange, healthRange;
    private final boolean alwaysEdible, wolfFavorite;

    public ConsumptionNode(boolean isEnabled, RangeProperty<Integer> foodRange, RangeProperty<Float> saturationRange,
                           RangeProperty<Float> healthRange, boolean alwaysEdible, boolean wolfFavorite) {
        this.isEnabled = isEnabled;
        this.foodRange = foodRange;
        this.saturationRange = saturationRange;
        this.healthRange = healthRange;
        this.alwaysEdible = alwaysEdible;
        this.wolfFavorite = wolfFavorite;
    }

    @Override
    public Boolean getValue() {
        return isEnabled;
    }

    public RangeProperty<Float> getHealthRange() {
        return healthRange;
    }

    public RangeProperty<Integer> getFoodRange() {
        return foodRange;
    }

    public RangeProperty<Float> getSaturationRange() {
        return saturationRange;
    }

    public boolean isAlwaysEdible() {
        return alwaysEdible;
    }

    public boolean isWolfFavorite() {
        return wolfFavorite;
    }
}
