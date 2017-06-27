/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud.debug;

import com.almuradev.almura.client.gui.component.hud.UIHUDPanel;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import com.google.common.collect.ImmutableMap;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.WorldType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Map;

public class UIDebugBlockPanel extends UIHUDPanel {

    private static final TextTemplate KEY_VALUE_TEXT_TEMPLATE = TextTemplate.of(
            TextTemplate.arg("key").color(TextColors.WHITE), ": ",
            TextTemplate.arg("value").color(TextColors.GRAY));
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private MalisisFont font = MalisisFont.minecraftFont;
    private FontOptions fontOptions = FontOptionsConstants.FRO_COLOR_WHITE;
    private boolean isLookingAtBlock = false;
    private boolean autoSize = true;
    private int baseWidth;
    private int baseHeight;
    private int autoWidth;
    private int autoHeight;

    public UIDebugBlockPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);
        this.baseWidth = width;
        this.baseHeight = height;
    }

    @Override
    public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        final Entity view = this.minecraft.getRenderViewEntity();
        if (view == null || this.minecraft.player == null || this.minecraft.player.world == null || !this.minecraft.gameSettings.showDebugInfo) {
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
        final RayTraceResult omo = this.minecraft.objectMouseOver;
        if (this.isLookingAtBlock) {
            final BlockPos lookPos = omo.getBlockPos();

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

    public boolean getAutoSize() {
        return this.autoSize;
    }

    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }

    private int getAutoSizeWidth() {
        return this.autoWidth + 4;
    }

    private int getAutoSizeHeight() {
        return this.autoHeight;
    }

    private void drawText(GuiRenderer renderer, Text text, int x, int y) {
        drawText(renderer, text, x, y, true, true);
    }

    @SuppressWarnings("deprecation")
    private void drawText(GuiRenderer renderer, Text text, int x, int y, boolean adjustAutoHeight, boolean adjustAutoWidth) {
        renderer.drawText(this.font, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text), x, y, this.zIndex, this.fontOptions);

        // AutoSize properties
        if (adjustAutoHeight) {
            this.autoHeight += (int) this.font.getStringHeight(this.fontOptions) + 4;
        }
        if (adjustAutoWidth) {
            this.autoWidth = Math.max(Minecraft.getMinecraft().fontRenderer.getStringWidth(TextSerializers.PLAIN.serialize(text)) + x, this.autoWidth);
        }
    }

    private void drawProperty(GuiRenderer renderer, String key, String value, int x, int y) {
        drawProperty(renderer, key, value, x, y, true, true);
    }

    @SuppressWarnings("deprecation")
    private void drawProperty(GuiRenderer renderer, String key, String value, int x, int y, boolean adjustAutoHeight, boolean adjustAutoWidth) {
        final Text text = KEY_VALUE_TEXT_TEMPLATE.apply(ImmutableMap.of("key", Text.of(key),"value", Text.of(value))).build();
        renderer.drawText(this.font, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text), x, y, this.zIndex, this.fontOptions);

        // AutoSize properties
        if (adjustAutoHeight) {
            this.autoHeight += (int) this.font.getStringHeight(this.fontOptions) + 4;
        }
        if (adjustAutoWidth) {
            this.autoWidth = Math.max(Minecraft.getMinecraft().fontRenderer.getStringWidth(TextSerializers.PLAIN.serialize(text).trim()) + x + 2,
                    this.autoWidth);
        }
    }

    private void drawBlock(GuiRenderer renderer, IBlockState blockState, int x, int y) {
        renderer.bindTexture(MalisisGui.BLOCK_TEXTURE);
        renderer.drawItemStack(new ItemStack(blockState.getBlock()), x, y);

        // AutoSize properties
        this.autoWidth = Math.max(16, this.autoWidth);
        this.autoHeight += 20;
    }
}
