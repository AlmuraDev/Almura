/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud.debug;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.WorldType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;

public class UIDebugBlockPanel extends AbstractDebugPanel {

    private boolean isLookingAtBlock = false;

    public UIDebugBlockPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);
    }

    @Override
    public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (!this.isVisible()) {
            return;
        }

        final Entity view = this.getView();
        if (view == null) {
            return;
        }

        // Determine if we should draw
        final RayTraceResult omo = this.minecraft.objectMouseOver;
        this.isLookingAtBlock = omo != null && omo.typeOfHit == RayTraceResult.Type.BLOCK && omo.getBlockPos() != null;

        if (this.isLookingAtBlock) {
            super.draw(renderer, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        super.drawForeground(renderer, mouseX, mouseY, partialTick);

        this.baseHeight = 22;
        this.minecraft.mcProfiler.startSection("debug");

        // Draw block we're currently looking at
        if (this.isLookingAtBlock) {
            final BlockPos lookPos = this.minecraft.objectMouseOver.getBlockPos();

            IBlockState lookState = this.minecraft.world.getBlockState(lookPos);
            if (this.minecraft.world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                lookState = lookState.getActualState(this.minecraft.world, lookPos);
            }

            this.clipContent = false;

            if (lookState.getBlock() != Blocks.AIR) {
                this.drawBlock(renderer, lookState, 4, this.getAutoSizeHeight() + 4);
                this.drawText(renderer, Text.of(TextColors.WHITE, Block.REGISTRY.getNameForObject(lookState.getBlock())), 24,
                        this.autoHeight - 14, false, true);

                final boolean hasValidContent = !lookState.getProperties().isEmpty();

                if (hasValidContent) {
                    this.autoHeight -= 2;
                }

                for (Map.Entry<IProperty<?>, Comparable<?>> entry : lookState.getProperties().entrySet()) {
                    final IProperty property = entry.getKey();
                    final String name = property.getName();
                    final Comparable value = entry.getValue();
                    final String describedValue = property.getName(value);
                    if (value instanceof Boolean) {
                        this.drawText(renderer, Text.of(TextColors.WHITE, name, ": ", ((Boolean) value) ? TextColors.GREEN : TextColors.RED, describedValue),24, this.autoHeight);
                    } else {
                        this.drawProperty(renderer, name, describedValue, 24, this.autoHeight);
                    }
                }

                if (hasValidContent) {
                    this.autoHeight -= 4;
                }
            }
        }

        this.autoHeight += 4; // Extra padding

        // AutoSizing
        if (this.autoSize) {
            this.setSize(Math.max(this.getAutoSizeWidth(), this.baseWidth), Math.max(this.getAutoSizeHeight(), this.baseHeight));
            this.autoHeight = 0;
            this.autoWidth = 0;
        }

        this.minecraft.mcProfiler.endSection();
    }

    private void drawBlock(GuiRenderer renderer, IBlockState blockState, int x, int y) {
        renderer.bindTexture(MalisisGui.BLOCK_TEXTURE);
        renderer.drawItemStack(new ItemStack(blockState.getBlock()), x, y);

        // AutoSize properties
        this.autoWidth = Math.max(16, this.autoWidth);
        this.autoHeight += 20;
    }
}
