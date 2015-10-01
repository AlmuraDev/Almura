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

import java.util.Set;

public class TreePrefab implements IPackObject {
    private final Pack pack;
    private final String identifier;
    private final int minTreeHeight;
    private final RangeProperty<Integer> heightVariance;
    private final GameObject wood, leaves;
    private final boolean generationEnabled;
    private final Set<FruitPrefab> fruitPrefabs, hangingFruitPrefabs;
    private final NormalTreeGenerator generator;

    public TreePrefab(Pack pack, String identifier, int minTreeHeight, RangeProperty<Integer> heightVariance, GameObject wood, GameObject leaves,
            boolean generationEnabled, Set<FruitPrefab> fruitPrefabs, Set<FruitPrefab> hangingFruitPrefabs) {
        this.pack = pack;
        this.identifier = identifier;
        this.minTreeHeight = minTreeHeight;
        this.heightVariance = heightVariance;
        this.wood = wood;
        this.leaves = leaves;
        this.generationEnabled = generationEnabled;
        this.fruitPrefabs = fruitPrefabs;
        this.hangingFruitPrefabs = hangingFruitPrefabs;
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

    public boolean isGenerationEnabled() {
        return generationEnabled;
    }

    public Set<FruitPrefab> getFruitPrefabs() {
        return fruitPrefabs;
    }

    public Set<FruitPrefab> getHangingFruitPrefabs() {
        return hangingFruitPrefabs;
    }

    public NormalTreeGenerator getGenerator() {
        return generator;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TreePrefab)) {
            return false;
        }

        final TreePrefab other = (TreePrefab) obj;
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
                .add("generationEnabled", generationEnabled)
                .add("fruitPrefabs", fruitPrefabs)
                .add("hangingFruitPrefabs", hangingFruitPrefabs)
                .add("generator", generator)
                .toString();
    }
}
