/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.client.model;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An accessory model. Accessory textures use the 64x32 layout of the 1.7.10 accessories (most are drawn at twice that size, which maps
 * the same).
 */
@SideOnly(Side.CLIENT)
public abstract class AccessoryModel extends ModelBase {

    protected AccessoryModel() {
        this.textureWidth = 64;
        this.textureHeight = 32;
    }

    protected ModelRenderer box(final int u, final int v, final float x, final float y, final float z, final int width, final int height,
            final int depth) {
        final ModelRenderer part = new ModelRenderer(this, u, v);
        part.addBox(x, y, z, width, height, depth);
        return part;
    }

    /**
     * Renders the accessory on the player model, whose parts are posed for this frame.
     */
    public abstract void render(ModelPlayer model, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float ageInTicks,
            float scale);
}
