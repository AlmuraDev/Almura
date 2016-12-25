/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen;

import com.almuradev.almura.Constants;
import com.almuradev.almura.client.gui.component.entity.RenderEntityAngle;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.Optional;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public abstract class SimpleScreen extends MalisisGui {

    protected final Optional<SimpleScreen> parent;

    /**
     * Creates a gui with an absent parent
     */
    public SimpleScreen() {
        this(null);
    }

    /**
     * Creates a gui with a parent
     *
     * @param parent the {@link SimpleScreen} that we came from
     */
    public SimpleScreen(@Nullable SimpleScreen parent) {
        this.parent = Optional.ofNullable(parent);
        this.renderer.setDefaultTexture(Constants.Gui.TEXTURE_SPRITESHEET);
    }

    /**
     * Gets the X position after applying padding against another component, uses {@link Anchor#LEFT} by default
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @return The padded X position
     */
    public static int getPaddedX(UIComponent<?> component, int padding) {
        return getPaddedX(component, padding, Anchor.LEFT);
    }

    /**
     * Gets the X position after applying padding against another component
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @param anchor The direction we're going
     * @return The padded X position
     */
    public static int getPaddedX(UIComponent<?> component, int padding, int anchor) {
        if (anchor == Anchor.LEFT) {
            return component.getX() + component.getWidth() + padding;
        } else if (anchor == Anchor.RIGHT) {
            return component.getX() - component.getWidth() - padding;
        } else {
            throw new IllegalArgumentException("Invalid anchor used [" + anchor + "], anchor must be LEFT or RIGHT.");
        }
    }

    /**
     * Gets the Y position after applying padding against another component, uses {@link Anchor#TOP} by default
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @return The padded Y position
     */
    public static int getPaddedY(UIComponent<?> component, int padding) {
        return getPaddedY(component, padding, Anchor.TOP);
    }

    /**
     * Gets the Y position after applying padding against another component
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @param anchor The direction we're going
     * @return The padded Y position
     */
    public static int getPaddedY(UIComponent<?> component, int padding, int anchor) {
        if (anchor == Anchor.BOTTOM) {
            return component.getY() - component.getHeight() - padding;
        } else if (anchor == Anchor.TOP) {
            return component.getY() + component.getHeight() + padding;
        } else {
            throw new IllegalArgumentException("Invalid anchor used [" + anchor + "], anchor must be BOTTOM or TOP.");
        }
    }

    public static void drawEntityOnScreen(int x, int y, float scale, RenderEntityAngle renderEntityAngle, EntityLivingBase entityLivingBase) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 50.0F);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float yawOffset = entityLivingBase.renderYawOffset;
        float rotationYaw = entityLivingBase.rotationYaw;
        float rotationPitch = entityLivingBase.rotationPitch;
        float prevRotationYawHead = entityLivingBase.prevRotationYawHead;
        float rotationYawHead = entityLivingBase.rotationYawHead;
        GlStateManager.rotate(renderEntityAngle.pitch, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(renderEntityAngle.yaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(renderEntityAngle.roll, 0.0f, 0.0f, 1.0f);
        RenderHelper.enableStandardItemLighting();
        entityLivingBase.renderYawOffset = (float)Math.atan(0D) * 20.0F;
        entityLivingBase.rotationYaw = (float)Math.atan(0D) * 40.0F;
        entityLivingBase.rotationPitch = -((float)Math.atan(0D)) * 20.0F;
        entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw;
        entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        renderManager.setPlayerViewY(180.0F);
        renderManager.setRenderShadow(false);
        renderManager.doRenderEntity(entityLivingBase, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        renderManager.setRenderShadow(true);
        entityLivingBase.renderYawOffset = yawOffset;
        entityLivingBase.rotationYaw = rotationYaw;
        entityLivingBase.rotationPitch = rotationPitch;
        entityLivingBase.prevRotationYawHead = prevRotationYawHead;
        entityLivingBase.rotationYawHead = rotationYawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        RenderHelper.enableGUIStandardItemLighting();
    }

    /**
     * Closes this {@link SimpleScreen} and displays the parent, if present.
     */
    @Override
    public final void close() {
        Keyboard.enableRepeatEvents(false);
        if (this.mc.player != null) {
            this.mc.player.closeScreen();
        }

        this.onClose();

        this.mc.displayGuiScreen(this.parent.isPresent() ? this.parent.get() : null);
        if (!this.parent.isPresent()) {
            this.mc.setIngameFocus();
        }
    }

    protected void onClose() {
    }
}

