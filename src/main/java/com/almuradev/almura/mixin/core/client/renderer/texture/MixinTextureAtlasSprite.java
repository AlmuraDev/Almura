/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.client.renderer.texture;

import com.almuradev.almura.mixin.interfaces.IMixinTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextureAtlasSprite.class)
public abstract class MixinTextureAtlasSprite implements IMixinTextureAtlasSprite {

    @Shadow protected float minU;
    @Shadow protected float maxU;
    @Shadow protected float minV;
    @Shadow protected float maxV;

    @Override
    public void setMinU(float minU) {
        this.minU = minU;
    }

    @Override
    public void setMinV(float minV) {
        this.minV = minV;
    }

    @Override
    public void setMaxU(float maxU) {
        this.maxU = maxU;
    }

    @Override
    public void setMaxV(float maxV) {
        this.maxV = maxV;
    }
}
