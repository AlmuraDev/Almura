package com.almuradev.almura.client.gui.component.entity;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.util.MathUtil;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLivingBase;

public class UIEntity extends UIComponent {

    private final EntityLivingBase entityLivingBase;
    private final RenderLiving renderLiving;
    private RenderEntityAngle renderEntityAngle;
    private float modelWidth, modelHeight, modelDepth;
    private int width, height;

    /**
     Model X: Width  (left   ->  right)
     Model Y: Height (bottom ->  top)
     Model Z: Depth  (front  ->  back)
     */
    public UIEntity(MalisisGui gui, int width, int height, EntityLivingBase entityLivingBase, RenderEntityAngle renderEntityAngle) {
        super(gui);
        this.entityLivingBase = entityLivingBase;
        this.renderLiving = (RenderLiving) Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(this.entityLivingBase);
        this.width = width;
        this.height = height;
        this.renderEntityAngle = renderEntityAngle;

        float lowestX = 0f;
        float highestX = 0f;
        float lowestY = 0f;
        float highestY = 0f;
        float lowestZ = 0f;
        float highestZ = 0f;

        for (ModelRenderer renderer : renderLiving.getMainModel().boxList) {
            for (ModelBox cube : renderer.cubeList) {
                // X
                lowestX = Math.min(lowestX, cube.posX1);
                lowestX = Math.min(lowestX, cube.posX2);
                highestX = Math.max(highestX, cube.posX1);
                highestX = Math.max(highestX, cube.posX2);
                // Y
                lowestY = Math.min(lowestY, cube.posY1);
                lowestY = Math.min(lowestY, cube.posY2);
                highestY = Math.max(highestY, cube.posY1);
                highestY = Math.max(highestY, cube.posY2);
                // Z
                lowestZ = Math.min(lowestZ, cube.posZ1);
                lowestZ = Math.min(lowestZ, cube.posZ2);
                highestZ = Math.max(highestZ, cube.posZ1);
                highestZ = Math.max(highestZ, cube.posZ2);
            }
        }

        this.modelWidth = highestX - lowestX;
        this.modelHeight = highestY - lowestY;
        this.modelDepth = highestZ - lowestZ;
        Almura.instance.logger.debug("X: [Lowest: {}, Highest: {}, Width: {}]", lowestX, highestX, this.modelWidth);
        Almura.instance.logger.debug("Y: [Lowest: {}, Highest: {}, Height: {}]", lowestY, highestY, this.modelHeight);
        Almura.instance.logger.debug("Z: [Lowest: {}, Highest: {}, Depth: {}]", lowestZ, highestZ, this.modelDepth);
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        final ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        final float relativeScale = Math.max((2.0f / resolution.getScaledWidth()) * modelWidth, (2.0f / resolution.getScaledHeight() * modelHeight));
        final float scaleFactor = Math.max(width * relativeScale, height * relativeScale);
        final float scale = Math.max(modelWidth, modelHeight) / scaleFactor;
        SimpleScreen.drawEntityOnScreen((int) (screenX() + scale), (int) (screenY() + scale + 10), scale, renderEntityAngle, this.entityLivingBase);
    }
}
