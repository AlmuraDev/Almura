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

/**
 * A ring around the left wrist, in arm space (the shoulder is the origin, the arm reaches down to y 10). Slim (3 wide) arms get a narrower
 * ring.
 */
@SideOnly(Side.CLIENT)
public final class BraceletModel extends AccessoryModel {

    private final ModelRenderer[] wide = {
            this.box(0, 0, -1F, 7F, -3F, 4, 1, 1),
            this.box(10, 0, -2F, 7F, -3F, 1, 1, 6),
            this.box(0, 2, -1F, 7F, 2F, 4, 1, 1),
            this.box(0, 4, 3F, 7F, -3F, 1, 1, 6)
    };
    private final ModelRenderer[] slim = {
            this.box(0, 0, -1F, 7F, -3F, 3, 1, 1),
            this.box(10, 0, -2F, 7F, -3F, 1, 1, 6),
            this.box(0, 2, -1F, 7F, 2F, 3, 1, 1),
            this.box(0, 4, 2F, 7F, -3F, 1, 1, 6)
    };

    @Override
    public void render(final ModelPlayer model, final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount,
            final float ageInTicks, final float scale) {
        model.bipedLeftArm.postRender(scale);
        for (final ModelRenderer part : "slim".equals(player.getSkinType()) ? this.slim : this.wide) {
            part.render(scale);
        }
    }
}
