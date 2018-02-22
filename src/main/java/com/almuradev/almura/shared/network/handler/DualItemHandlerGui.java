/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.network.handler;

import com.almuradev.almura.shared.inventory.DualItemHandlerContainer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

@SideOnly(Side.CLIENT)
public final class DualItemHandlerGui extends GuiContainer {

    private final ITextComponent upperTitle, lowerTitle;

    private final int slotCount;
    private final int inventoryRows;

    public DualItemHandlerGui(IItemHandler upperItemHandler, IItemHandler lowerItemHandler, ITextComponent upperTitle, ITextComponent lowerTitle) {
        super(new DualItemHandlerContainer(upperItemHandler, lowerItemHandler));
        this.allowUserInput = false;

        this.upperTitle = upperTitle;
        this.lowerTitle = lowerTitle;

        this.slotCount = upperItemHandler.getSlots();
        this.inventoryRows = this.slotCount / 9;
        this.ySize = 114 + this.inventoryRows * 18;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Copy-paste from GuiChest
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Copy-paste from GuiChest but modified to use titles passed in
        this.fontRenderer.drawString("§f" + this.upperTitle.getUnformattedText(), 8, 6, 4210752);
        this.fontRenderer.drawString(this.lowerTitle.getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // Copy-paste from GuiChest
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GuiChest.CHEST_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.drawTexturedModalRect(i, j + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }
}
