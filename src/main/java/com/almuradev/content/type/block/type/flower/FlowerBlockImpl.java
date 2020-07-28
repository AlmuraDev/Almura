/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.flower;

import com.almuradev.almura.asm.mixin.accessors.block.BlockAccessor;
import com.almuradev.content.type.block.type.flower.state.FlowerBlockStateDefinition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public final class FlowerBlockImpl extends BlockBush implements FlowerBlock {
  private final FlowerBlockStateDefinition definition;

  FlowerBlockImpl(final FlowerBlockBuilder builder) {
    super((Material) builder.material.get());
    builder.fill(this);
    this.definition = builder.singleState();
    this.definition.fill(this);
    ((BlockAccessor) (Object) this).accessor$setDisplayOnCreativeTab(null);
  }

  @Override
  public FlowerBlockStateDefinition definition(final IBlockState state) {
    return this.definition;
  }

  @Override
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
    return super.getBoundingBox(state, source, pos).offset(state.getOffset(source, pos));
  }

  @Override
  public EnumOffsetType getOffsetType() {
    return Block.EnumOffsetType.NONE;
  }
}
