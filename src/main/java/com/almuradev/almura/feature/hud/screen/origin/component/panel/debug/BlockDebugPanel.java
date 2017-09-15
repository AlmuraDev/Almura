/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel.debug;

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
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;

import javax.annotation.Nullable;

public class BlockDebugPanel extends AbstractDebugPanel {

    private boolean lookingAtBlock;

    public BlockDebugPanel(final MalisisGui gui, final int width, final int height) {
        super(gui, width, height);
    }

    @Override
    public void draw(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        if (!this.isVisible()) {
            return;
        }

        @Nullable final Entity view = this.getView();
        if (view == null) {
            return;
        }

        // Determine if we should draw
        final RayTraceResult omo = this.client.objectMouseOver;
        this.lookingAtBlock = omo != null && omo.typeOfHit == RayTraceResult.Type.BLOCK && omo.getBlockPos() != null;

        if (this.lookingAtBlock) {
            super.draw(renderer, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        super.drawForeground(renderer, mouseX, mouseY, partialTick);

        this.baseHeight = 22;
        this.client.mcProfiler.startSection("debug");

        // Draw block we're currently looking at
        if (this.lookingAtBlock) {
            final BlockPos pos = this.client.objectMouseOver.getBlockPos();
            final IBlockState state = getState(this.client.world, pos);

            this.clipContent = false;

            if (state.getBlock() != Blocks.AIR) {
                this.drawBlock(state, 4, this.autoHeight + 4);
                this.drawText(Text.of(TextColors.WHITE, Block.REGISTRY.getNameForObject(state.getBlock())), 24,
                        this.autoHeight - 14, false, true);

                final Map<IProperty<?>, Comparable<?>> properties = state.getProperties();
                final boolean hasProperties = !properties.isEmpty();

                if (hasProperties) {
                    this.autoHeight -= 2;
                }

                for (final Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
                    final IProperty<?> property = entry.getKey();
                    final String name = property.getName();
                    final Comparable<?> value = entry.getValue();
                    final String describedValue = getName(property, value);
                    if (value instanceof Boolean) {
                        this.drawText(Text.of(TextColors.WHITE, name, ": ", ((Boolean) value) ? TextColors.GREEN : TextColors.RED, describedValue),24, this.autoHeight);
                    } else {
                        this.drawProperty(name, describedValue, 24, this.autoHeight);
                    }
                }

                if (hasProperties) {
                    this.autoHeight -= 4;
                }
            }
        }

        this.autoHeight += 4; // Extra padding
        if (this.autoSize) {
            this.setSize(Math.max(this.autoSizeWidth(), this.baseWidth), Math.max(this.autoHeight, this.baseHeight));
            this.autoHeight = 0;
            this.autoWidth = 0;
        }

        this.client.mcProfiler.endSection();
    }

    private void drawBlock(final IBlockState state, final int x, final int y) {
        this.renderer.bindTexture(MalisisGui.BLOCK_TEXTURE);
        this.renderer.drawItemStack(new ItemStack(state.getBlock()), x, y);

        this.autoWidth = Math.max(16, this.autoWidth);
        this.autoHeight += 20;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getName(final IProperty<T> property, final Comparable<?> value) {
        return property.getName((T) value);
    }

    private static IBlockState getState(final World world, final BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
            state = state.getActualState(world, pos);
        }
        return state;
    }
}
