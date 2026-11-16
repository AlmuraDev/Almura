/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.machine.ProcessingBlockEntity;
import com.almuradev.almura.feature.machine.ProcessingMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

/**
 * A processing machine's screen, as 3.5's {@code ProcessingGui} on its texture: the power meter on the left (always full: 4.0's machines need
 * no power), what the machine will make in the middle (with arrows to pick the output when the input has more than one recipe, and Start /
 * Stop below on the kitchen machines), and the progress icon between the input slot above and the output slot below. Client only.
 */
public final class ProcessingScreen extends AbstractContainerScreen<ProcessingMenu> {

    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(Almura.ID, "textures/gui/machine/seed_processor.png");
    // Power meter: frame on the background, fill sprite at (208, 0)
    private static final int ENERGY_FRAME_X = 18, ENERGY_FRAME_Y = 40, ENERGY_FRAME_WIDTH = 32, ENERGY_FRAME_HEIGHT = 17;
    private static final int ENERGY_X = 22, ENERGY_Y = 44, ENERGY_WIDTH = 24, ENERGY_HEIGHT = 9, ENERGY_U = 208, ENERGY_V = 0;
    // Progress icon: fills left to right with the sprite at (176, 0)
    private static final int PROGRESS_X = 124, PROGRESS_Y = 38, PROGRESS_WIDTH = 20, PROGRESS_HEIGHT = 19, PROGRESS_U = 176, PROGRESS_V = 0;
    // The output selector around the preview, and Start / Stop below it
    private static final int ARROW_Y = 24, PREVIOUS_X = 60, NEXT_X = 104, ARROW_WIDTH = 12, ARROW_HEIGHT = 20;
    private static final int START_X = 62, START_Y = 50, START_WIDTH = 52, START_HEIGHT = 20;
    private static final int LABEL_COLOUR = 0xFF404040;

    private @Nullable Button previous, next, start;

    public ProcessingScreen(final ProcessingMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.previous = this.addRenderableWidget(Button.builder(Component.literal("<"), button -> this.press(ProcessingMenu.BUTTON_PREVIOUS))
            .bounds(this.leftPos + PREVIOUS_X, this.topPos + ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT).build());
        this.next = this.addRenderableWidget(Button.builder(Component.literal(">"), button -> this.press(ProcessingMenu.BUTTON_NEXT))
            .bounds(this.leftPos + NEXT_X, this.topPos + ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT).build());
        this.start = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.press(ProcessingMenu.BUTTON_START_STOP))
            .bounds(this.leftPos + START_X, this.topPos + START_Y, START_WIDTH, START_HEIGHT).build());
        this.updateButtons();
    }

    /**
     * Vanilla's menu button message: the server's menu cycles the output or starts / stops the machine.
     */
    private void press(final int button) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, button);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateButtons();
    }

    /**
     * The arrows only when there's a choice; machines that wait for the player then also show Start / Stop.
     */
    private void updateButtons() {
        final boolean choice = this.menu.data(ProcessingBlockEntity.DATA_CHOICES) > 1;
        if (this.previous != null && this.next != null && this.start != null) {
            this.previous.visible = choice;
            this.next.visible = choice;
            this.start.visible = choice && this.menu.type().startOnChoice();
            this.start.setMessage(Component.translatable(this.menu.data(ProcessingBlockEntity.DATA_STARTED) != 0
                ? "gui.almura.machine.stop" : "gui.almura.machine.start"));
        }
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        final int left = this.leftPos, top = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, left, top, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);
        // The power meter, full: no power is needed
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, left + ENERGY_X, top + ENERGY_Y, ENERGY_U, ENERGY_V, ENERGY_WIDTH, ENERGY_HEIGHT,
            256, 256);
        // The progress icon fills left to right
        final int progress = Math.min(PROGRESS_WIDTH,
            this.menu.data(ProcessingBlockEntity.DATA_PROGRESS) * PROGRESS_WIDTH / ProcessingBlockEntity.PROCESS_TICKS);
        if (progress > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, left + PROGRESS_X, top + PROGRESS_Y, PROGRESS_U, PROGRESS_V, progress,
                PROGRESS_HEIGHT, 256, 256);
        }
    }

    @Override
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int xm, final int ym) {
        graphics.text(this.font, this.title, (this.imageWidth - this.font.width(this.title)) / 2, 6, LABEL_COLOUR, false);
        graphics.text(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, LABEL_COLOUR, false);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (this.isHovering(ENERGY_FRAME_X, ENERGY_FRAME_Y, ENERGY_FRAME_WIDTH, ENERGY_FRAME_HEIGHT, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, Component.translatable("gui.almura.machine.no_power_required"), mouseX, mouseY);
        } else if (this.isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            final int percent = this.menu.data(ProcessingBlockEntity.DATA_PROGRESS) * 100 / ProcessingBlockEntity.PROCESS_TICKS;
            graphics.setTooltipForNextFrame(this.font, Component.translatable("gui.almura.machine.progress", percent), mouseX, mouseY);
        }
    }
}
