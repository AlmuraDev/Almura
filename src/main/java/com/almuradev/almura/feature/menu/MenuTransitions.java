/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.feature.menu.main.WorldLoadingGui;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.Nullable;

/**
 * Transitions between the menus outside of a world: the first menu after startup fades in from the startup splash's background, and each menu
 * after that fades in over a picture of the one before it. Joining a world or changing dimension fades the same way through the loading screen
 * ("Please wait") and the terrain screen, and from the terrain screen into the world.
 */
@SideOnly(Side.CLIENT)
public final class MenuTransitions implements Witness {

    private static final long STARTUP_FADE_MILLIS = 1200, SCREEN_FADE_MILLIS = 350;
    // Forge's splash background when its settings don't say (white)
    private static final int DEFAULT_SPLASH_BACKGROUND = 0xFFFFFF;

    // The one instance (created with the menu module), for the loading screen, which isn't a GUI screen and so gets no events
    @Nullable private static MenuTransitions instance;

    private boolean startedUp;
    // The picture of the menu being left, in a texture the size of the screen
    private int captureTexture = -1, captureWidth, captureHeight;
    // The fade under way: from the picture or from a colour, starting on the next frame drawn (startMillis -1), 0 length when none
    private boolean fromCapture;
    private int fromColor;
    private long startMillis, lengthMillis;
    // Which was drawn last, a game loop frame (a menu or the world) or the loading screen ("Please wait", "Preparing spawn area"), and the
    // loading screen's own framebuffer
    private boolean menuDrawnSinceLoading, loadingDrawnSinceMenu;
    @Nullable private Framebuffer loadingFramebuffer;

