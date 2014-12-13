/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraft.item.Item;

public class ItemProperty implements IProperty<Item> {

    private final Item item;
    private final int amount;
    private final int damage;

    public ItemProperty(Item item, int amount, int damage) {
        this.item = item;
        this.amount = amount;
        this.damage = damage;
    }

    @Override
    public Item getSource() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public int getDamage() {
        return damage;
    }
}
