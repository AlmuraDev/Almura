/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.util;

import net.minecraft.client.renderer.block.model.BakedQuadRetextured;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class QuadUtil {

    public static void remapQuad(BakedQuadRetextured quad, TextureAtlasSprite destroyTexture, TextureAtlasSprite modelTexture) {
        for (int i = 0; i < 4; ++i)
        {
            int j = quad.getFormat().getIntegerSize() * i;
            int uvIndex = quad.getFormat().getUvOffsetById(0) / 4;

            final int rawU = quad.getVertexData()[j + uvIndex];
            final int rawV = quad.getVertexData()[j + uvIndex + 1];

            float u = Float.intBitsToFloat(rawU);
            float v = Float.intBitsToFloat(rawV);

            float interpolatedU = destroyTexture.getInterpolatedU(modelTexture.getUnInterpolatedU(u));
            float interpolatedV = destroyTexture.getInterpolatedV(modelTexture.getUnInterpolatedV(v));

            quad.getVertexData()[j + uvIndex] = Float.floatToRawIntBits(interpolatedU);
            quad.getVertexData()[j + uvIndex + 1] = Float.floatToRawIntBits(interpolatedV);


            /**
             * Almura's UV generation code
             *
             * float textureU = ((texture.x + (vertex.u * texture.width)) / (float) sprite.getIconWidth()) * 16;
             * float textureV = ((texture.y + (vertex.v * texture.height)) / (float) sprite.getIconHeight()) * 16;
             * builder.put(e, sprite.getInterpolatedU(textureU), sprite.getInterpolatedV(textureV), 0f, 1f);
             */
        }
    }
}
