/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.harvestsack.client;

import com.almuradev.almura.feature.harvestsack.ContainerHarvestSack;
import com.almuradev.almura.feature.harvestsack.SackInventory;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Chest style screen for a harvesting sack.
 */
@SideOnly(Side.CLIENT)
public final class GuiHarvestSack extends GuiContainer {

    private static final ResourceLocation CHEST_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int ROWS = SackInventory.SIZE / 9;

    private final InventoryPlayer playerInventory;
    private final String title;

    public GuiHarvestSack(final ContainerHarvestSack container, final InventoryPlayer playerInventory) {
        super(container);
        this.playerInventory = playerInventory;
        // The sack's name (a renamed sack shows its own name)
        this.title = container.getSack().getDisplayName();
        this.allowUserInput = false;
        this.ySize = 114 + ROWS * 18;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(this.title, 8, 6, 0x404040);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CHEST_TEXTURE);
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, ROWS * 18 + 17);
        this.drawTexturedModalRect(x, y + ROWS * 18 + 17, 0, 126, this.xSize, 96);
    }
}
