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
import org.lwjgl.input.Mouse;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Optional;

public class IngameFarmersAlmanac extends SimpleScreen {

    private static final NumberFormat numberFormat = new DecimalFormat("0.00");
    private static final String formTitle = "Farmer's Almanac";
    private static final int formWidth = 145;
    private static final int formHeight = 60;
    private static final int propertyContainerMaxHeight = 120;
    private static final int formAlpha = 185;
    private static final int leftPad = 8;
    private final Minecraft client = Minecraft.getMinecraft();
    private final ClientboundWorldPositionInformationPacket message;
    private UIBackgroundContainer propertyContainer;
    private int lastPropertyY = 4;

    private boolean unlockMouse = true;
    private int lastUpdate = 0;

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

        // Property container
        this.propertyContainer = new UIBackgroundContainer(this);
        new UISlimScrollbar(this, this.propertyContainer, UIScrollBar.Type.VERTICAL).setAutoHide(true);
        this.propertyContainer.setBackgroundAlpha(50);
        this.propertyContainer.setPosition(0, SimpleScreen.getPaddedY(separator, 5));

        form.add(this.propertyContainer);

        // Get all displayed properties
        loadProperties(world, blockState, blockPos);

        // Adjust the size of the container to reach at most the specified max height
        propertyContainer.setSize(UIComponent.INHERITED, Math.min(propertyContainerMaxHeight, propertyContainer.getContentHeight()));

        final UIButton closeButton = new UIButtonBuilder(this)
                .text("Close")
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(-4, -4)
                .onClick(this::close)
                .build("button.close");
        form.add(closeButton);

        // Adjust the size of the form to better fit content
        form.setSize(form.getContentWidth() + leftPad, form.getContentHeight() + closeButton.getHeight() + 10);

        // Readjust size for width because MalisisCore doesn't account for the scrollbar with UIComponent.INHERITED
        propertyContainer.setSize(propertyContainer.getWidth() - 1, propertyContainer.getHeight());

        addToScreen(form);

        Mouse.setGrabbed(false);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }
        if (++this.lastUpdate > 100) {
            // I am winning now.
        }
    }

    @SuppressWarnings("unchecked")
    private void loadProperties(World world, IBlockState blockState, BlockPos blockPos) {
        final Hydration hydration = getHydrationDefinition(blockState).orElse(null);
        if (hydration != null) {
            this.addLineLabel(Text.of(TextColors.WHITE, "Hydrated by:"));

            hydration.blockStates().forEach(bs -> {
                this.addLineLabel(Text.of(TextColors.WHITE, "- ", bs.block().getLocalizedName()), 2, false);
                bs.properties().forEach(property -> bs.value(property).ifPresent(stateValue -> {
                    final String formattedValue;
                    if (stateValue instanceof RangeStateValue) {
                        final RangeStateValue rangeStateValue = (RangeStateValue) stateValue;
                        formattedValue = numberFormat.format(rangeStateValue.min()) + "-" + numberFormat.format(rangeStateValue.max());
                    } else {
                        final Comparable rawValue = stateValue.get((IProperty) property);
                        formattedValue = rawValue == null ? "" : rawValue.toString();
                    }
                    this.addLineLabel(this.getGenericPropertyText("- " + property.getName(), formattedValue), 6, false);
                }));
            });
            this.addLineLabel(this.getGenericPropertyText("Hydration Radius", String.valueOf(hydration.getMaxRadius())));
        }

        // Growth stage
        getGrowthStage(blockState)
                .ifPresent(growthStage -> this.addLineLabel(this.getGenericPropertyText("Growth Stage",
                                String.format("%d of %d", growthStage.getFirst(), growthStage.getSecond()))));

        // Ground moisture
        final IBlockState moistureBlockStateTarget = blockState.getBlock() instanceof BlockCrops ? world.getBlockState(blockPos.down()) : blockState;
        final int moistureLevel = getMoistureLevel(moistureBlockStateTarget);
        final boolean isFertile = moistureLevel > 0;
        final TextColor fertileColor = isFertile ? TextColors.DARK_GREEN : TextColors.RED;
        this.addLineLabel(Text.of(TextColors.WHITE, "Moisture: ", fertileColor, isFertile ? "Fertile" : "Too dry"));

        // Ground temperature
        final DoubleRange temperatureRange = getTemperatureRange(world, blockState, blockPos).orElse(null);
        if (temperatureRange != null) {
            this.addLineLabel(Text.of(
                    TextColors.WHITE, "Ground Temperature: ",
                        (temperatureRange.min() == -1 || temperatureRange.max() == -1) ? TextColors.RED : TextColors.DARK_GREEN,
                        numberFormat.format(message.biomeTemperature)));
            this.addLineLabel(this.getGenericPropertyText("Required",
                    String.format("%s-%s", numberFormat.format(temperatureRange.min()), numberFormat.format(temperatureRange.max()))), 2, false);
        }

        // Biome rain
        this.addLineLabel(Text.of(TextColors.WHITE, "Rain: ",
                        message.biomeRainfall > 0.4 ? TextColors.DARK_GREEN : TextColors.RED, numberFormat.format(message.biomeRainfall)));

        // Sunlight value
        final int sunlight = world.getLightFor(EnumSkyBlock.SKY, blockPos) - world.getSkylightSubtracted();
        this.addLineLabel(this.getGenericPropertyText("Sunlight", String.valueOf(sunlight)));

        // Area light value
        final DoubleRange lightRange = getLightRange(world, blockState, blockPos).orElse(null);
        if (lightRange != null) {
            final int lightValue = world.getLightFor(EnumSkyBlock.SKY, blockPos);
            final TextColor valueColor = lightValue < 6 ? (sunlight < 6 ? TextColors.RED : TextColors.YELLOW) : TextColors.DARK_GREEN;
            this.addLineLabel(Text.of(TextColors.WHITE, "Area light: ", valueColor, String.valueOf(lightValue)));
            this.addLineLabel(this.getGenericPropertyText("Required",
                    String.format("%s-%s", numberFormat.format(lightRange.min()), numberFormat.format(lightRange.max()))), 2, false);
        }

        // Can Die
        this.addLineLabel(this.getGenericPropertyText("Can Die", String.valueOf(canRollback(blockState))));

        // Harvest Tool
        final String harvestTool = blockState.getBlock().getHarvestTool(blockState);
        if (harvestTool != null && !harvestTool.isEmpty()) {
            addLineLabel(this.getGenericPropertyText("Harvested by", harvestTool));
        }
    }

    private Text getGenericPropertyText(String key, String value) {
        return Text.of(TextColors.WHITE, key, ": ", TextColors.GRAY, value);
    }

    private void addLineLabel(Text text) {
        this.addLineLabel(text, 0, false);
    }

    private void addLineLabel(Text text, boolean multline) {
        this.addLineLabel(text, 0, multline);
    }

    @SuppressWarnings("deprecation")
    private void addLineLabel(Text text, int indent, boolean multiline) {
        final String prefix = indent > 0 ? String.format("%" + indent + "s", "") : "";
        final UILabel lineLabel = new UILabel(this, TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(prefix, text)), multiline);
        lineLabel.setPosition(leftPad, this.lastPropertyY);
        if (multiline) {
            lineLabel.setSize(lineLabel.getContentWidth(), lineLabel.getContentHeight());
        }
        this.propertyContainer.add(lineLabel);
        this.lastPropertyY += lineLabel.getHeight() + (multiline ? 0 : 2);
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
}