    public MenuTransitions() {
        instance = this;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(final GuiOpenEvent event) {
        final Minecraft minecraft = Minecraft.getMinecraft();
        final GuiScreen next = event.getGui();
        final GuiScreen current = minecraft.currentScreen;
        if (next == null) {
            // Into the world, once the terrain screen closes
            if (isTerrainScreen(current) && this.capture(minecraft.getFramebuffer())) {
                this.fromCapture = true;
                this.start(SCREEN_FADE_MILLIS);
            }
            return;
        }
        // Outside of a world only, except the terrain screens, which open once the client's world exists (joining or changing dimension)
        if (minecraft.world != null && !isTerrainScreen(next)) {
            return;
        }

        if (!this.startedUp) {
            // The first menu: from the splash's background
            this.startedUp = true;
            this.fromCapture = false;
            this.fromColor = splashBackground(minecraft);
            this.start(STARTUP_FADE_MILLIS);
            return;
        }

        if (this.loadingDrawnSinceMenu && this.loadingFramebuffer != null) {
            // From the loading screen, which was drawn into its own framebuffer
            if (this.capture(this.loadingFramebuffer)) {
                this.fromCapture = true;
                this.start(SCREEN_FADE_MILLIS);
            }
            return;
        }
        if (current != null && current != next && this.capture(minecraft.getFramebuffer())) {
            this.fromCapture = true;
            this.start(SCREEN_FADE_MILLIS);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDrawScreen(final GuiScreenEvent.DrawScreenEvent.Post event) {
        this.drawFade(event.getGui().width, event.getGui().height);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderTick(final TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        // A frame of a menu or of the game (the loading screen draws outside of the game loop): the next loading screen fades from it
        this.menuDrawnSinceLoading = true;
        this.loadingDrawnSinceMenu = false;

        // Screens draw the fade themselves; with none open, over the world and its HUD
        final Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.currentScreen == null && this.lengthMillis > 0) {
            minecraft.entityRenderer.setupOverlayRendering();
            final ScaledResolution resolution = new ScaledResolution(minecraft);
            this.drawFade(resolution.getScaledWidth(), resolution.getScaledHeight());
        }
    }

    /**
     * @return Whether the screen is shown while a world's terrain loads: vanilla's, or the one Almura shows in its place
     */
    private static boolean isTerrainScreen(@Nullable final GuiScreen screen) {
        return screen instanceof GuiDownloadTerrain || screen instanceof WorldLoadingGui;
    }

    /**
     * Called by the loading screen before each frame it draws. The first frame after a menu starts a fade from that menu, which the loading
     * screen then plays through on its own frames (vanilla only redraws it every 200ms or so while it waits, too few for a fade).
     *
     * @param framebuffer the loading screen's framebuffer
     * @return Whether a fade is under way
     */
    public static boolean beginLoadingFrame(final Framebuffer framebuffer) {
        final MenuTransitions transitions = instance;
        if (transitions == null) {
            return false;
        }
        transitions.loadingFramebuffer = framebuffer;
        if (transitions.menuDrawnSinceLoading) {
            transitions.menuDrawnSinceLoading = false;
            if (transitions.capture(Minecraft.getMinecraft().getFramebuffer())) {
                transitions.fromCapture = true;
                transitions.start(SCREEN_FADE_MILLIS);
            }
        }
        transitions.loadingDrawnSinceMenu = true;
        return transitions.lengthMillis > 0;
    }

    /**
     * Called by the loading screen after drawing a frame: draws the fade under way over it, if any.
     *
     * @return Whether the fade is still under way
     */
    public static boolean drawLoadingFade(final int width, final int height) {
        return instance != null && instance.drawFade(width, height);
    }

    /**
     * Draws the fade under way over what was drawn, if any.
     *
     * @return Whether it is still under way
     */
    private boolean drawFade(final int width, final int height) {
        if (this.lengthMillis <= 0) {
            return false;
        }
        final long now = Minecraft.getSystemTime();
        if (this.startMillis < 0) {
            this.startMillis = now;
        }
        final float t = (now - this.startMillis) / (float) this.lengthMillis;
        if (t >= 1.0F) {
            this.lengthMillis = 0;
            return false;
        }
        final float alpha = 1.0F - t * t * (3.0F - 2.0F * t);
        this.drawOverlay(width, height, alpha);
        return true;
    }

    private void start(final long lengthMillis) {
        this.startMillis = -1;
        this.lengthMillis = lengthMillis;
    }

    /**
     * Copies what the framebuffer holds (the last frame of the menu or loading screen being left) into the capture texture.
     */
    private boolean capture(@Nullable final Framebuffer framebuffer) {
        if (framebuffer == null || !OpenGlHelper.isFramebufferEnabled()) {
            return false;
        }
        final int width = framebuffer.framebufferWidth, height = framebuffer.framebufferHeight;
        if (this.captureTexture < 0 || width != this.captureWidth || height != this.captureHeight) {
            if (this.captureTexture >= 0) {
                TextureUtil.deleteTexture(this.captureTexture);
            }
            this.captureTexture = TextureUtil.glGenTextures();
            this.captureWidth = width;
            this.captureHeight = height;
            GlStateManager.bindTexture(this.captureTexture);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            GlStateManager.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);
        } else {
            GlStateManager.bindTexture(this.captureTexture);
        }
        framebuffer.bindFramebuffer(false);
        GlStateManager.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
        return true;
    }

    /**
     * Draws the capture (or the fade colour) over the whole screen, at {@code alpha}, leaving the settings it changes as they were.
     */
    private void drawOverlay(final int width, final int height, final float alpha) {
        final boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        final boolean alphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        final boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);

        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        if (this.fromCapture) {
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(this.captureTexture);
            // The capture is upside down (the framebuffer starts at the bottom)
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(0, height, 0).tex(0, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.pos(width, height, 0).tex(1, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.pos(width, 0, 0).tex(1, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.pos(0, 0, 0).tex(0, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        } else {
            GlStateManager.disableTexture2D();
            final float red = (this.fromColor >> 16 & 255) / 255.0F, green = (this.fromColor >> 8 & 255) / 255.0F, blue = (this.fromColor & 255) / 255.0F;
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(0, height, 0).color(red, green, blue, alpha).endVertex();
            buffer.pos(width, height, 0).color(red, green, blue, alpha).endVertex();
            buffer.pos(width, 0, 0).color(red, green, blue, alpha).endVertex();
            buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
        }
        tessellator.draw();

        // Textures are bound through GlStateManager; the next bind by name picks its own again
        GlStateManager.bindTexture(0);
        if (texture) {
            GlStateManager.enableTexture2D();
        } else {
            GlStateManager.disableTexture2D();
        }
        if (!blend) {
            GlStateManager.disableBlend();
        }
        if (alphaTest) {
            GlStateManager.enableAlpha();
        }
        if (depth) {
            GlStateManager.enableDepth();
        }
    }

    /**
     * @return The startup splash's background colour, from Forge's splash settings
     */
    private static int splashBackground(final Minecraft minecraft) {
        final File file = new File(new File(minecraft.gameDir, "config"), "splash.properties");
        if (file.isFile()) {
            try (final InputStream in = new FileInputStream(file)) {
                final Properties properties = new Properties();
                properties.load(in);
                return Integer.decode(properties.getProperty("background", "0xFFFFFF").trim()) & 0xFFFFFF;
            } catch (final Exception ignored) {
                // Unreadable: the default
            }
        }
        return DEFAULT_SPLASH_BACKGROUND;
    }
}
