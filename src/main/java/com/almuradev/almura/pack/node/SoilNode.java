/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import net.minecraft.block.Block;

public class SoilNode implements INode<BiomeNode> {

    private final Block soil;
    private final BiomeNode value;

    public SoilNode(Block soil, BiomeNode value) {
        this.soil = soil;
        this.value = value;
    }

    @Override
    public BiomeNode getValue() {
        return value;
    }

    public Block getSoil() {
        return soil;
    }
}
