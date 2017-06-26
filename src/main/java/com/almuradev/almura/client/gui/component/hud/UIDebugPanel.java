/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud;

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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Map;
import java.util.Optional;


public class UIDebugPanel extends UIHUDPanel {

    private static final TextTemplate KEY_VALUE_TEXT_TEMPLATE = TextTemplate.of(
            TextTemplate.arg("key").color(TextColors.WHITE), ": ",
            TextTemplate.arg("value").color(TextColors.GRAY));
    private Minecraft minecraft = Minecraft.getMinecraft();
    private MalisisFont font = MalisisFont.minecraftFont;
    private FontOptions fontOptions = FontOptionsConstants.FRO_COLOR_WHITE;
    private boolean autoSize = true;
    private int baseWidth;
    private int baseHeight;
    private int autoWidth;
    private int autoHeight;

    public UIDebugPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);
        this.baseWidth = width;
        this.baseHeight = height;
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft.player == null || this.minecraft.player.world == null || !this.minecraft.gameSettings.showDebugInfo) {
            return;
        }

        super.drawForeground(renderer, mouseX, mouseY, partialTick);

        this.minecraft.mcProfiler.startSection("debug");

        // Properties
        final EntityLivingBase viewer = Optional.ofNullable((EntityLivingBase) this.minecraft.getRenderViewEntity()).orElse(this.minecraft.player);
        final BlockPos viewerPos = new BlockPos(viewer.posX, viewer.posY, viewer.posZ);
        final Chunk chunk = viewer.getEntityWorld().getChunkFromBlockCoords(viewerPos);

        // Title
        final String title = "Minecraft " + this.minecraft.getVersion();
        final int titleWidth = this.minecraft.fontRenderer.getStringWidth(title);
        this.drawText(renderer, Text.of(TextColors.GOLD, title), (Math.max(this.autoWidth, this.baseWidth) / 2) - (titleWidth / 2) - 2, 4);

        // Reset autoWidth after we've drawn the title
        if (this.autoSize) {
            this.autoWidth = 0;
        }

        // Keys/Values
        this.drawProperty(renderer, "Java", this.getJavaDetails(), 4, this.autoHeight += 4);
        this.drawProperty(renderer, "Memory", this.getMemoryDetails(), 4, this.getAutoSizeHeight());
        this.drawProperty(renderer, "FPS", String.valueOf(Minecraft.getDebugFPS()), 4, this.getAutoSizeHeight());
        this.drawProperty(renderer, "X", String.format("%.5f", this.minecraft.player.posX), 4, this.getAutoSizeHeight());
        this.drawProperty(renderer, "Y", String.format("%.5f", this.minecraft.player.posY), 4, this.getAutoSizeHeight());
        this.drawProperty(renderer, "Z", String.format("%.5f", this.minecraft.player.posZ), 4, this.getAutoSizeHeight());
        this.drawProperty(renderer, "Block", this.getBlockDetails(viewerPos), 4, this.getAutoSizeHeight());
        this.drawProperty(renderer, "Chunk", this.getChunkDetails(viewerPos), 4, this.getAutoSizeHeight());
        if (viewer.getEntityWorld().isBlockLoaded(viewerPos) && viewerPos.getY() >= 0 && viewerPos.getY() < 256 && !chunk.isEmpty()) {
            this.drawProperty(renderer, "Biome", chunk.getBiome(viewerPos, this.minecraft.world.getBiomeProvider()).getBiomeName(),
                    4, this.getAutoSizeHeight());
            this.drawProperty(renderer, "Light", this.getLightDetails(viewerPos, chunk),4, this.getAutoSizeHeight());
        }

        // Draw block we're currently looking at
        if (this.minecraft.objectMouseOver != null && this.minecraft.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
            final BlockPos blockPos = this.minecraft.objectMouseOver.getBlockPos();
            IBlockState blockState = this.minecraft.world.getBlockState(blockPos);

            if (this.minecraft.world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                blockState = blockState.getActualState(this.minecraft.world, blockPos);
            }

            this.clipContent = false;

            if (blockState.getBlock() != Blocks.AIR) {
                this.drawBlock(renderer, blockState, 4, this.getAutoSizeHeight() + 2);
                this.drawText(renderer, Text.of(TextColors.WHITE, Block.REGISTRY.getNameForObject(blockState.getBlock())), 24,
                        this.autoHeight - 18, false, true);

                boolean hasValidContent = blockState.getProperties().entrySet().stream().anyMatch(e -> e.getValue() instanceof Boolean);

                if (hasValidContent) {
                    this.autoHeight -= 6;
                }

                for (Map.Entry<IProperty<?>, Comparable<?>> entry : blockState.getProperties().entrySet()) {
                    if (entry.getValue() instanceof Boolean) {
                        this.drawText(renderer,
                                Text.of(TextColors.WHITE, entry.getKey().getName(), ": ",
                                        ((Boolean) entry.getValue() ? TextColors.GREEN : TextColors.RED), entry.getValue()),24, this.autoHeight);

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

    private void drawProperty(GuiRenderer renderer, String key, String value, int x, int y, boolean adjustAutoHeight, boolean adjustAutoWidth) {
        final Text text = KEY_VALUE_TEXT_TEMPLATE.apply(ImmutableMap.of("key", Text.of(key),"value", Text.of(value))).build();
        renderer.drawText(this.font, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text), x, y, this.zIndex, this.fontOptions);

        // AutoSize properties
        if (adjustAutoHeight) {
            this.autoHeight += (int) this.font.getStringHeight(this.fontOptions) + 4;
        }
        if (adjustAutoWidth) {
            this.autoWidth = Math.max(Minecraft.getMinecraft().fontRenderer.getStringWidth(TextSerializers.PLAIN.serialize(text).trim()) + x + 2, this
                    .autoWidth);
        }
    }

    private void drawBlock(GuiRenderer renderer, IBlockState blockState, int x, int y) {
        //renderer.bindTexture(MalisisGui.BLOCK_TEXTURE);
        renderer.drawItemStack(new ItemStack(blockState.getBlock()), x, y);

        // AutoSize properties
        this.autoWidth = Math.max(16, this.autoWidth);
        this.autoHeight += 20;
    }

    private String getJavaDetails() {
        return System.getProperty("java.version") + " " + (this.minecraft.isJava64bit() ? 64 : 32) + "-bit";
    }

    private String getMemoryDetails() {
        final long maxMemory = Runtime.getRuntime().maxMemory();
        final long totalMemory = Runtime.getRuntime().totalMemory();
        final long freeMemory = Runtime.getRuntime().freeMemory();
        final long usedMemory = totalMemory - freeMemory;
        return (usedMemory * 100L / maxMemory) + "%% " + convertBytesToMegabytes(usedMemory) + "/" + convertBytesToMegabytes(maxMemory) + "MB";
    }

    private String getAllocatedDetails() {
        final long maxMemory = Runtime.getRuntime().maxMemory();
        final long totalMemory = Runtime.getRuntime().totalMemory();
        return (totalMemory * 100L / maxMemory) + "%% " + convertBytesToMegabytes(totalMemory) + "MB";
    }

    private String getBlockDetails(BlockPos viewerPos) {
        return viewerPos.getX() + ", " + viewerPos.getY() + ", " + viewerPos.getZ();
    }

    private String getChunkDetails(BlockPos viewerPos) {
        return (viewerPos.getX() % 15) + " " +
                (viewerPos.getY() % 15) + " " +
                (viewerPos.getZ() % 15) + " in " +
                (viewerPos.getX() >> 4) + " " +
                (viewerPos.getY() >> 4) + " " +
                (viewerPos.getZ() >> 4);
    }

    private String getLightDetails(BlockPos viewerPos, Chunk chunk) {
        return String.valueOf(chunk.getLightSubtracted(viewerPos, 0)) +
                " (" + chunk.getLightFor(EnumSkyBlock.SKY, viewerPos) + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, viewerPos) + " block)";
    }

    private static long convertBytesToMegabytes(long bytes) {
        return bytes / 1024L / 1024L;
    }
}
