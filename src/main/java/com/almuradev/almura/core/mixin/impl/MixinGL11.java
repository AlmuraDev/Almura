/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.impl;

import com.almuradev.almura.core.mixin.Mixin;
import com.almuradev.almura.core.mixin.Overwrite;
import com.almuradev.almura.core.mixin.Shadow;
import org.lwjgl.BufferChecks;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import java.lang.reflect.Field;

@Mixin(GL11.class)
public abstract class MixinGL11 {

    private static Field f;
    private static int cachedId;
    private static long function_pointer;

    @Shadow
    static native void nglBindTexture(int target, int texture, long function_pointer);

    @Overwrite
    public static void glBindTexture(int target, int texture) {
        ContextCapabilities caps = GLContext.getCapabilities();

        if (f == null) {
            try {
                f = ContextCapabilities.class.getDeclaredField("glBindTexture");
                f.setAccessible(true);
                function_pointer = f.getLong(caps);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (texture != cachedId && target == GL11.GL_TEXTURE_2D) {
            BufferChecks.checkFunctionAddress(function_pointer);
            nglBindTexture(target, texture, function_pointer);
            cachedId = texture;
        } else {
            BufferChecks.checkFunctionAddress(function_pointer);
            nglBindTexture(target, texture, function_pointer);
        }
    }
}
