package com.almuradev.almura.core.mixin.impl;

import org.lwjgl.BufferChecks;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.GL11;

import com.almuradev.almura.core.mixin.Mixin;
import com.almuradev.almura.core.mixin.Overwrite;

@Mixin(GL11.class)
public abstract class MixinGL11 extends GL11 {
	
	private static int cachedId;

	@Overwrite
	public static void glBindTexture(int target, int texture) {
		if (texture != cachedId) {
			ContextCapabilities caps = GLContext.getCapabilities();
			long function_pointer = caps.glBindTexture;
			BufferChecks.checkFunctionAddress(function_pointer);
			nglBindTexture(target, texture, function_pointer);
			cachedId = texture;
		}
	}
}
