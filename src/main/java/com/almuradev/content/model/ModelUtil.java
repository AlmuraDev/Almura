/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model;

import com.almuradev.content.model.obj.OBJBakedModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ModelUtil {

    private ModelUtil() {
    }

    public static IBakedModel getDamageModel(IBakedModel originalBakedModel, TextureAtlasSprite damageSprite, IBlockState state, IBlockAccess world,
            BlockPos pos) {

        state = state.getBlock().getExtendedState(state, world, pos);

        if (originalBakedModel instanceof OBJBakedModel) {
            return ((OBJBakedModel) originalBakedModel).retextureQuadsFor(damageSprite);
        }

        return (new SimpleBakedModel.Builder(state, originalBakedModel, damageSprite, pos)).makeBakedModel();
    }
}
