/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import com.almuradev.content.type.block.component.aabb.BlockAABB;
import net.minecraft.block.Block;

public abstract class AbstractSingleBlockStateDefinition<B extends BlockAABB.Box, C extends BlockAABB.Collision, W extends BlockAABB.WireFrame> extends AbstractBlockStateDefinition<B, C, W> {
    protected AbstractSingleBlockStateDefinition(final AbstractBlockStateDefinitionBuilder<? extends BlockStateDefinition, ? extends AbstractBlockStateDefinitionBuilder> builder) {
        super(builder);
    }

    public void fill(final Block block) {
        this.hardness.ifPresent(hardness -> block.setHardness((float) hardness));
        this.lightEmission.ifPresent(emission -> block.setLightLevel((float) emission));
        this.lightOpacity.ifPresent(block::setLightOpacity);
        this.resistance.ifPresent(resistance -> block.setResistance((float) resistance));
    }
}
