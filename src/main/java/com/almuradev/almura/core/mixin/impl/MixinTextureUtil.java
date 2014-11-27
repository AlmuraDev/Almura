package com.almuradev.almura.core.mixin.impl;

import com.almuradev.almura.core.mixin.Mixin;
import com.almuradev.almura.core.mixin.Overwrite;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

@Mixin(TextureUtil.class)
public abstract class MixinTextureUtil extends TextureUtil {
    private static int cachedId;

    @Overwrite
    public static void bindTexture(int p_94277_0_) {
        if (cachedId == p_94277_0_) {
            return;
        }
        System.out.println(p_94277_0_);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, p_94277_0_);
        cachedId = p_94277_0_;
    }
}
