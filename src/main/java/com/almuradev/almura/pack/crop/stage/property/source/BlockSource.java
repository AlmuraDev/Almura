/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property.source;

import net.minecraft.block.Block;

public class BlockSource implements ISource<Block> {
    private final Block block;

    public BlockSource(Block block) {
        this.block = block;
    }
    @Override
    public Block getSource() {
        return block;
    }
}
