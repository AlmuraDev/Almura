/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.seedprocessor.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorContainer;
import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;

/**
 * Power meter on the left (stored EU), then the pipe to the progress icon, with the input slot above it and the output slot below.
 */
@SideOnly(Side.CLIENT)
public final class SeedProcessorGui extends GuiContainer {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Almura.ID, "textures/gui/machine/seed_processor.png");

    // Power meter: frame on the background, fill sprite at (208, 0)
    private static final int ENERGY_FRAME_X = 18, ENERGY_FRAME_Y = 40, ENERGY_FRAME_WIDTH = 32, ENERGY_FRAME_HEIGHT = 17;
    private static final int ENERGY_X = 22, ENERGY_Y = 44, ENERGY_WIDTH = 24, ENERGY_HEIGHT = 9;
    private static final int ENERGY_U = 208, ENERGY_V = 0;
    // Progress icon: fills left to right with the sprite at (176, 0)
    private static final int PROGRESS_X = 124, PROGRESS_Y = 38, PROGRESS_WIDTH = 20, PROGRESS_HEIGHT = 19;
    private static final int PROGRESS_U = 176, PROGRESS_V = 0;

    private final SeedProcessorContainer container;
    private final InventoryPlayer playerInventory;
    private final String title;

    public SeedProcessorGui(final SeedProcessorContainer container, final InventoryPlayer playerInventory, final String title) {
        super(container);
        this.container = container;
        this.playerInventory = playerInventory;
        this.title = title;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        if (this.isPointInRegion(ENERGY_FRAME_X, ENERGY_FRAME_Y, ENERGY_FRAME_WIDTH, ENERGY_FRAME_HEIGHT, mouseX, mouseY)) {
            final String text = this.requiresPower()
                    ? I18n.format("gui.almura.machine.energy", this.container.property(SeedProcessorContainer.PROPERTY_ENERGY),
                    this.container.property(SeedProcessorContainer.PROPERTY_CAPACITY))
                    : I18n.format("gui.almura.machine.no_power_required");
            this.drawHoveringText(Collections.singletonList(text), mouseX, mouseY);
        } else if (this.isPointInRegion(PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            final int percent = this.container.property(SeedProcessorContainer.PROPERTY_PROGRESS) * 100 / SeedProcessorTileEntity.PROCESS_TICKS;
            this.drawHoveringText(Collections.singletonList(I18n.format("gui.almura.machine.progress", percent)), mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(this.title, (this.xSize - this.fontRenderer.getStringWidth(this.title)) / 2, 6, 0x404040);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND);
        final int left = this.guiLeft;
        final int top = this.guiTop;
        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);

        // Power meter fills with the stored energy (always full when no power is required)
        final int energy = this.requiresPower() ? this.scaled(this.container.property(SeedProcessorContainer.PROPERTY_ENERGY),
                this.container.property(SeedProcessorContainer.PROPERTY_CAPACITY), ENERGY_WIDTH) : ENERGY_WIDTH;
        if (energy > 0) {
            this.drawTexturedModalRect(left + ENERGY_X, top + ENERGY_Y, ENERGY_U, ENERGY_V, energy, ENERGY_HEIGHT);
        }

        // Progress icon fills left to right
        final int progress = this.scaled(this.container.property(SeedProcessorContainer.PROPERTY_PROGRESS), SeedProcessorTileEntity.PROCESS_TICKS,
                PROGRESS_WIDTH);
        if (progress > 0) {
            this.drawTexturedModalRect(left + PROGRESS_X, top + PROGRESS_Y, PROGRESS_U, PROGRESS_V, progress, PROGRESS_HEIGHT);
        }
    }

    private boolean requiresPower() {
        return this.container.property(SeedProcessorContainer.PROPERTY_REQUIRES_POWER) != 0;
    }

    private int scaled(final int value, final int max, final int size) {
        return max <= 0 ? 0 : Math.min(size, value * size / max);
    }
}
