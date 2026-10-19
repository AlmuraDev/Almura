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
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Power meter on the left (stored EU), what the machine will make in the middle (with arrows to pick the output when the input has more than
 * one recipe), and the progress icon between the input slot above and the output slot below. Machines that use water (the wash plant)
 * also have a water container slot above an emptied-container slot on the left of a water gauge, with the selector in the top left.
 */
@SideOnly(Side.CLIENT)
public final class ProcessingGui extends GuiContainer {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Almura.ID, "textures/gui/machine/seed_processor.png");
    // Machines that use water: the same, plus the water container slots and the water gauge (from the Paydirt wash plant)
    private static final ResourceLocation WATER_BACKGROUND = new ResourceLocation(Almura.ID, "textures/gui/machine/washplant.png");

    // Power meter: frame on the background, fill sprite at (208, 0)
    private static final int ENERGY_FRAME_X = 18, ENERGY_FRAME_Y = 40, ENERGY_FRAME_WIDTH = 32, ENERGY_FRAME_HEIGHT = 17;
    private static final int ENERGY_X = 22, ENERGY_Y = 44, ENERGY_WIDTH = 24, ENERGY_HEIGHT = 9;
    private static final int ENERGY_U = 208, ENERGY_V = 0;
    // Progress icon: fills left to right with the sprite at (176, 0)
    private static final int PROGRESS_X = 124, PROGRESS_Y = 38, PROGRESS_WIDTH = 20, PROGRESS_HEIGHT = 19;
    private static final int PROGRESS_U = 176, PROGRESS_V = 0;
    // Water gauge: the inside of the gauge frame, filled from the bottom with the sprite at (196, 0)
    private static final int WATER_FRAME_X = 82, WATER_FRAME_Y = 20, WATER_FRAME_WIDTH = 17, WATER_FRAME_HEIGHT = 52;
    private static final int WATER_X = 84, WATER_BOTTOM = 70, WATER_WIDTH = 12, WATER_HEIGHT = 47;
    private static final int WATER_U = 196, WATER_V = 0;
    // Output preview and selector, with Start / Stop below, in the empty middle of the background; machines that use water have the
    // gauge there, so theirs sits in the empty top left instead
    private static final int ARROW_WIDTH = 12, ARROW_HEIGHT = 20;
    private final int previewX, previewY, arrowY, previousX, nextX;
    private static final int START_X = 62, START_Y = 50, START_WIDTH = 52, START_HEIGHT = 20;
    private static final DecimalFormat CHANCE_FORMAT = new DecimalFormat("0.##");

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
        if (container.machine().type().usesWater()) {
            this.previewX = 26;
            this.previewY = 19;
            this.arrowY = 17;
            this.previousX = 10;
            this.nextX = 42;
        } else {
            this.previewX = 79;
            this.previewY = 26;
            this.arrowY = 24;
            this.previousX = 60;
            this.nextX = 104;
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.previousButton = this.addButton(new GuiButton(ProcessingContainer.BUTTON_PREVIOUS, this.guiLeft + this.previousX, this.guiTop + this.arrowY,
                ARROW_WIDTH, ARROW_HEIGHT, "<"));
        this.nextButton = this.addButton(new GuiButton(ProcessingContainer.BUTTON_NEXT, this.guiLeft + this.nextX, this.guiTop + this.arrowY,
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
        } else if (this.usesWater() && this.isPointInRegion(WATER_FRAME_X, WATER_FRAME_Y, WATER_FRAME_WIDTH, WATER_FRAME_HEIGHT, mouseX, mouseY)) {
            this.drawHoveringText(Collections.singletonList(I18n.format("gui.almura.machine.water",
                    this.container.property(ProcessingContainer.PROPERTY_WATER), this.container.property(ProcessingContainer.PROPERTY_WATER_CAPACITY),
                    this.container.machine().type().waterPerOperation())), mouseX, mouseY);
        } else if (this.isPointInRegion(PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            final int percent = this.container.property(ProcessingContainer.PROPERTY_PROGRESS) * 100 / ProcessingTileEntity.PROCESS_TICKS;
            this.drawHoveringText(Collections.singletonList(I18n.format("gui.almura.machine.progress", percent)), mouseX, mouseY);
        } else if (!preview.isEmpty() && this.isPointInRegion(this.previewX, this.previewY, 16, 16, mouseX, mouseY)) {
            final List<String> tooltip = this.getItemToolTip(preview);
            final ProcessingRecipe recipe = this.selectedRecipe();
            // Chance outputs (the wash plant's nuggets) say how often they come out
            if (recipe != null && recipe.chance() < 1) {
                tooltip.add(TextFormatting.GRAY + I18n.format("gui.almura.machine.chance", CHANCE_FORMAT.format(recipe.chance() * 100)));
            }
            this.drawHoveringText(tooltip, mouseX, mouseY);
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
            this.itemRender.renderItemAndEffectIntoGUI(preview, this.previewX, this.previewY);
            this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, preview, this.previewX, this.previewY, null);
            RenderHelper.disableStandardItemLighting();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(this.usesWater() ? WATER_BACKGROUND : BACKGROUND);
        final int left = this.guiLeft;
        final int top = this.guiTop;
        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);

        // Power meter fills with the stored energy (always full when no power is required)
        final int energy = this.requiresPower() ? this.scaled(this.container.property(ProcessingContainer.PROPERTY_ENERGY),
                this.container.property(ProcessingContainer.PROPERTY_CAPACITY), ENERGY_WIDTH) : ENERGY_WIDTH;
        if (energy > 0) {
            this.drawTexturedModalRect(left + ENERGY_X, top + ENERGY_Y, ENERGY_U, ENERGY_V, energy, ENERGY_HEIGHT);
        }

        // Water gauge fills from the bottom
        if (this.usesWater()) {
            final int water = this.scaled(this.container.property(ProcessingContainer.PROPERTY_WATER),
                    this.container.property(ProcessingContainer.PROPERTY_WATER_CAPACITY), WATER_HEIGHT);
            if (water > 0) {
                this.drawTexturedModalRect(left + WATER_X, top + WATER_BOTTOM - water, WATER_U, WATER_V + WATER_HEIGHT - water, WATER_WIDTH, water);
            }
        }

        // Progress icon fills left to right
        final int progress = this.scaled(this.container.property(ProcessingContainer.PROPERTY_PROGRESS), ProcessingTileEntity.PROCESS_TICKS,
                PROGRESS_WIDTH);
        if (progress > 0) {
            this.drawTexturedModalRect(left + PROGRESS_X, top + PROGRESS_Y, PROGRESS_U, PROGRESS_V, progress, PROGRESS_HEIGHT);
        }
    }

    /**
     * What the selector picks between: the machine's modes, or the recipes for what's in the input slot (the same list, in the same order,
     * as the server's).
     */
    private List<ProcessingRecipe> recipes() {
        final ItemStack input = this.container.machine().getInventory().getStackInSlot(ProcessingTileEntity.INPUT_SLOT);
        return this.container.machine().type().recipes().selectable(input);
    }

    /**
     * What the machine will make: the selected recipe's output, or nothing.
     */
    private ItemStack preview() {
        final ProcessingRecipe recipe = this.selectedRecipe();
        return recipe == null ? ItemStack.EMPTY : recipe.output();
    }

    @Nullable
    private ProcessingRecipe selectedRecipe() {
        final List<ProcessingRecipe> recipes = this.recipes();
        if (recipes.isEmpty()) {
            return null;
        }
        final int selected = this.container.property(ProcessingContainer.PROPERTY_SELECTED);
        return recipes.get(selected < recipes.size() ? selected : 0);
    }

    private boolean usesWater() {
        return this.container.machine().type().usesWater();
    }

    private boolean requiresPower() {
        return this.container.property(ProcessingContainer.PROPERTY_REQUIRES_POWER) != 0;
    }

    private int scaled(final int value, final int max, final int size) {
        return max <= 0 ? 0 : Math.min(size, value * size / max);
    }
}
