/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.tree;

import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.mapper.GameObject;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.tree.gen.NormalTreeGenerator;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class Tree implements IPackObject {
    private final Pack pack;
    private final String identifier;
    private final int minTreeHeight;
    private final RangeProperty<Integer> heightVariance;
    private final GameObject wood, leaves;
    private final Optional<GameObject> fruit, hangingFruit;
    private final Optional<RangeProperty<Double>> fruitChance, hangingFruitChance;
    private final NormalTreeGenerator generator;

    public Tree(Pack pack, String identifier, int minTreeHeight, RangeProperty<Integer> heightVariance, GameObject wood, GameObject leaves,
            Optional<GameObject> fruit, Optional<RangeProperty<Double>> fruitChance, Optional<GameObject> hangingFruit,
            Optional<RangeProperty<Double>> hangingFruitChance) {
        this.pack = pack;
        this.identifier = identifier;
        this.minTreeHeight = minTreeHeight;
        this.heightVariance = heightVariance;
        this.wood = wood;
        this.leaves = leaves;
        this.fruit = fruit;
        this.fruitChance = fruitChance;
        this.hangingFruit = hangingFruit;
        this.hangingFruitChance = hangingFruitChance;
        this.generator = new NormalTreeGenerator(this);
    }

    @Override
    public Pack getPack() {
        return pack;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public int getMinTreeHeight() {
        return minTreeHeight;
    }

    public RangeProperty<Integer> getHeightVariance() {
        return heightVariance;
    }

    public GameObject getWood() {
        return wood;
    }

    public GameObject getLeaves() {
        return leaves;
    }

    public Optional<GameObject> getFruit() {
        return fruit;
    }

    public Optional<RangeProperty<Double>> getFruitChance() {
        return fruitChance;
    }

    public Optional<GameObject> getHangingFruit() {
        return hangingFruit;
    }

    public Optional<RangeProperty<Double>> getHangingFruitChance() {
        return hangingFruitChance;
    }

    public NormalTreeGenerator getGenerator() {
        return generator;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tree)) {
            return false;
        }

        final Tree other = (Tree) obj;
        return pack.equals(other.pack) && identifier.equals(other.identifier);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("pack", pack)
                .add("identifier", identifier)
                .add("minTreeHeight", minTreeHeight)
                .add("heightVariance", heightVariance)
                .add("wood", wood)
                .add("leaves", leaves)
                .add("fruit", fruit)
                .add("fruitChance", fruitChance)
                .add("hangingFruit", hangingFruit)
                .add("hangingFruitChance", hangingFruitChance)
                .add("generator", generator)
                .toString();
    }
}
