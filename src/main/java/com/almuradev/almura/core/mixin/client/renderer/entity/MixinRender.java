/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.client.renderer.entity;

import com.almuradev.almura.client.DisplayNameManager;
import com.google.common.base.Optional;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Render.class)
public abstract class MixinRender {

    @Shadow protected RenderManager renderManager;

    @Shadow
    abstract FontRenderer getFontRendererFromRenderManager();

    @Overwrite
    protected void renderLivingLabel(Entity p_147906_1_, String p_147906_2_, double p_147906_3_, double p_147906_5_, double p_147906_7_,
            int p_147906_9_) {
        double d3 = p_147906_1_.getDistanceSqToEntity(this.renderManager.livingPlayer);

        if (d3 <= (double) (p_147906_9_ * p_147906_9_)) {
            if (p_147906_1_ instanceof EntityPlayer) {
                // Left: Display Name, Right: Title
                final DisplayNameManager.Tuple<Optional<String>, Optional<String>> tuple = DisplayNameManager.getDisplayNameAndTitle(
                        p_147906_1_.getCommandSenderName());

                if (tuple.left.isPresent() && tuple.right.isPresent() && !tuple.right.get().isEmpty()) {
                    drawNameplate(p_147906_1_, tuple.right.get(), p_147906_3_, p_147906_5_, p_147906_7_, p_147906_9_);

                    p_147906_5_ += ((float) this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * 0.025);
                }
            }
            drawNameplate(p_147906_1_, p_147906_2_, p_147906_3_, p_147906_5_, p_147906_7_, p_147906_9_);
        }
    }

    private void drawNameplate(Entity p_147906_1_, String p_147906_2_, double p_147906_3_, double p_147906_5_, double p_147906_7_, int p_147906_9_) {
        FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
        float f = 1.6F;
        float f1 = 0.016666668F * f;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) p_147906_3_ + 0.0F, (float) p_147906_5_ + p_147906_1_.height + 0.5F, (float) p_147906_7_);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-f1, -f1, f1);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.instance;
        byte b0 = 0;

        if (p_147906_2_.equals("deadmau5")) {
            b0 = -10;
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        int j = fontrenderer.getStringWidth(p_147906_2_) / 2;
        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tessellator.addVertex((double) (-j - 1), (double) (-1 + b0), 0.0D);
        tessellator.addVertex((double) (-j - 1), (double) (8 + b0), 0.0D);
        tessellator.addVertex((double) (j + 1), (double) (8 + b0), 0.0D);
        tessellator.addVertex((double) (j + 1), (double) (-1 + b0), 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        fontrenderer.drawString(p_147906_2_, -fontrenderer.getStringWidth(p_147906_2_) / 2, b0, 553648127);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        fontrenderer.drawString(p_147906_2_, -fontrenderer.getStringWidth(p_147906_2_) / 2, b0, -1);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
}
