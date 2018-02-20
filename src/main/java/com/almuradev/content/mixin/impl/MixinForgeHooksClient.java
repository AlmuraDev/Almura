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

@SideOnly(Side.CLIENT)
@Mixin(ForgeHooksClient.class)
public abstract class MixinForgeHooksClient {

    /**
     * @author Zidane - Chris Sanders
     * @reason Redirect to ModelUtil for damage model bug-fix
     */
    @Overwrite
    public static IBakedModel getDamageModel(IBakedModel ibakedmodel, TextureAtlasSprite texture, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return ModelUtil.getDamageModel(ibakedmodel, texture, state, world, pos);
    }
}
