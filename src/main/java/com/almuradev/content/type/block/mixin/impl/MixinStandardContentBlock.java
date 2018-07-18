/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.mixin.impl;

import com.almuradev.content.type.block.AbstractBlockStateDefinition;
import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import com.almuradev.content.type.block.type.leaf.LeafBlockImpl;
import com.almuradev.content.type.block.type.log.LogBlockImpl;
import com.almuradev.content.type.block.type.normal.NormalBlockImpl;
import com.almuradev.content.type.block.type.sapling.SaplingBlockImpl;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin({
    // horizontal is special
    CropBlockImpl.class,
    LeafBlockImpl.class,
    LogBlockImpl.class,
    NormalBlockImpl.class,
    SaplingBlockImpl.class
})
public abstract class MixinStandardContentBlock extends Block implements ContentBlock {
    public MixinStandardContentBlock(final Material material) {
        super(material);
    }

    @Deprecated
    @Override
    @SuppressWarnings("ConstantConditions")
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final AbstractBlockStateDefinition<?, ?, ?> definition = (AbstractBlockStateDefinition<?, ?, ?>) this.definition(state);
        return definition.box != null ? definition.box.box() : super.getBoundingBox(state, world, pos);
    }

    @Deprecated
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final AbstractBlockStateDefinition<?, ?, ?> definition = (AbstractBlockStateDefinition<?, ?, ?>) this.definition(state);
        return definition.collisionBox != null ? definition.collisionBox.box() : super.getCollisionBoundingBox(state, world, pos);
    }

    @Deprecated
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public AxisAlignedBB getSelectedBoundingBox(final IBlockState state, final World world, final BlockPos pos) {
        final AbstractBlockStateDefinition<?, ?, ?> definition = (AbstractBlockStateDefinition<?, ?, ?>) this.definition(state);
        return definition.wireFrame != null ? definition.wireFrame.box().offset(pos) : super.getSelectedBoundingBox(state, world, pos);
    }
}
