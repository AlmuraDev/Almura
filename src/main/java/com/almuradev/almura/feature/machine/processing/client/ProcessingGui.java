/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.machine.processing.ProcessingContainer;
import com.almuradev.almura.feature.machine.processing.ProcessingRecipe;
import com.almuradev.almura.feature.machine.processing.ProcessingTileEntity;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Power meter on the left (stored EU), what the machine will make in the middle (with arrows to pick the output when the input has more than
 * one recipe), and the progress icon between the input slot above and the output slot below.
 */
@SideOnly(Side.CLIENT)
public final class ProcessingGui extends GuiContainer {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Almura.ID, "textures/gui/machine/seed_processor.png");

    // Power meter: frame on the background, fill sprite at (208, 0)
    private static final int ENERGY_FRAME_X = 18, ENERGY_FRAME_Y = 40, ENERGY_FRAME_WIDTH = 32, ENERGY_FRAME_HEIGHT = 17;
    private static final int ENERGY_X = 22, ENERGY_Y = 44, ENERGY_WIDTH = 24, ENERGY_HEIGHT = 9;
    private static final int ENERGY_U = 208, ENERGY_V = 0;
    // Progress icon: fills left to right with the sprite at (176, 0)
    private static final int PROGRESS_X = 124, PROGRESS_Y = 38, PROGRESS_WIDTH = 20, PROGRESS_HEIGHT = 19;
    private static final int PROGRESS_U = 176, PROGRESS_V = 0;
    // Output preview and selector, with Start / Stop below, in the empty middle of the background
    private static final int PREVIEW_X = 79, PREVIEW_Y = 26;
    private static final int ARROW_WIDTH = 12, ARROW_HEIGHT = 20, ARROW_Y = 24;
    private static final int PREVIOUS_X = 60, NEXT_X = 104;
    private static final int START_X = 62, START_Y = 50, START_WIDTH = 52, START_HEIGHT = 20;

    private final ProcessingContainer container;
    private final InventoryPlayer playerInventory;
    private final String title;
    private GuiButton previousButton;
    private GuiButton nextButton;
    private GuiButton startButton;

    public ProcessingGui(final ProcessingContainer container, final InventoryPlayer playerInventory, final String title) {
        super(container);
        this.container = container;
        this.playerInventory = playerInventory;
        this.title = title;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.previousButton = this.addButton(new GuiButton(ProcessingContainer.BUTTON_PREVIOUS, this.guiLeft + PREVIOUS_X, this.guiTop + ARROW_Y,
                ARROW_WIDTH, ARROW_HEIGHT, "<"));
        this.nextButton = this.addButton(new GuiButton(ProcessingContainer.BUTTON_NEXT, this.guiLeft + NEXT_X, this.guiTop + ARROW_Y,
                ARROW_WIDTH, ARROW_HEIGHT, ">"));
        this.startButton = this.addButton(new GuiButton(ProcessingContainer.BUTTON_START_STOP, this.guiLeft + START_X, this.guiTop + START_Y,
                START_WIDTH, START_HEIGHT, ""));
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (button == this.previousButton || button == this.nextButton || button == this.startButton) {
            // Vanilla's container button message: handled by ProcessingContainer#enchantItem on the server
            this.mc.playerController.sendEnchantPacket(this.container.windowId, button.id);
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        // The arrows are only needed when there's a choice; machines that wait for the player then also show Start / Stop
        final boolean choice = this.recipes().size() > 1;
        this.previousButton.visible = choice;
        this.nextButton.visible = choice;
        this.startButton.visible = choice && this.container.machine().type().startOnChoice();
        this.startButton.displayString = I18n.format(this.container.property(ProcessingContainer.PROPERTY_STARTED) != 0
                ? "gui.almura.machine.stop" : "gui.almura.machine.start");

        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        final ItemStack preview = this.preview();
        if (this.isPointInRegion(ENERGY_FRAME_X, ENERGY_FRAME_Y, ENERGY_FRAME_WIDTH, ENERGY_FRAME_HEIGHT, mouseX, mouseY)) {
            final String text = this.requiresPower()
                    ? I18n.format("gui.almura.machine.energy", this.container.property(ProcessingContainer.PROPERTY_ENERGY),
                    this.container.property(ProcessingContainer.PROPERTY_CAPACITY))
                    : I18n.format("gui.almura.machine.no_power_required");
            this.drawHoveringText(Collections.singletonList(text), mouseX, mouseY);
        } else if (this.isPointInRegion(PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            final int percent = this.container.property(ProcessingContainer.PROPERTY_PROGRESS) * 100 / ProcessingTileEntity.PROCESS_TICKS;
            this.drawHoveringText(Collections.singletonList(I18n.format("gui.almura.machine.progress", percent)), mouseX, mouseY);
        } else if (!preview.isEmpty() && this.isPointInRegion(PREVIEW_X, PREVIEW_Y, 16, 16, mouseX, mouseY)) {
            this.renderToolTip(preview, mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(this.title, (this.xSize - this.fontRenderer.getStringWidth(this.title)) / 2, 6, 0x404040);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 0x404040);

        // What the machine will make from the current input
        final ItemStack preview = this.preview();
        if (!preview.isEmpty()) {
            RenderHelper.enableGUIStandardItemLighting();
            this.itemRender.renderItemAndEffectIntoGUI(preview, PREVIEW_X, PREVIEW_Y);
            this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, preview, PREVIEW_X, PREVIEW_Y, null);
            RenderHelper.disableStandardItemLighting();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND);
        final int left = this.guiLeft;
        final int top = this.guiTop;
        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);

        // Power meter fills with the stored energy (always full when no power is required)
        final int energy = this.requiresPower() ? this.scaled(this.container.property(ProcessingContainer.PROPERTY_ENERGY),
                this.container.property(ProcessingContainer.PROPERTY_CAPACITY), ENERGY_WIDTH) : ENERGY_WIDTH;
        if (energy > 0) {
            this.drawTexturedModalRect(left + ENERGY_X, top + ENERGY_Y, ENERGY_U, ENERGY_V, energy, ENERGY_HEIGHT);
        }

        // Progress icon fills left to right
        final int progress = this.scaled(this.container.property(ProcessingContainer.PROPERTY_PROGRESS), ProcessingTileEntity.PROCESS_TICKS,
                PROGRESS_WIDTH);
        if (progress > 0) {
            this.drawTexturedModalRect(left + PROGRESS_X, top + PROGRESS_Y, PROGRESS_U, PROGRESS_V, progress, PROGRESS_HEIGHT);
        }
    }

    /**
     * The recipes for what's in the input slot (the same list, in the same order, as the server's).
     */
    private List<ProcessingRecipe> recipes() {
        final ItemStack input = this.container.machine().getInventory().getStackInSlot(ProcessingTileEntity.INPUT_SLOT);
        return this.container.machine().type().recipes().recipesFor(input);
    }

    /**
     * What the machine will make: the selected recipe's output, or nothing.
     */
    private ItemStack preview() {
        final List<ProcessingRecipe> recipes = this.recipes();
        if (recipes.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final int selected = this.container.property(ProcessingContainer.PROPERTY_SELECTED);
        return recipes.get(selected < recipes.size() ? selected : 0).output();
    }

    private boolean requiresPower() {
        return this.container.property(ProcessingContainer.PROPERTY_REQUIRES_POWER) != 0;
    }

    private int scaled(final int value, final int max, final int size) {
        return max <= 0 ? 0 : Math.min(size, value * size / max);
    }
}
