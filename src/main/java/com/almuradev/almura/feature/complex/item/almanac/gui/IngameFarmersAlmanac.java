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
import com.almuradev.almura.shared.util.MathUtil;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.math.BigDecimal;
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
    private UILabel cropStatusLabel;
    private int lastPropertyY = 4;

    private boolean unlockMouse = true;
    private int lastUpdate = 0;

    private boolean growing = true;
    private boolean readyForHarvest = false;

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

        // Line separator
        final UISeparator separator = new UISeparator(this);
        separator.setSize(form.getWidth(), 1);
        separator.setPosition(0, -5, Anchor.TOP | Anchor.CENTER);
        form.add(separator);

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
        final UISeparator separator2 = new UISeparator(this);
        separator2.setSize(form.getWidth(), 1);
        separator2.setPosition(0, SimpleScreen.getPaddedY(labelRegistryName, 3), Anchor.TOP | Anchor.CENTER);
        form.add(separator2);

        // Property container
        this.propertyContainer = new UIBackgroundContainer(this);
        new UISlimScrollbar(this, this.propertyContainer, UIScrollBar.Type.VERTICAL).setAutoHide(true);
        this.propertyContainer.setBackgroundAlpha(50);
        this.propertyContainer.setPosition(0, SimpleScreen.getPaddedY(separator2, 5));

        form.add(this.propertyContainer);

        this.cropStatusLabel = new UILabel(this); // Init this early so we can load stuff to it later.

        // Get all displayed properties
        loadProperties(world, blockState, blockPos);

        // Adjust the size of the container to reach at most the specified max height
        this.propertyContainer.setSize(UIComponent.INHERITED, Math.min(propertyContainerMaxHeight, propertyContainer.getContentHeight()));

        // Line separator
        final UISeparator separator3 = new UISeparator(this);
        separator3.setSize(form.getWidth(), 1);
        separator3.setPosition(0,-22, Anchor.BOTTOM | Anchor.CENTER);
        form.add(separator3);

        // Localized name

        if (this.growing) {
            this.cropStatusLabel.setText(TextFormatting.DARK_GREEN + "Growing...");
        } else {
            this.cropStatusLabel.setText(TextFormatting.RED + "Dying!");
        }

        if (this.readyForHarvest) {
            this.cropStatusLabel.setText(TextFormatting.GREEN + "Ready for Harvest!");
        }

        this.cropStatusLabel.setFontOptions(FontColors.WHITE_FO);
        this.cropStatusLabel.setPosition(5,-5, Anchor.BOTTOM | Anchor.LEFT);
        form.add(cropStatusLabel);

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
        this.propertyContainer.setSize(propertyContainer.getWidth() - 1, propertyContainer.getHeight());

        addToScreen(form);

        Mouse.setGrabbed(false);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (this.unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            this.unlockMouse = false; // Only unlock once per session.
        }
        if (++this.lastUpdate > 100) {
            // I am winning now.
        }
    }

    @SuppressWarnings("unchecked")
    private void loadProperties(World world, IBlockState blockState, BlockPos targetBlockPos) {
        final Hydration hydration = getHydrationDefinition(blockState).orElse(null);
        if (hydration != null) {
            this.addLineLabel(Text.of(TextColors.WHITE, "Hydrated by:"));

            hydration.blockStates().forEach(bs -> {
                this.addLineLabel(Text.of(TextColors.WHITE, "- ", TextColors.YELLOW, bs.block().getLocalizedName()), 2);
                bs.properties().forEach(property -> bs.value(property).ifPresent(stateValue -> {
                    final String formattedValue;
                    if (stateValue instanceof RangeStateValue) {
                        final RangeStateValue rangeStateValue = (RangeStateValue) stateValue;
                        formattedValue = numberFormat.format(rangeStateValue.min()) + "-" + numberFormat.format(rangeStateValue.max());
                    } else {
                        final Comparable rawValue = stateValue.get((IProperty) property);
                        formattedValue = rawValue == null ? "" : rawValue.toString();
                    }
                    this.addLineLabel(this.getGenericPropertyText("- " + property.getName(), formattedValue), 6);
                }));
                //Todo: this MAX radius check, is it intended to be per block or per hydration or per crop?
                if (hydration.getMaxRadius()> 0) {  //Range > 0 for only things such as water, never farmland.
                    this.addLineLabel(this.getGenericPropertyText("- radius", String.valueOf(hydration.getMaxRadius())), 6);

                    boolean found = false;
                    for (final BlockPos.MutableBlockPos inRange : BlockPos.getAllInBoxMutable(
                            targetBlockPos.add(-hydration.getMaxRadius(), -1, -hydration.getMaxRadius()),
                            targetBlockPos.add(hydration.getMaxRadius(), -1, hydration.getMaxRadius())
                    )) {
                        IBlockState inRangeState = null;
                        if (inRange.equals(targetBlockPos)) {
                            inRangeState = null;
                        } else {
                            inRangeState = world.getBlockState(inRange);
                        }
                        if (inRangeState != null && hydration.doesStateMatch(inRangeState)) {
                            found = true;
                        }
                    }
                    if (found) {
                        this.addLineLabel(Text.of(TextColors.WHITE, "- found: ", TextColors.DARK_GREEN, "Yes"), 6);
                    } else {
                        this.addLineLabel(Text.of(TextColors.WHITE, "- found: ", TextColors.RED, "No"), 6);
                        this.growing = false;
                        //ToDo: this design only allows for ONE hydration block to be used before this breaks.
                    }
                }
            });
        }

        // Growth stage
        getGrowthStage(blockState)
                .ifPresent(growthStage ->
                        this.addLineLabel(this.getGenericPropertyText("Growth Stage",String.format("%d of %d", growthStage.getFirst() + 1, growthStage.getSecond() + 1))));

        getGrowthStage(blockState)
                .ifPresent(growthStage ->
                        readyForHarvest = growthStage.getFirst() == growthStage.getSecond());

        // Ground moisture
        final IBlockState moistureBlockStateTarget = blockState.getBlock() instanceof BlockCrops ? world.getBlockState(targetBlockPos.down()) : blockState;
        final int moistureLevel = getMoistureLevel(moistureBlockStateTarget);
        final boolean isFertile = (moistureLevel > 0) || (hydration != null && growing);  //Todo: theoretically this should work.
        final TextColor fertileColor = isFertile ? TextColors.DARK_GREEN : TextColors.RED;
        this.addLineLabel(Text.of(TextColors.WHITE, "Soil Quality: ", fertileColor, isFertile ? "Fertile" : "Too dry"));
        if (isFertile) {
            final TextColor moistureColor = moistureLevel > 3 ? TextColors.DARK_GREEN : TextColors.YELLOW;
            this.addLineLabel(Text.of(TextColors.WHITE, "Moisture Level: ", moistureColor, moistureLevel), 2);
        } else {
            this.growing = false;
        }

        // Ground temperature
        final DoubleRange temperatureRange = getTemperatureRange(world, blockState, targetBlockPos).orElse(null);

        if (temperatureRange != null) {
            if (!MathUtil.withinRange(this.round(message.biomeTemperature,2), temperatureRange.min(), temperatureRange.max())) {
                this.growing = false;
            }
            final TextColor temperatureColor = MathUtil.withinRange(this.round(message.biomeTemperature,2), temperatureRange.min(), temperatureRange.max())
                    ? TextColors.DARK_GREEN
                    : TextColors.RED;
            this.addLineLabel(Text.of(TextColors.WHITE, "Ground Temperature: ", temperatureColor, numberFormat.format(message.biomeTemperature)));
            this.addLineLabel(this.getGenericPropertyText("Required",
                    String.format("%s-%s", numberFormat.format(temperatureRange.min()), numberFormat.format(temperatureRange.max()))), 2);
        } else { //Is a farmland block
            this.addLineLabel(Text.of(TextColors.WHITE, "Ground Temperature: ", TextColors.DARK_GREEN, numberFormat.format(message.biomeTemperature)));
        }

        // Biome rain
        this.addLineLabel(Text.of(TextColors.WHITE, "Rain: ",
                message.biomeRainfall > 0.5 ? TextColors.DARK_GREEN : TextColors.RED, numberFormat.format(message.biomeRainfall)));

        // Server light values, can't trust client world. lookups.
        final int sunlight = message.skyLight;
        final int areaLight = message.blockLight;
        final int combinedLightValue = message.combinedLight;
        final boolean isDaytime = message.isDaytime;
        final boolean canSeeSky = message.canSeeSky;

        // Area light value
        final DoubleRange lightRange = getLightRange(world, blockState, targetBlockPos).orElse(null);
        if (lightRange != null) {
            if (!MathUtil.withinRange(combinedLightValue, lightRange.min(), lightRange.max())) {
                if (!isDaytime && !canSeeSky) // Its night time and the crop is NOT exposed to direct sunlight
                    this.growing = false;
            }

            TextColor combinedlightColor = MathUtil.withinRange(combinedLightValue, lightRange.min(), lightRange.max()) ? TextColors.DARK_GREEN : TextColors.RED;
            if (!isDaytime && canSeeSky) {
                combinedlightColor = TextColors.YELLOW; // Night time and the crop IS exposed to direct sunlight.
            }

            this.addLineLabel(Text.of(TextColors.WHITE, "Combined Light: ", combinedlightColor, String.valueOf(combinedLightValue)));
            if (!isDaytime && canSeeSky && !readyForHarvest) {
                this.addLineLabel(Text.of(TextColors.GRAY, "[Night Time Growth]"),3);
            }
            this.addLineLabel(this.getGenericPropertyText("Required",
                    String.format("%s-%s", numberFormat.format(lightRange.min()), numberFormat.format(lightRange.max()))), 2);
            this.addLineLabel(Text.of(TextColors.WHITE, "Sunlight: ", TextColors.YELLOW, String.valueOf(sunlight)),4);
            this.addLineLabel(Text.of(TextColors.WHITE, "Area light: ", TextColors.YELLOW, String.valueOf(areaLight)),4);
        } else { // is farmland
            this.addLineLabel(Text.of(TextColors.WHITE, "Combined Light: ", TextColors.DARK_GREEN, String.valueOf(combinedLightValue)));
        }

        // Can Die
        if (!(blockState.getBlock() instanceof BlockFarmland)) {
            if (!this.readyForHarvest) {
                if (String.valueOf(canRollback(blockState)).equalsIgnoreCase("true")) {
                    this.addLineLabel((Text.of(TextColors.WHITE, "Can Die: ", TextColors.RED, "Yes")));
                } else {
                    this.addLineLabel((Text.of(TextColors.WHITE, "Can Die: ", TextColors.DARK_GREEN, "No")));
                }
            }

        } else {
            this.cropStatusLabel.setVisible(false);
        }

        // Harvest Tool
        final String harvestTool = blockState.getBlock().getHarvestTool(blockState);
        if (harvestTool != null && !harvestTool.isEmpty()) {
            this.addLineLabel(this.getGenericPropertyText("Harvested by", harvestTool));
        }
    }

    private Text getGenericPropertyText(String key, String value) {
        return Text.of(TextColors.WHITE, key, ": ", TextColors.GRAY, value);
    }

    private void addLineLabel(Text text) {
        this.addLineLabel(text, 0);
    }

    @SuppressWarnings("deprecation")
    private void addLineLabel(Text text, int indent) {
        final String prefix = indent > 0 ? String.format("%" + indent + "s", "") : "";
        final UILabel lineLabel = new UILabel(this, TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(prefix, text)));
        lineLabel.setPosition(leftPad, this.lastPropertyY);
        this.propertyContainer.add(lineLabel);
        this.lastPropertyY += lineLabel.getHeight() + 2;
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

    private static float round(float number, int decimalPlace) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
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
