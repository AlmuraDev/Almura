/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.almanac.gui;

import com.almuradev.almura.feature.complex.item.almanac.asm.interfaces.IMixinBlockCrops;
import com.almuradev.almura.feature.complex.item.almanac.network.ClientboundWorldPositionInformationPacket;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.content.type.block.state.value.RangeStateValue;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import com.almuradev.content.type.block.type.crop.processor.growth.Growth;
import com.almuradev.content.type.block.type.crop.processor.hydration.Hydration;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinition;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class IngameFarmersAlmanac extends SimpleScreen {

    private static final NumberFormat numberFormat = new DecimalFormat("0.00");
    private static final String formTitle = "Farmer's Almanac";
    private static final int formWidth = 145;
    private static final int formHeight = 60;
    private static final int propertyContainerMaxHeight = 120;
    private static final int formAlpha = 185;
    private static final int leftPad = 8;
    private final List<Property> properties = new LinkedList<>();
    private final Minecraft client = Minecraft.getMinecraft();

    private final ClientboundWorldPositionInformationPacket message;

    public IngameFarmersAlmanac(ClientboundWorldPositionInformationPacket message) {
        this.message = message;
    }

    // TODO: Translation support
    @SuppressWarnings("deprecation")
    @Override
    public void construct() {
        guiscreenBackground = false;

        final UIFormContainer form = new UIFormContainer(this, formWidth, formHeight, formTitle);

        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(false);
        form.setBorder(FontColors.WHITE, 1, formAlpha);
        form.setBackgroundAlpha(formAlpha);

        final WorldClient world = this.client.world;
        final RayTraceResult omo = this.client.objectMouseOver;
        final BlockPos blockPos = omo.getBlockPos();
        final IBlockState blockState = getState(world, blockPos);
        final Block block = blockState.getBlock();

        // Block image
        final ItemStack pickStack = blockState.getBlock().getPickBlock(blockState, omo, this.client.world, omo.getBlockPos(), this.client.player);
        final UIImage blockImage = new UIImage(this, pickStack);
        blockImage.setPosition(leftPad / 2, 0, Anchor.LEFT | Anchor.TOP);
        form.add(blockImage);

        // Localized name
        final UILabel labelLocalizedName = new UILabel(this, block.getLocalizedName());
        labelLocalizedName.setFontOptions(FontColors.WHITE_FO);
        labelLocalizedName.setPosition(SimpleScreen.getPaddedX(blockImage, 4), 0);
        form.add(labelLocalizedName);

        // Registry name
        final String registryName = block.getRegistryName() == null ? "unknown" : block.getRegistryName().toString();
        final UILabel labelRegistryName = new UILabel(this, registryName);
        labelRegistryName.setFontOptions(FontColors.GRAY_FO);
        labelRegistryName.setPosition(labelLocalizedName.getX(), SimpleScreen.getPaddedY(labelLocalizedName, 2));
        form.add(labelRegistryName);

        // Line separator
        final UISeparator separator = new UISeparator(this);
        separator.setSize(form.getWidth() - 15, 1);
        separator.setPosition(0, SimpleScreen.getPaddedY(labelRegistryName, 3), Anchor.TOP | Anchor.CENTER);
        form.add(separator);

        final UIBackgroundContainer propertyContainer = new UIBackgroundContainer(this);
        new UISlimScrollbar(this, propertyContainer, UIScrollBar.Type.VERTICAL).setAutoHide(true);
        propertyContainer.setBackgroundAlpha(50);
        propertyContainer.setPosition(0, SimpleScreen.getPaddedY(separator, 5));
        form.add(propertyContainer);

        // Get all displayed properties
        loadProperties(world, blockState, blockPos);

        // Add labels for all properties
        int lastY = 4;
        for (Property property : properties) {
            final UILabel propertyLabel = new UILabel(this, TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                    Text.of(property.indent > 0 ? String.format("%" + property.indent + "s", "") : "",
                            property.key, property.key.getColor(), ": ",
                            property.value)),
                    property.multiline);
            propertyLabel.setPosition(leftPad, lastY);
            if (property.multiline) {
                propertyLabel.setSize(propertyLabel.getContentWidth(), propertyLabel.getContentHeight());
            }
            propertyContainer.add(propertyLabel);
            lastY += propertyLabel.getHeight() + (property.multiline ? 0 : 2);
        }

        propertyContainer.setSize(UIComponent.INHERITED, Math.min(propertyContainerMaxHeight, propertyContainer.getContentHeight()));

        final UIButton closeButton = new UIButtonBuilder(this)
                .text("Close")
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(-4, -4)
                .onClick(this::close)
                .build("button.close");
        form.add(closeButton);

        form.setSize(form.getContentWidth() + leftPad, form.getContentHeight() + closeButton.getHeight() + 10);
        // Because UIComponent.INHERITED doesn't account for the scrollbar
        propertyContainer.setSize(propertyContainer.getWidth() - 1, propertyContainer.getHeight());

        addToScreen(form);
    }

    private void loadProperties(World world, IBlockState blockState, BlockPos blockPos) {
        // Sweet baby Guthix
        // TODO: This CAN be done better.
        final Hydration hydration = getHydrationDefinition(blockState).orElse(null);
        if (hydration != null) {
            final String sourceNamePrefix = "  - ";
            final String sourcePropertyPrefix = "      ";
            final List<String> sources = new LinkedList<>();
            hydration.blockStates().forEach(bs -> {
                final StringBuilder builder = new StringBuilder();
                builder.append(sourceNamePrefix).append(bs.block().getLocalizedName());
                bs.properties().forEach(property -> bs.value(property).ifPresent(stateValue -> {
                    final String formattedValue;
                    if (stateValue instanceof RangeStateValue) {
                        final RangeStateValue rangeStateValue = (RangeStateValue) stateValue;
                        formattedValue = numberFormat.format(rangeStateValue.min()) + "-" + numberFormat.format(rangeStateValue.max());
                    } else {
                        final Comparable rawValue = stateValue.get((IProperty) property);
                        if (rawValue != null) {
                            formattedValue = rawValue.toString();
                        } else {
                            formattedValue = "";
                        }
                    }
                    builder.append("\n").append(sourcePropertyPrefix).append(property.getName()).append(": ").append(formattedValue);
                }));
                sources.add(builder.toString());
            });
            properties.add(Property.builder()
                    .content(Text.of(TextColors.WHITE, "Hydrated by"),
                            Text.of(TextColors.GRAY, Text.NEW_LINE, String.join("\n", sources)))
                    .multiline(true)
                    .build());
            properties.add(Property.builder()
                    .content("Hydration Radius", String.valueOf(hydration.getMaxRadius()))
                    .build());
        }

        // Growth stage
        getGrowthStage(blockState).ifPresent(growthStage -> properties.add(Property.builder()
                .content("Growth Stage", String.format("%d of %d", growthStage.getFirst(), growthStage.getSecond())).build()));

        // Ground moisture
        final int moistureLevel = getMoistureLevel(blockState);
        final boolean isFertile = moistureLevel > 0;
        properties.add(Property.builder()
                .content(TextColors.WHITE, "Moisture", isFertile ? TextColors.DARK_GREEN : TextColors.RED, isFertile ? "Fertile" : "Too dry")
                .build());

        // Ground temperature
        final DoubleRange temperatureRange = getTemperatureRange(world, blockState, blockPos).orElse(null);
        if (temperatureRange != null) {
            properties.add(Property.builder()
                .content(TextColors.WHITE,
                        "Ground Temperature",
                        (temperatureRange.min() == -1 || temperatureRange.max() == -1) ? TextColors.RED : TextColors.DARK_GREEN,
                        numberFormat.format(message.biomeTemperature))
                .build());
            properties.add(Property.builder()
                .content("Required",
                    String.format("%s-%s", numberFormat.format(temperatureRange.min()), numberFormat.format(temperatureRange.max())))
                .indent(2)
                .build());
        }

        // Biome rain
        properties.add(Property.builder()
                .content(TextColors.WHITE, "Rain",
                        message.biomeRainfall > 0.4 ? TextColors.DARK_GREEN : TextColors.RED, numberFormat.format(message.biomeRainfall))
                .build());

        // Sunlight value
        final int sunlight = world.getLightFor(EnumSkyBlock.SKY, blockPos) - world.getSkylightSubtracted();
        properties.add(Property.builder().content("Sunlight", String.valueOf(sunlight)).build());

        // Area light value
        final DoubleRange lightRange = getLightRange(world, blockState, blockPos).orElse(null);
        if (lightRange != null) {
            final int lightValue = world.getLightFor(EnumSkyBlock.SKY, blockPos);
            final TextColor valueColor = lightValue < 6 ? (sunlight < 6 ? TextColors.RED : TextColors.YELLOW) : TextColors.DARK_GREEN;
            properties.add(Property.builder().content(TextColors.WHITE, "Area light", valueColor, String.valueOf(lightValue)).build());
            properties.add(Property.builder()
                    .content("Required", String.format("%s-%s", numberFormat.format(lightRange.min()), numberFormat.format(lightRange.max())))
                    .indent(2)
                    .build());
        }

        // Can Die
        properties.add(Property.builder().content("Can Die", String.valueOf(canRollback(blockState))).build());

        // Harvest Tool
        final String harvestTool = blockState.getBlock().getHarvestTool(blockState);
        if (harvestTool != null && !harvestTool.isEmpty()) {
            properties.add(Property.builder().content("Harvested by", harvestTool).build());
        }
    }

    private static IBlockState getState(final World world, final BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
            state = state.getActualState(world, pos);
        }
        return state;
    }

    private static int getMoistureLevel(IBlockState blockState) {
        if (blockState.getProperties().containsKey(BlockFarmland.MOISTURE)) {
            return blockState.getValue(BlockFarmland.MOISTURE);
        }

        return 0;
    }

    private static Optional<Tuple<Integer, Integer>> getGrowthStage(IBlockState blockState) {
        final Block block = blockState.getBlock();
        if (block instanceof IMixinBlockCrops) {
            final BlockCrops blockCrops = (BlockCrops) block;
            final PropertyInteger ageProperty = ((IMixinBlockCrops) block).getAgePropertyDirect();
            if (blockState.getProperties().containsKey(ageProperty)) {
                return Optional.of(Tuple.of((blockState.getValue(ageProperty)), blockCrops.getMaxAge()));
            }
        }

        return Optional.empty();
    }

    private static Optional<CropBlockStateDefinition> getBlockStateDefinition(IBlockState blockState) {
        final Block block = blockState.getBlock();
        if (block instanceof CropBlockImpl) {
            final CropBlockImpl cropBlockImpl = (CropBlockImpl) block;
            final int age = blockState.getValue(cropBlockImpl.getAgeProperty());
            return Optional.of(cropBlockImpl.state(age));
        }

        return Optional.empty();
    }

    private static boolean canRollback(IBlockState blockState) {
        return getBlockStateDefinition(blockState).map(d -> d.canRollback).orElse(false);
    }

    private static Optional<Growth> getGrowthDefinition(IBlockState blockState) {
        return getBlockStateDefinition(blockState).map(d -> d.growth);
    }

    private static Optional<Hydration> getHydrationDefinition(IBlockState blockState) {
        return getBlockStateDefinition(blockState).map(d -> d.hydration);
    }

    private static Optional<DoubleRange> getTemperatureRange(World world, IBlockState blockState, BlockPos blockPos) {
        final Growth growth = getGrowthDefinition(blockState).orElse(null);
        if (growth != null) {
            return Optional.ofNullable(growth.getOrLoadTemperatureRequiredRangeForBiome(world.getBiome(blockPos)));
        }

        return Optional.empty();
    }

    private static Optional<DoubleRange> getLightRange(World world, IBlockState blockState, BlockPos blockPos) {
        final Growth growth = getGrowthDefinition(blockState).orElse(null);
        if (growth != null) {
            return Optional.ofNullable(growth.getOrLoadLightRangeForBiome(world.getBiome(blockPos)));
        }

        return Optional.empty();
    }

    private static class Property {

        final Text key;
        final Text value;
        final int indent;
        final boolean multiline;

        Property(Text key, Text value, int indent, boolean multiline) {
            this.key = key;
            this.value = value;
            this.indent = indent;
            this.multiline = multiline;
        }

        static Builder builder() {
            return new Builder();
        }

        static class Builder {
            private Text key;
            private Text value;
            private int indent;
            private boolean multiline;

            Builder content(String key, String value) {
                return content(Text.of(TextColors.WHITE, key), Text.of(TextColors.GRAY, value));
            }

            Builder content(TextColor keyColor, String key, TextColor valueColor, String value) {
                return content(Text.of(keyColor, key), Text.of(valueColor, value));
            }

            Builder content(Text key, Text value) {
                this.key = key;
                this.value = value;
                return this;
            }

            Builder indent(int indent) {
                this.indent = indent;
                return this;
            }

            Builder multiline(boolean multiline) {
                this.multiline = multiline;
                return this;
            }

            Property build() {
                return new Property(key, value, indent, multiline);
            }
        }
    }
}
