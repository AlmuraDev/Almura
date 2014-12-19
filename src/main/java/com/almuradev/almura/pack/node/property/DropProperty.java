/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;


import net.minecraft.item.Item;

public class DropProperty extends ItemProperty {

    private final BonusProperty<Integer, Integer> bonusProperty;

    public DropProperty(Item item, RangeProperty<Integer> amountProperty, int damage, BonusProperty<Integer, Integer> bonusProperty) {
        super(item, amountProperty, damage);
        this.bonusProperty = bonusProperty;
    }

    public BonusProperty<Integer, Integer> getBonusProperty() {
        return bonusProperty;
    }
}
