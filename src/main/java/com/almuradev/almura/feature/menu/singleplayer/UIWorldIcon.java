/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.singleplayer;

import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * A world's icon (its icon.png, or vanilla's unknown icon), drawn from a texture loaded outside the Malisis sprite sheet.
 */
@SideOnly(Side.CLIENT)
final class UIWorldIcon extends BasicContainer<UIWorldIcon> {

    private final ResourceLocation texture;

    UIWorldIcon(final MalisisGui gui, final ResourceLocation texture, final int size) {
        super(gui, size, size);
        this.texture = texture;
        this.setBackgroundAlpha(0);
    }

    @Override
    public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        // Drawn outside of the renderer's batch, leaving blending as it found it
        Tessellator.getInstance().draw();
        final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);

        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(this.screenX(), this.screenY(), 0.0F, 0.0F, this.getWidth(), this.getHeight(), this.getWidth(),
            this.getHeight());
        if (!blend) {
            GlStateManager.disableBlend();
        }

        renderer.bindDefaultTexture();
        renderer.startDrawing();
    }
}
