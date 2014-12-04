/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property;

import com.almuradev.almura.pack.crop.stage.IProperty;
import net.minecraft.item.Item;

public class Drop implements IProperty {
    public final Item item;
    public final int amount;
    public final int damage;

    public Drop(Item item, int amount, int damage) {
        this.item = item;
        this.amount = amount;
        this.damage = damage;
    }
}
