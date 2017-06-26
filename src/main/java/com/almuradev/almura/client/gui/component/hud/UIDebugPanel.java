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
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Map;


public class UIDebugPanel extends UIHUDPanel {

    private static final TextTemplate KEY_VALUE_TEXT_TEMPLATE = TextTemplate.of(
            TextTemplate.arg("key").color(TextColors.WHITE), ": ",
            TextTemplate.arg("value").color(TextColors.GRAY));
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final Text title;
    private final int titleWidth;
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

        final String title = "Minecraft " + this.minecraft.getVersion();
        this.title = Text.of(TextColors.GOLD, title);
        this.titleWidth = this.minecraft.fontRenderer.getStringWidth(title) / 2;
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        final Entity view = this.minecraft.getRenderViewEntity();
        if (view == null || this.minecraft.player == null || this.minecraft.player.world == null || !this.minecraft.gameSettings.showDebugInfo) {
            return;
        }

        super.drawForeground(renderer, mouseX, mouseY, partialTick);

        this.minecraft.mcProfiler.startSection("debug");

        this.drawText(renderer, this.title, (Math.max(this.autoWidth, this.baseWidth) / 2) - this.titleWidth - 2, 4);
        // Reset autoWidth after we've drawn the title
        if (this.autoSize) {
            this.autoWidth = 0;
        }

        // Position
        final double x = view.posX;
        final int fx = MathHelper.floor(x);
        final double y = view.getEntityBoundingBox().minY; // vanilla uses minY of the AABB
        final int fy = MathHelper.floor(y);
        final double z = view.posZ;
        final int fz = MathHelper.floor(z);
        final BlockPos pos = new BlockPos(x, y, z);
        final Chunk chunk = view.getEntityWorld().getChunkFromBlockCoords(pos);

        // Standard details
        this.drawProperty(renderer, "Java", this.getJavaDetails(), 4, this.autoHeight += 4);
        this.drawProperty(renderer, "Memory", this.getMemoryDetails(), 4, this.getAutoSizeHeight());
        this.drawProperty(renderer, "FPS", String.valueOf(Minecraft.getDebugFPS()), 4, this.getAutoSizeHeight());
        // Respect reduced debug
        if (this.minecraft.isReducedDebug()) {
            this.drawProperty(renderer, "Chunk-relative", String.format("%d %d %d", fx & 15, fy & 15, fz & 15), 4, this.getAutoSizeHeight());
        } else {
            this.drawProperty(renderer, "X", String.format("%.3f", x), 4, this.getAutoSizeHeight());
            this.drawProperty(renderer, "Y", String.format("%.3f", y), 4, this.getAutoSizeHeight());
            this.drawProperty(renderer, "Z", String.format("%.3f", z), 4, this.getAutoSizeHeight());
            final EnumFacing facing = view.getHorizontalFacing();
            final String facingTowards;
            switch (facing) {
                case NORTH:
                    facingTowards = " (-z)";
                    break;
                case SOUTH:
                    facingTowards = " (+z)";
                    break;
                case WEST:
                    facingTowards = " (-x)";
                    break;
                case EAST:
                    facingTowards = " (+x)";
                    break;
                default: // invalid
                    facingTowards = "";
            }
            this.drawProperty(renderer, "Facing", String.format("%s%s (%.1f, %.1f)", facing.name().charAt(0), facingTowards,
                    MathHelper.wrapDegrees(view.rotationYaw), MathHelper.wrapDegrees(view.rotationPitch)), 4, this.getAutoSizeHeight());
            this.drawProperty(renderer, "Block", fx + ", " + fy + ", " + fz, 4, this.getAutoSizeHeight());
            this.drawProperty(renderer, "Chunk", String.format("%d %d %d in %d %d %d", fx & 15, fy & 15, fz & 15, fx >> 4, fy >> 4, fz >> 4), 4, this.getAutoSizeHeight());
            if (view.getEntityWorld().isBlockLoaded(pos) && pos.getY() >= 0 && pos.getY() < 256 && !chunk.isEmpty()) {
                this.drawProperty(renderer, "Biome", chunk.getBiome(pos, this.minecraft.world.getBiomeProvider()).getBiomeName(),
                        4, this.getAutoSizeHeight());
                this.drawProperty(renderer, "Light", this.getLightDetails(pos, chunk),4, this.getAutoSizeHeight());
            }
        }

        // Draw block we're currently looking at
        final RayTraceResult omo = this.minecraft.objectMouseOver;
        if (omo != null && omo.typeOfHit == RayTraceResult.Type.BLOCK && omo.getBlockPos() != null) {
            final BlockPos lookPos = omo.getBlockPos();

            this.drawProperty(renderer, "Look", String.format("%d %d %d", lookPos.getX(), lookPos.getY(), lookPos.getZ()), 4, this.getAutoSizeHeight());

            IBlockState lookState = this.minecraft.world.getBlockState(lookPos);
            if (this.minecraft.world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                lookState = lookState.getActualState(this.minecraft.world, lookPos);
            }

            this.clipContent = false;

            if (lookState.getBlock() != Blocks.AIR) {
                this.drawBlock(renderer, lookState, 4, this.getAutoSizeHeight() + 2);
                this.drawText(renderer, Text.of(TextColors.WHITE, Block.REGISTRY.getNameForObject(lookState.getBlock())), 24, this.autoHeight - 18, false, true);

                final boolean hasValidContent = !lookState.getProperties().isEmpty();

                if (hasValidContent) {
                    this.autoHeight -= 6;
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
        return System.getProperty("java.version") + " " + (this.minecraft.isJava64bit() ? 64 : 32) + "bit";
    }

    private String getMemoryDetails() {
        final long maxMemory = Runtime.getRuntime().maxMemory();
        final long totalMemory = Runtime.getRuntime().totalMemory();
        final long freeMemory = Runtime.getRuntime().freeMemory();
        final long usedMemory = totalMemory - freeMemory;
        return (usedMemory * 100L / maxMemory) + "%% " + convertBytesToMegabytes(usedMemory) + "/" + convertBytesToMegabytes(maxMemory) + "MB";
    }

    private String getLightDetails(BlockPos viewerPos, Chunk chunk) {
        return String.valueOf(chunk.getLightSubtracted(viewerPos, 0)) +
                " (" + chunk.getLightFor(EnumSkyBlock.SKY, viewerPos) + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, viewerPos) + " block)";
    }

    private static long convertBytesToMegabytes(long bytes) {
        return bytes / 1024L / 1024L;
    }
}
