/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.client;

import com.almuradev.almura.feature.accessory.AccessoryType;
import com.almuradev.almura.feature.accessory.client.model.AccessoryModel;
import com.almuradev.almura.feature.accessory.client.model.BraceletModel;
import com.almuradev.almura.feature.accessory.client.model.EarsModel;
import com.almuradev.almura.feature.accessory.client.model.HeadAccessoryModel;
import com.almuradev.almura.feature.accessory.client.model.TailModel;
import com.almuradev.almura.feature.accessory.client.model.WingsModel;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;
import java.util.Map;

/**
 * Draws the accessories a player wears, after the player model, on every player the client renders (the local player in third person,
 * in inventory screens, and everyone else).
 */
@SideOnly(Side.CLIENT)
public final class AccessoryLayer implements LayerRenderer<AbstractClientPlayer> {

    private static final Map<AccessoryType, AccessoryModel> MODELS = new EnumMap<>(AccessoryType.class);

    static {
        MODELS.put(AccessoryType.BRACELET, new BraceletModel());
        MODELS.put(AccessoryType.CAT_HAT, HeadAccessoryModel.catHat());
        MODELS.put(AccessoryType.EARS, new EarsModel());
        MODELS.put(AccessoryType.HALO, HeadAccessoryModel.halo());
        MODELS.put(AccessoryType.NOTCH_HAT, HeadAccessoryModel.notchHat());
        MODELS.put(AccessoryType.SUNGLASSES, HeadAccessoryModel.sunglasses());
        MODELS.put(AccessoryType.TAIL, new TailModel());
        MODELS.put(AccessoryType.TOP_HAT, HeadAccessoryModel.topHat());
        MODELS.put(AccessoryType.WINGS, new WingsModel());
    }

    private final RenderPlayer renderer;
    private final ClientAccessoryManager manager;

    AccessoryLayer(final RenderPlayer renderer, final ClientAccessoryManager manager) {
        this.renderer = renderer;
        this.manager = manager;
    }

    @Override
    public void doRenderLayer(final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount, final float partialTicks,
            final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
        if (player.isInvisible()) {
            return;
        }
        final Map<AccessoryType, String> worn = this.manager.getWorn(player);
        if (worn.isEmpty()) {
            return;
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        for (final Map.Entry<AccessoryType, String> entry : worn.entrySet()) {
            final AccessoryModel model = MODELS.get(entry.getKey());
            if (model == null) {
                continue;
            }
            this.renderer.bindTexture(entry.getKey().texture(entry.getValue()));
            GlStateManager.pushMatrix();
            if (player.isSneaking()) {
                // The player model is drawn lowered while sneaking, layers have to follow it themselves
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            model.render(this.renderer.getMainModel(), player, limbSwing, limbSwingAmount, ageInTicks, scale);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
