/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.client.model;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Boxes that move with the head (hats, halo, sunglasses), in head space: the neck is the origin, the head spans y -8..0.
 */
@SideOnly(Side.CLIENT)
public final class HeadAccessoryModel extends AccessoryModel {

    private final List<ModelRenderer> parts = new ArrayList<>();
    // Below 1 the accessory is drawn see-through (sunglasses)
    private float alpha = 1.0F;

    private HeadAccessoryModel add(final int u, final int v, final float x, final float y, final float z, final int width, final int height,
            final int depth) {
        this.parts.add(this.box(u, v, x, y, z, width, height, depth));
        return this;
    }

    @Override
    public void render(final ModelPlayer model, final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount,
            final float ageInTicks, final float scale) {
        model.bipedHead.postRender(scale);
        if (this.alpha < 1.0F) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0F, 1.0F, 1.0F, this.alpha);
        }
        for (final ModelRenderer part : this.parts) {
            part.render(scale);
        }
        if (this.alpha < 1.0F) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
        }
    }

    /**
     * Where hat brims sit: their top just above the skin's hat layer (which reaches y -8.5), so the head and hair never show through the
     * brim, as low as that allows.
     */
    private static final float BRIM_TOP = -8.6F;

    public static HeadAccessoryModel topHat() {
        return new HeadAccessoryModel()
                .add(0, 0, -5.5F, BRIM_TOP, -5.5F, 11, 2, 11)
                .add(0, 13, -3.5F, BRIM_TOP - 8F, -3.5F, 7, 8, 7);
    }

    public static HeadAccessoryModel notchHat() {
        return new HeadAccessoryModel()
                .add(0, 0, -5F, BRIM_TOP, -5F, 10, 1, 10)
                .add(0, 11, -4F, BRIM_TOP - 4F, -4F, 8, 4, 8);
    }

    public static HeadAccessoryModel catHat() {
        return new HeadAccessoryModel()
                .add(0, 0, -5F, BRIM_TOP, -5F, 10, 2, 10)
                .add(0, 12, -4F, BRIM_TOP - 10F, -4F, 8, 10, 8);
    }

    /**
     * A round ring (radius 5, wider than the 8 wide head) floating 1.5 above the head: 16 short segments, each turned a step further
     * around the centre.
     */
    public static HeadAccessoryModel halo() {
        final HeadAccessoryModel model = new HeadAccessoryModel();
        final int segments = 16;
        for (int i = 0; i < segments; i++) {
            final ModelRenderer segment = new ModelRenderer(model, 0, 0);
            segment.addBox(-1F, -0.5F, -5.5F, 2, 1, 1);
            segment.setRotationPoint(0F, -10F, 0F);
            segment.rotateAngleY = (float) (i * 2 * Math.PI / segments);
            model.parts.add(segment);
        }
        return model;
    }

    /**
     * The sunglasses textures are painted like a skin's head layer (lenses across the eye row of the face, arms along the sides), so
     * they are drawn as a shell around the head, just outside the skin's hat layer, raised a pixel to sit at eye level, and slightly
     * see-through.
     */
    public static HeadAccessoryModel sunglasses() {
        final HeadAccessoryModel model = new HeadAccessoryModel();
        model.alpha = 0.8F;
        final ModelRenderer shell = new ModelRenderer(model, 0, 0);
        shell.addBox(-4F, -9F, -4F, 8, 8, 8, 0.6F);
        model.parts.add(shell);
        return model;
    }
}
