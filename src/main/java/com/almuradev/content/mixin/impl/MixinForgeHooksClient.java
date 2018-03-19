/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.mixin.impl;

import com.almuradev.content.model.ModelUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ForgeHooksClient.class, remap = false)
@SideOnly(Side.CLIENT)
public abstract class MixinForgeHooksClient {
    /**
     * @author Zidane - Chris Sanders
     * @reason Redirect to ModelUtil for damage model bug-fix
     */
    @Overwrite
    public static IBakedModel getDamageModel(final IBakedModel ibakedmodel, final TextureAtlasSprite texture, final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return ModelUtil.getDamageModel(ibakedmodel, texture, state, world, pos);
    }
}
