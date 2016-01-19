/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.client.renderer.entity;

import com.almuradev.almura.Configuration;
import com.almuradev.almura.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity extends Render {

    @Shadow
    private static float NAME_TAG_RANGE;

    @Shadow
    private static float NAME_TAG_RANGE_SNEAK;

    @Overwrite
    protected void passSpecialRender(EntityLivingBase entity, double p_77033_2_, double p_77033_4_, double p_77033_6_) {

        if (!(entity instanceof EntityPlayer || entity instanceof EntityAnimal)) {
            return;
        }

        if (entity instanceof EntityAnimal) {
            if (!Configuration.DISPLAY_ANIMAL_HEAT) {
                return;
            }
        }

        if (MinecraftForge.EVENT_BUS
                .post(new RenderLivingEvent.Specials.Pre(entity, (RendererLivingEntity) (Object) this, p_77033_2_, p_77033_4_, p_77033_6_))) {
            return;
        }
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

        if (Minecraft.isGuiEnabled() && !entity.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) && entity.riddenByEntity == null) {
            String entityName = entity.getFormattedCommandSenderName().getFormattedText();
            float f = 1.6F;
            float f1 = 0.016666668F * f;
            double d3 = entity.getDistanceSqToEntity(this.renderManager.livingPlayer);
            float f2 = entity.isSneaking() ? NAME_TAG_RANGE_SNEAK : NAME_TAG_RANGE;
            double f6 = (double) (f2 * f2);

            if (entity instanceof EntityAnimal) {
                f6 = (double) (f2);
                EntityAnimal animal = (EntityAnimal) entity;
                if (animal.getGrowingAge() == 0 && !animal.isInLove()) { // Animal is in heat.
                    entityName = Colors.DARK_AQUA + entityName;
                }
            }

            if (d3 < f6) {
                if (entity.isSneaking()) {
                    FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
                    GL11.glPushMatrix();
                    GL11.glTranslatef((float) p_77033_2_ + 0.0F, (float) p_77033_4_ + entity.height + 0.5F, (float) p_77033_6_);
                    GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                    GL11.glScalef(-f1, -f1, f1);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glTranslatef(0.0F, 0.25F / f1, 0.0F);
                    GL11.glDepthMask(false);
                    GL11.glEnable(GL11.GL_BLEND);
                    OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                    Tessellator tessellator = Tessellator.instance;
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    tessellator.startDrawingQuads();
                    int i = fontrenderer.getStringWidth(entityName) / 2;
                    tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
                    tessellator.addVertex((double) (-i - 1), -1.0D, 0.0D);
                    tessellator.addVertex((double) (-i - 1), 8.0D, 0.0D);
                    tessellator.addVertex((double) (i + 1), 8.0D, 0.0D);
                    tessellator.addVertex((double) (i + 1), -1.0D, 0.0D);
                    tessellator.draw();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    GL11.glDepthMask(true);
                    fontrenderer.drawString(entityName, -fontrenderer.getStringWidth(entityName) / 2, 0, 553648127);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glPopMatrix();
                } else {
                    ((RendererLivingEntity) (Object) this).renderOffsetLivingLabel(entity, p_77033_2_, p_77033_4_, p_77033_6_, entityName, f1, d3);
                }
            }
        }
        MinecraftForge.EVENT_BUS
                .post(new RenderLivingEvent.Specials.Post(entity, (RendererLivingEntity) (Object) this, p_77033_2_, p_77033_4_, p_77033_6_));
    }
}
