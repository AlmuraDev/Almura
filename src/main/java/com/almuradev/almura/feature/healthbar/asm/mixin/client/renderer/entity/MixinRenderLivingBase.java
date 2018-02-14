package com.almuradev.almura.feature.healthbar.asm.mixin.client.renderer.entity;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;


@SideOnly(Side.CLIENT)
@Mixin(value = RenderLivingBase.class, priority = 999)
public abstract class MixinRenderLivingBase extends Render<EntityLivingBase> {

    @Shadow protected ModelBase mainModel;

    public MixinRenderLivingBase(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn);
        this.mainModel = modelBaseIn;
        this.shadowSize = shadowSizeIn;
    }

    @Override
    public void renderLivingLabel(EntityLivingBase entityIn, String name, double x, double y, double z, int maxDistance) {
        double distance = entityIn.getDistanceSq(this.renderManager.renderViewEntity);
        double distanceLimit = (maxDistance * maxDistance); // Default is 4096

        if (entityIn instanceof EntityPlayerMP) {
            distanceLimit = 1024;
        } else {
            distanceLimit = 512;
        }

        if (distance <= distanceLimit) {  //default 4096
            boolean isSneaking = entityIn.isSneaking();
            float viewerYaw = this.renderManager.playerViewY;
            float viewerPitch = this.renderManager.playerViewX;
            boolean isThirdPersonFrontal = this.renderManager.options.thirdPersonView == 2;
            float sneakAdjustment = entityIn.height + 0.5F - (isSneaking ? 0.25F : 0.0F);
            int verticalShift = -10;

            drawCombo(entityIn, this.getFontRendererFromRenderManager(), name, (float)x,(float)y + sneakAdjustment, (float)z, verticalShift, viewerYaw, viewerPitch, isThirdPersonFrontal, isSneaking);
        }
    }

    /**
     * @author Dockter
     * @reason To have control over rendering names
     */

    @Overwrite // Was override but synthetic bridge conflict occurred.
    public void renderName(EntityLivingBase entity, double x, double y, double z) {
        if (this.showName()) {
            this.renderLivingLabel(entity, entity.getDisplayName().getFormattedText(), x, y, z, 64);
        }
    }

    private boolean showName() {
        // ToDo:  Timer code goes here.
        // ToDo:  Lots of other things I haven't though of too.
        return true;
    }

    public void drawCombo(EntityLivingBase entityIn, FontRenderer fontRendererIn, String str, float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch, boolean isThirdPersonFrontal, boolean isSneaking)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float)(isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        if (!isSneaking) {
            GlStateManager.disableDepth();
        }
        int i = fontRendererIn.getStringWidth(str) / 2;

        double a = entityIn.getMaxHealth() / entityIn.getHealth();
        double b = 100 / a;
        double c = (double) (i*2) / 100;
        double size = c * b;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        // Draw dark box for Nameplate.
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((double)(-i - 1), (double)(-1 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos((double)(-i - 1), (double)(8 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos((double)(i + 1), (double)(8 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos((double)(i + 1), (double)(-1 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();

        // Re-position for healthbar.
        int healthbarOffset = verticalShift + 10;

        if (entityIn.getHealth() > 0) {
            // Draw healthbar
            bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos((double) (-i - 1), (double) (-1 + healthbarOffset), 0.0D).color(1.0F, 0.0F, 0.0F, 0.45F).endVertex();
            bufferbuilder.pos((double) (-i - 1), (double) (3 + healthbarOffset), 0.0D).color(1.0F, 0.0F, 0.0F, 0.45F).endVertex();
            bufferbuilder.pos((double) ((-i + 1) + size), (double) (3 + healthbarOffset), 0.0D).color(1.0F, 0.0F, 0.0F, 0.45F).endVertex();
            bufferbuilder.pos((double) ((-i + 1) + size), (double) (-1 + healthbarOffset), 0.0D).color(1.0F, 0.0F, 0.0F, 0.45F).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
        }

        if (!isSneaking) {
            fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, 553648127);
            GlStateManager.enableDepth();
        }

        GlStateManager.depthMask(true);
        fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, isSneaking ? 553648127 : -1);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
