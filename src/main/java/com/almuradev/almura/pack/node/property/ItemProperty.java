/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraft.item.Item;

public class ItemProperty implements IProperty<Item> {

    private final Item item;
    private final RangeProperty<Integer> amountProperty;
    private final int damage;

    public ItemProperty(Item item, RangeProperty<Integer> amountProperty, int damage) {
        this.item = item;
        this.amountProperty = amountProperty;
        this.damage = damage;
    }

    @Override
    public Item getSource() {
        return item;
    }

    public RangeProperty<Integer> getAmountProperty() {
        return amountProperty;
    }

    public int getDamage() {
        return damage;
    }
}
