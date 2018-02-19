/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.mixin.impl;

import com.almuradev.content.mixin.iface.IMixinTextureAtlasSprite;
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
    public void setMinU(final float minU) {
        this.minU = minU;
    }

    @Override
    public void setMinV(final float minV) {
        this.minV = minV;
    }

    @Override
    public void setMaxU(final float maxU) {
        this.maxU = maxU;
    }

    @Override
    public void setMaxV(final float maxV) {
        this.maxV = maxV;
    }
}
