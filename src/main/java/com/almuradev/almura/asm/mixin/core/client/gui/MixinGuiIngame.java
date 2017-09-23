/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.client.gui;

import com.almuradev.almura.feature.hud.ClientHeadUpDisplay;
import com.almuradev.almura.feature.hud.screen.AbstractHUD;
import com.google.common.collect.Ordering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collection;

import javax.inject.Inject;

@Mixin(GuiIngame.class)
public abstract class MixinGuiIngame extends Gui {

    @Inject private static ClientHeadUpDisplay hud;

    /**
     * @author Steven Downer (Grinch)
     */
    @Overwrite
    protected void renderPotionEffects(ScaledResolution resolution)
    {
        Minecraft mc = Minecraft.getMinecraft();
        Collection<PotionEffect> collection = mc.player.getActivePotionEffects();

        if (!collection.isEmpty())
        {
            mc.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
            GlStateManager.enableBlend();
            int i = 0;
            int j = 0;

            for (PotionEffect potioneffect : Ordering.natural().reverse().sortedCopy(collection))
            {
                Potion potion = potioneffect.getPotion();

                if (!potion.shouldRenderHUD(potioneffect)) continue;
                // Rebind in case previous renderHUDEffect changed texture
                mc.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
                if (potioneffect.doesShowParticles())
                {
                    int k = resolution.getScaledWidth();
                    // Almura start
                    int l = hud.getHUD().map(AbstractHUD::getPotionOffsetY).orElse(1);
                    // Almura end

                    if (mc.isDemo())
                    {
                        l += 15;
                    }

                    int i1 = potion.getStatusIconIndex();

                    if (potion.isBeneficial())
                    {
                        ++i;
                        k = k - 25 * i;
                    }
                    else
                    {
                        ++j;
                        k = k - 25 * j;
                        l += 26;
                    }

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    float f = 1.0F;

                    if (potioneffect.getIsAmbient())
                    {
                        this.drawTexturedModalRect(k, l, 165, 166, 24, 24);
                    }
                    else
                    {
                        this.drawTexturedModalRect(k, l, 141, 166, 24, 24);

                        if (potioneffect.getDuration() <= 200)
                        {
                            int j1 = 10 - potioneffect.getDuration() / 20;
                            f = MathHelper.clamp((float)potioneffect.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float)potioneffect.getDuration() * (float)Math.PI / 5.0F) * MathHelper.clamp((float)j1 / 10.0F * 0.25F, 0.0F, 0.25F);
                        }
                    }

                    GlStateManager.color(1.0F, 1.0F, 1.0F, f);

                    // FORGE - Move status icon check down from above so renderHUDEffect will still be called without a status icon
                    if (potion.hasStatusIcon())
                    this.drawTexturedModalRect(k + 3, l + 3, i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                    potion.renderHUDEffect(k, l, potioneffect, mc, f);
                }
            }
        }
    }
}
