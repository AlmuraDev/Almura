/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.tree;

import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.mapper.GameObject;
import com.almuradev.almura.pack.tree.gen.TreeGenerator;
import com.google.common.base.Optional;

public class Tree implements IPackObject {
    private final Pack pack;
    private final String identifier;
    private final int minTreeHeight;
    private final GameObject wood, leaves;
    private final Optional<GameObject> fruit, hangingFruit;
    private final TreeGenerator generator;

    public Tree(Pack pack, String identifier, int minTreeHeight, GameObject wood, GameObject leaves, Optional<GameObject> fruit, Optional<GameObject>
            hangingFruit) {
        this.pack = pack;
        this.identifier = identifier;
        this.minTreeHeight = minTreeHeight;
        this.wood = wood;
        this.leaves = leaves;
        this.fruit = fruit;
        this.hangingFruit = hangingFruit;
        this.generator = new TreeGenerator(this);
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

    public GameObject getWood() {
        return wood;
    }

    public GameObject getLeaves() {
        return leaves;
    }

    public Optional<GameObject> getFruit() {
        return fruit;
    }

    public Optional<GameObject> getHangingFruit() {
        return hangingFruit;
    }

    public TreeGenerator getGenerator() {
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

//    @Override
//    public String toString() {
//        return MoreObjects.toStringHelper(this)
//                .add("pack", pack)
//                .add("identifier", identifier)
//                .add("wood", wood)
//                .add("leaves", leaves)
//                .add("fruit", fruit)
//                .add("hangingFruit", hangingFruit)
//                .add("generator", generator)
//                .toString();
//    }
}
