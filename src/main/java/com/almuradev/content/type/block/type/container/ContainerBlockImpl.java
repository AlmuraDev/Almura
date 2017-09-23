/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container;

import com.almuradev.content.type.block.type.container.state.ContainerBlockStateDefinition;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class ContainerBlockImpl extends BlockContainer implements ContainerBlock {

    private final int limit;

    ContainerBlockImpl(final ContainerBlockBuilder builder) {
        this(builder, builder.singleState());
        builder.fill(this);
    }

    private ContainerBlockImpl(final ContainerBlockBuilder builder, final ContainerBlockStateDefinition definition) {
        super((Material) builder.material.get());
        this.limit = builder.limit;
    }

    @Override
    public TileEntity createNewTileEntity(final World world, final int meta) {
        return new ContainerBlockEntity(this.limit);
    }
}
