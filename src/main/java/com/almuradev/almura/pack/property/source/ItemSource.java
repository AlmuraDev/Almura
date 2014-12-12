/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property.source;

import net.minecraft.item.Item;

public class ItemSource implements ISource<Item> {

    private final Item item;
    private final int amount;
    private final int damage;
    private final BonusSource bonusSource;

    public ItemSource(Item item, int amount, int damage, BonusSource bonusSource) {
        this.item = item;
        this.amount = amount;
        this.damage = damage;
        this.bonusSource = bonusSource;
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

    public BonusSource getBonusSource() {
        return bonusSource;
    }
}
