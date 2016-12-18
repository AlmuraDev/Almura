/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen;

import com.almuradev.almura.client.gui.GuiConstants;
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
        this.renderer.setDefaultTexture(GuiConstants.TEXTURE_SPRITESHEET);
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

    protected static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase entityLivingBase)
    {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float renderYawOffset = entityLivingBase.renderYawOffset;
        float rotationYaw = entityLivingBase.rotationYaw;
        float rotationPitch = entityLivingBase.rotationPitch;
        float prevRotationYawHead = entityLivingBase.prevRotationYawHead;
        float rotationYawHead = entityLivingBase.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float)Math.atan((double)(mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        entityLivingBase.renderYawOffset = (float)Math.atan((double)(mouseX / 40.0F)) * 20.0F;
        entityLivingBase.rotationYaw = (float)Math.atan((double)(mouseX / 40.0F)) * 40.0F;
        entityLivingBase.rotationPitch = -((float)Math.atan((double)(mouseY / 40.0F))) * 20.0F;
        entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw;
        entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.doRenderEntity(entityLivingBase, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        entityLivingBase.renderYawOffset = renderYawOffset;
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
    }
}

