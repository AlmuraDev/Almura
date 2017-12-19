/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.screen;

import com.almuradev.almura.Almura;
import com.almuradev.almura.asm.StaticAccess;
import com.almuradev.almura.shared.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public abstract class PanoramicScreen extends SimpleScreen {

    private static final Path PATH_ASSETS_ALMURA = Paths.get(".").resolve("assets").resolve(Almura.ID);
    private static final Map<Daypart, List<PanoramicBundle>> bundles = new HashMap<>();
    private static final Random random = new Random();
    private final PanoramicBundle bundle;
    private ResourceLocation backgroundTexture;
    private int timer;

    static {
        final Path basePath = PATH_ASSETS_ALMURA.resolve("textures/gui/title/background");
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**_0.png");
        for (Daypart daypart : Daypart.values()) {
            final Path daypartBasePath = basePath.resolve(daypart.name().toLowerCase());
            if (!Files.exists(daypartBasePath)) {
                continue;
            }
            final DirectoryStream<Path> daypartPaths;
            try {
                daypartPaths = Files.newDirectoryStream(daypartBasePath);
                for (Path path : daypartPaths) {
                    if (matcher.matches(path)) {
                        bundles.computeIfAbsent(daypart, k -> new ArrayList<>());
                        try {
                            bundles.get(daypart).add(new PanoramicBundle(path));
                        } catch (FileNotFoundException e) {
                            StaticAccess.logger.warn("Unable to load panoramic for [" + path + "]", e);
                        }
                    }
                }
            } catch (IOException e) {
                StaticAccess.logger.warn("Unable to load panoramics.", e);
            }
        }
    }

    public PanoramicScreen() {
        this(null);
    }

    public PanoramicScreen(@Nullable SimpleScreen parent) {
        super(parent);

        // Get the list of bundles based on the current daypart
        final List<PanoramicBundle> daypartBundles = bundles.get(getDaypart());

        // Add the vanilla panorama locations if we have none to load, otherwise choose a random one
        if (bundles.isEmpty() || daypartBundles == null || daypartBundles.isEmpty()) {
            StaticAccess.logger.warn("No custom panoramics found. Defaulting to vanilla panoramic.");
            this.bundle = new PanoramicBundle(new ResourceLocation("textures/gui/title/background/panorama_0.png"));
        } else {
            this.bundle = daypartBundles.get(random.nextInt(daypartBundles.size()));
        }

        this.backgroundTexture = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("background", new DynamicTexture(256, 256));

        // Don't show the vanilla background
        this.guiscreenBackground = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        GlStateManager.disableAlpha();
        this.renderSkybox(partialTick);
        GlStateManager.enableAlpha();
        this.drawGradientRect(0, 0, this.width, this.height, -2130706433, 16777215);
        this.drawGradientRect(0, 0, this.width, this.height, 0, Integer.MIN_VALUE);
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    private void renderSkybox(float partialTicks) {
        this.timer++;

        Minecraft.getMinecraft().getFramebuffer().unbindFramebuffer();
        GlStateManager.viewport(0, 0, 256, 256);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();

        this.drawPanorama(tessellator, buffer, partialTicks);
        this.rotateAndBlurSkybox(tessellator, buffer);
        this.rotateAndBlurSkybox(tessellator, buffer);
        this.rotateAndBlurSkybox(tessellator, buffer);
        this.rotateAndBlurSkybox(tessellator, buffer);
        this.rotateAndBlurSkybox(tessellator, buffer);
        this.rotateAndBlurSkybox(tessellator, buffer);
        this.rotateAndBlurSkybox(tessellator, buffer);
        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
        GlStateManager.viewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        final float ratio = 120.0F / (float) (this.width > this.height ? this.width : this.height);
        final float w = (float) this.height * ratio / 256.0F;
        final float h = (float) this.width * ratio / 256.0F;
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(0.0D, (double) this.height, (double) this.zLevel)
                .tex((double) (0.5F - w), (double) (0.5F + h))
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .endVertex();
        buffer.pos((double) this.width, (double) this.height, (double) this.zLevel)
                .tex((double) (0.5F - w), (double) (0.5F - h))
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .endVertex();
        buffer.pos((double) this.width, 0.0D, (double) this.zLevel)
                .tex((double) (0.5F + w), (double) (0.5F - h))
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .endVertex();
        buffer.pos(0.0D, 0.0D, (double) this.zLevel)
                .tex((double) (0.5F + w), (double) (0.5F + h))
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .endVertex();
        tessellator.draw();
    }

    private void rotateAndBlurSkybox(Tessellator tessellator, BufferBuilder buffer) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.backgroundTexture);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 256, 256);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.colorMask(true, true, true, false);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        GlStateManager.disableAlpha();

        for (int i = 0; i < 3; ++i) {
            final int blurIntensity = (int) ((1.0F / (i + 1)) * 255.0F);
            final float rotation = (i - 1) / 256.0F;
            buffer.pos((double) this.width, (double) this.height, (double) this.zLevel)
                    .tex((double) (0.0F + rotation), 1.0D)
                    .color(255, 255, 255, blurIntensity)
                    .endVertex();
            buffer.pos((double) this.width, 0.0D, (double) this.zLevel)
                    .tex((double) (1.0F + rotation), 1.0D)
                    .color(255, 255, 255, blurIntensity)
                    .endVertex();
            buffer.pos(0.0D, 0.0D, (double) this.zLevel)
                    .tex((double) (1.0F + rotation), 0.0D)
                    .color(255, 255, 255, blurIntensity)
                    .endVertex();
            buffer.pos(0.0D, (double) this.height, (double) this.zLevel)
                    .tex((double) (0.0F + rotation), 0.0D)
                    .color(255, 255, 255, blurIntensity)
                    .endVertex();
        }

        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
    }

    private void drawPanorama(Tessellator tessellator, BufferBuilder buffer, float partialTicks) {
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        for (int i = 0; i < 64; ++i) {
            GlStateManager.pushMatrix();
            final float x = ((float) (i % 8) / 8.0F - 0.5F) / 64.0F;
            final float y = ((float) (i / 8) / 8.0F - 0.5F) / 64.0F;
            GlStateManager.translate(x, y, 0.0F);
            GlStateManager.rotate(MathHelper.sin(((float) this.timer + partialTicks) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-((float) this.timer + partialTicks) * 0.1F, 0.0F, 1.0F, 0.0F);

            for (int j = 0; j < 6; ++j) {
                GlStateManager.pushMatrix();

                switch (j) {
                    case 1:
                        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                        break;
                    case 2:
                        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                        break;
                    case 3:
                        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                        break;
                    case 4:
                        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                        break;
                    case 5:
                        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                        break;
                }

                Minecraft.getMinecraft().getTextureManager().bindTexture(this.bundle.getResourceLocations()[j]);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                final int alpha = 255 / (i + 1);
                buffer.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, alpha).endVertex();
                buffer.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, alpha).endVertex();
                buffer.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, alpha).endVertex();
                buffer.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, alpha).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }

        buffer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
    }

    private static Daypart getDaypart() {
        return getDaypart(LocalTime.now());
    }

    private static Daypart getDaypart(LocalTime time) {
        final int hour = time.getHour();

        if (MathUtil.withinRange(hour, 12, 15)) {
            return Daypart.NOON;
        } else if (MathUtil.withinRange(hour, 16, 19)) {
            return Daypart.DUSK;
        } else if (hour >= 20 && hour - 12 <= 5) {
            return Daypart.NIGHT;
        } else {
            return Daypart.DAWN;
        }
    }

    private enum Daypart {
        DUSK,
        DAWN,
        NOON,
        NIGHT
    }

    private static class PanoramicBundle {
        private final ResourceLocation[] resources = new ResourceLocation[6];

        public PanoramicBundle(Path path) throws FileNotFoundException {
            boolean incomplete = false;
            for (int i = 1; i < 6; i++) {
                incomplete = !Files.exists(Paths.get(path.toString().replace("_0", "_" + i)));
                if (incomplete) {
                    break;
                }
            }
            if (incomplete) {
                throw new FileNotFoundException("Panoramic pack not complete! Must have six files with the same name ending in 0-5!");
            }

            this.resources[0] = new ResourceLocation(Almura.ID, PATH_ASSETS_ALMURA.relativize(path).toString());
            for (int i = 1; i < 6; i++) {
                this.resources[i] = new ResourceLocation(this.resources[0].getResourceDomain(), this.resources[0].getResourcePath().replace("0.png", i + ".png"));
            }
        }

        public PanoramicBundle(ResourceLocation baseLocation) {
            this.resources[0] = baseLocation;
            for (int i = 1; i < 6; i++) {
                this.resources[i] = new ResourceLocation(this.resources[0].getResourceDomain(), this.resources[0].getResourcePath().replace("0.png", i + ".png"));
            }
        }

        public final ResourceLocation[] getResourceLocations() {
            return this.resources;
        }
    }
}
