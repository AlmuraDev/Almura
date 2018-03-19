/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import com.almuradev.content.type.ContentType;
import com.almuradev.content.type.block.component.aabb.BlockAABB;
import com.almuradev.content.type.block.component.aabb.BlockAABBFactory;
import com.almuradev.content.type.block.component.aabb.NormalBoxFactory;
import com.almuradev.content.type.block.type.container.ContainerBlock;
import com.almuradev.content.type.block.type.crop.CropBlock;
import com.almuradev.content.type.block.type.horizontal.HorizontalBlock;
import com.almuradev.content.type.block.type.horizontal.component.aabb.HorizontalBoxFactory;
import com.almuradev.content.type.block.type.leaf.LeafBlock;
import com.almuradev.content.type.block.type.log.LogBlock;
import com.almuradev.content.type.block.type.normal.NormalBlock;
import com.almuradev.content.type.block.type.sapling.SaplingBlock;
import com.almuradev.content.type.block.type.slab.SlabBlock;
import com.almuradev.content.type.block.type.stair.StairBlock;

/**
 * An enumeration of block types.
 */
@ContentType.MultiType.Name("blocks")
public enum BlockGenre implements ContentType.MultiType<ContentBlock, ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>> {
    /**
     * A block type representing a container block.
     */
    CONTAINER("container", ContainerBlock.Builder.class),
    /**
     * A block type representing a crop block.
     */
    CROP("crop", CropBlock.Builder.class),
    /**
     * A block type representing a horizontal block.
     */
    HORIZONTAL("horizontal", HorizontalBlock.Builder.class, HorizontalBoxFactory.class),
    /**
     * A block type representing a leaf block.
     */
    LEAF("leaf", LeafBlock.Builder.class),
    /**
     * A block type representing a log block.
     */
    LOG("log", LogBlock.Builder.class),
    /**
     * A block type representing a normal block.
     */
    NORMAL("normal", NormalBlock.Builder.class),
    /**
     * A block type representing a sapling block.
     */
    SAPLING("sapling", SaplingBlock.Builder.class),
    /**
     * A block type representing a slab block.
     */
    SLAB("slab", SlabBlock.Builder.class),
    /**
     * A block type representing a stair block.
     */
    STAIR("stair", StairBlock.Builder.class);

    /**
     * The id of this block type.
     *
     * <p>The id is used for identification and loading.</p>
     */
    private final String id;
    /**
     * The builder.
     */
    private final Class<? extends ContentBlock.Builder<? extends ContentBlock, ? extends BlockStateDefinition, ? extends BlockStateDefinition.Builder<? extends BlockStateDefinition>>> builder;
    /**
     * The box factory.
     */
    public final Class<? extends BlockAABBFactory<? extends BlockAABB.Box, ? extends BlockAABB.Collision, ? extends BlockAABB.WireFrame>> boxFactory;

    BlockGenre(final String id, final Class<? extends ContentBlock.Builder<? extends ContentBlock, ? extends BlockStateDefinition, ? extends BlockStateDefinition.Builder<? extends BlockStateDefinition>>> builder) {
        this(id, builder, NormalBoxFactory.class);
    }

    BlockGenre(final String id, final Class<? extends ContentBlock.Builder<? extends ContentBlock, ? extends BlockStateDefinition, ? extends BlockStateDefinition.Builder<? extends BlockStateDefinition>>> builder, final Class<? extends BlockAABBFactory<? extends BlockAABB.Box, ? extends BlockAABB.Collision, ? extends BlockAABB.WireFrame>> boxFactory) {
        this.id = id;
        this.builder = builder;
        this.boxFactory = boxFactory;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>> builder() {
        return (Class<ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>>) this.builder;
    }
}
