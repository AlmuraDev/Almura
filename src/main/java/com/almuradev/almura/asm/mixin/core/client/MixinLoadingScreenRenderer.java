/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.client;

import com.almuradev.almura.asm.mixin.accessors.client.MinecraftAccessor;
import com.almuradev.almura.feature.menu.main.AlmuraLoadingScreen;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.MinecraftError;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces vanilla's dirt loading screen ("Loading world", "Preparing spawn area", "Saving world") with the Almura styled one drawn by
 * {@link AlmuraLoadingScreen}. Everything except the drawing itself mirrors vanilla's setLoadingProgress: the 100ms throttle, the
 * framebuffer handling, FML's own loading screen taking priority, and the display update.
 */
@Mixin(LoadingScreenRenderer.class)
public abstract class MixinLoadingScreenRenderer {

    @Shadow @Final private Minecraft mc;
    @Shadow @Final private Framebuffer framebuffer;
    @Shadow private String message;
    @Shadow private String currentlyDisplayedText;
    @Shadow private long systemTime;
    @Shadow private boolean loadingSuccess;

    @Inject(method = "setLoadingProgress", at = @At("HEAD"), cancellable = true)
    private void onSetLoadingProgress(final int progress, final CallbackInfo ci) {
        ci.cancel();

        if (!((MinecraftAccessor) this.mc).accessor$isRunning()) {
            if (!this.loadingSuccess) {
                throw new MinecraftError();
            }
            return;
        }

        final long now = Minecraft.getSystemTime();
        if (now - this.systemTime < 100L) {
            return;
        }
        this.systemTime = now;

        final ScaledResolution resolution = new ScaledResolution(this.mc);
        final int scale = resolution.getScaleFactor();
        final int width = resolution.getScaledWidth();
        final int height = resolution.getScaledHeight();

        if (OpenGlHelper.isFramebufferEnabled()) {
            this.framebuffer.framebufferClear();
        } else {
            GlStateManager.clear(256);
        }

        this.framebuffer.bindFramebuffer(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 100.0D, 300.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -200.0F);

        if (!OpenGlHelper.isFramebufferEnabled()) {
            GlStateManager.clear(16640);
        }

        try {
            // FML's own loading screen (e.g. mod errors) takes priority, as in vanilla
            if (!FMLClientHandler.instance().handleLoadingScreen(resolution)) {
                AlmuraLoadingScreen.draw(this.mc, width, height, this.currentlyDisplayedText, this.message, progress);
            }
        } catch (final java.io.IOException e) {
            throw new RuntimeException(e);
        }

        this.framebuffer.unbindFramebuffer();
        if (OpenGlHelper.isFramebufferEnabled()) {
            this.framebuffer.framebufferRender(width * scale, height * scale);
        }

        this.mc.updateDisplay();
        Thread.yield();
    }
}
