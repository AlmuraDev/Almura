/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class BlockStateMapper extends StateMapperBase {
    public static final BlockStateMapper INSTANCE = new BlockStateMapper();

    @Override
    protected ModelResourceLocation getModelResourceLocation(final IBlockState state) {
        return new ModelResourceLocation(
                ((SpecialBlockStateBlock) state.getBlock()).blockStateDefinitionLocation(),
                this.getPropertyString(state.getProperties())
        );
    }
}
