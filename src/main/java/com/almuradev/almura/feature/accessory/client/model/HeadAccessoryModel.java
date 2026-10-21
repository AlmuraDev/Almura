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

    private HeadAccessoryModel add(final int u, final int v, final float x, final float y, final float z, final int width, final int height,
            final int depth) {
        this.parts.add(this.box(u, v, x, y, z, width, height, depth));
        return this;
    }

    @Override
    public void render(final ModelPlayer model, final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount,
            final float ageInTicks, final float scale) {
        model.bipedHead.postRender(scale);
        for (final ModelRenderer part : this.parts) {
            part.render(scale);
        }
    }

    public static HeadAccessoryModel topHat() {
        return new HeadAccessoryModel()
                .add(0, 0, -5.5F, -9F, -5.5F, 11, 2, 11)
                .add(0, 13, -3.5F, -17F, -3.5F, 7, 8, 7);
    }

    public static HeadAccessoryModel notchHat() {
        return new HeadAccessoryModel()
                .add(0, 0, -5F, -9F, -5F, 10, 1, 10)
                .add(0, 11, -4F, -13F, -4F, 8, 4, 8);
    }

    public static HeadAccessoryModel catHat() {
        return new HeadAccessoryModel()
                .add(0, 0, -5F, -10F, -5F, 10, 2, 10)
                .add(0, 12, -4F, -20F, -4F, 8, 10, 8);
    }

    public static HeadAccessoryModel halo() {
        return new HeadAccessoryModel()
                .add(0, 0, -3F, -14F, -2F, 1, 1, 4)
                .add(0, 0, -2F, -14F, -3F, 4, 1, 1)
                .add(0, 0, 2F, -14F, -2F, 1, 1, 4)
                .add(0, 0, -2F, -14F, 2F, 4, 1, 1);
    }

    public static HeadAccessoryModel sunglasses() {
        return new HeadAccessoryModel()
                .add(0, 0, -3F, -4F, -5F, 2, 2, 1)
                .add(6, 0, 1F, -4F, -5F, 2, 2, 1)
                .add(0, 4, -1F, -4F, -5F, 2, 1, 1)
                .add(0, 6, 4F, -4F, -4F, 1, 1, 4)
                .add(6, 7, 3F, -4F, -5F, 2, 1, 1)
                .add(12, 0, -5F, -4F, -4F, 1, 1, 4)
                .add(6, 5, -5F, -4F, -5F, 2, 1, 1);
    }
}
