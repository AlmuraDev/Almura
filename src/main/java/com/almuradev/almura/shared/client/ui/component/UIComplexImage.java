/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class UIComplexImage extends UIImage {

    private ItemStack itemStack;

    public UIComplexImage(MalisisGui gui, ItemStack itemStack) {
        super(gui, itemStack);
        this.itemStack = itemStack;
    }


    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (itemStack != null) {
            //float originalZLevel = Minecraft.getMinecraft().getRenderItem().zLevel;  // Save existing value
            //Minecraft.getMinecraft().getRenderItem().zLevel = this.getZIndex();  // Set the value to the current zIndex of the current component.

            if (Minecraft.getMinecraft().getRenderItem().zLevel == 0F) {  // Detect if the value is still zero, if so subtract 100F because RenderItem adds 100F to the value.
            	//Minecraft.getMinecraft().getRenderItem().zLevel = -100F;  // Set this to -100F because I said so, trust me, I'm a professional.
            } else {
                //Minecraft.getMinecraft().getRenderItem().zLevel = this.getZIndex();  // This is fine if the set value was zero.
            }
            //Minecraft.getMinecraft().getRenderItem().zLevel = originalZLevel;

            // Below disable/Enable GL Depth Test is to fix the itemStack render issue with multiple windows using zLevel.
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            renderer.drawItemStack(itemStack);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            
            //Minecraft.getMinecraft().getRenderItem().zLevel = originalZLevel; // Revert Setting to prevent the rest of the order from getting messed up.
        }
    }
}
