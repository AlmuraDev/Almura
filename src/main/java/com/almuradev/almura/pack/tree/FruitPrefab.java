/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.tree;

import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.mapper.GameObject;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.google.common.base.Objects;

public class FruitPrefab implements IPackObject {
    private final Pack pack;
    private final String identifier;
    private final GameObject fruitObj;
    private final RangeProperty<Double> fruitChance;

    public FruitPrefab(Pack pack, String identifier, GameObject fruitObj, RangeProperty<Double> fruitChance) {
        this.pack = pack;
        this.identifier = identifier;
        this.fruitObj = fruitObj;
        this.fruitChance = fruitChance;
    }

    @Override
    public Pack getPack() {
        return pack;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public GameObject getFruit() {
        return fruitObj;
    }

    public RangeProperty<Double> getFruitChance() {
        return fruitChance;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FruitPrefab)) {
            return false;
        }

        final FruitPrefab other = (FruitPrefab) obj;
        return pack.equals(other.pack) && identifier.equals(other.identifier);
    }

    @SuppressWarnings("deprecation")
	@Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("pack", pack)
                .add("identifier", identifier)
                .add("fruitObj", fruitObj)
                .add("fruitChance", fruitChance)
                .toString();
    }
}
