/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraft.block.Block;

public class BlockProperty implements IProperty<Block> {

    private final Block block;

    public BlockProperty(Block block) {
        this.block = block;
    }

    @Override
    public Block getSource() {
        return block;
    }
}
