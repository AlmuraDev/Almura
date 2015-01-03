/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.mapper.GameObject;

public class SoilNode implements INode<BiomeNode> {

    private final GameObject soil;
    private final BiomeNode value;

    public SoilNode(GameObject soil, BiomeNode value) {
        this.soil = soil;
        this.value = value;
    }

    @Override
    public BiomeNode getValue() {
        return value;
    }

    public GameObject getSoil() {
        return soil;
    }
}
