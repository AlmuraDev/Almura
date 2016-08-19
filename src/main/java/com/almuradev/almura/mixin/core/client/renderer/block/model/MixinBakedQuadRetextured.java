/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.client.renderer.block.model;

import com.almuradev.almura.mixin.util.QuadUtil;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuadRetextured;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedQuadRetextured.class)
public abstract class MixinBakedQuadRetextured extends BakedQuad {

    @Shadow @Final private TextureAtlasSprite texture;

    private final BakedQuadRetextured this$ = (BakedQuadRetextured) (Object) this;

    // ignore
    public MixinBakedQuadRetextured(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn,
            TextureAtlasSprite spriteIn) {
        super(vertexDataIn, tintIndexIn, faceIn, spriteIn);
    }

    @Overwrite
    private void remapQuad()
    {
        QuadUtil.remapQuad(this$, this.texture, this.sprite);
    }
}
