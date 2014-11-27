package com.almuradev.almura.core.mixin.impl;

import com.almuradev.almura.core.mixin.Shadow;
import org.lwjgl.BufferChecks;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.GL11;

import com.almuradev.almura.core.mixin.Mixin;
import com.almuradev.almura.core.mixin.Overwrite;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(GL11.class)
public abstract class MixinGL11 {
	private static Field f;
	private static int cachedId;

	@Shadow
	static native void nglBindTexture(int target, int texture, long function_pointer);

	@Overwrite
	public static void glBindTexture(int target, int texture) {
		if (f == null) {
			try {
				f = ContextCapabilities.class.getDeclaredField("glBindTexture");
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		if (texture != cachedId) {
			ContextCapabilities caps = GLContext.getCapabilities();
			f.setAccessible(true);
			try {
				long function_pointer = f.getLong(caps);
				BufferChecks.checkFunctionAddress(function_pointer);
				nglBindTexture(target, texture, function_pointer);
				cachedId = texture;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
