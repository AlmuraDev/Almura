/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.asm.StaticAccess;
import com.almuradev.almura.core.client.config.ClientCategory;
import com.almuradev.almura.feature.hud.HUDType;
import com.almuradev.almura.feature.speed.FirstLaunchOptimization;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.slider.UISliderBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.base.Converter;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISlider;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

import javax.annotation.Nullable;

public class SimpleOptionsMenu extends SimpleScreen {

    private static final OptionsConverter OPTIONS_CONVERTER = new OptionsConverter();
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_WIDTH = 150;
    private static final int CONTROL_PADDING = 5;

    private UIButton buttonHudType, buttonOptimized;
    private UISlider<Integer> sliderOriginHudOpacity;
    @Nullable private UIComponent lastComponent = null;

    public SimpleOptionsMenu(GuiOptions parentOptions) {
        super(parentOptions);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void construct() {
        final ClientCategory config = StaticAccess.config.get().client;
        this.saveAndLoad();

        final UILabel titleLabel = new UILabel(this, I18n.format("almura.menu_button.options"));
        titleLabel.setFontOptions(FontColors.WHITE_FO);
        titleLabel.setPosition(0, 20, Anchor.TOP | Anchor.CENTER);

        this.addToScreen(titleLabel);

        final UIBackgroundContainer optionsContainer = new UIBackgroundContainer(this, 335, 200);
        optionsContainer.setBackgroundAlpha(0);
        optionsContainer.setPosition(0, getPaddedY(titleLabel, CONTROL_PADDING), Anchor.TOP | Anchor.CENTER);

        /*
         * LEFT COLUMN
         */
        this.buttonOptimized = new UIButtonBuilder(this)
                .text("Load Optimized Defaults")
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .listener(this)
                .build("button.optimized");
        this.updatePosition(this.buttonOptimized, Anchor.LEFT);

        this.buttonHudType = new UIButtonBuilder(this)
                .text("HUD: " + config.hud.substring(0, 1).toUpperCase() + config.hud.substring(1))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .listener(this)
                .build("button.hudType");
        this.updatePosition(this.buttonHudType, Anchor.LEFT);

        // TODO: Add builder for checkbox component
        final boolean displayWorldCompassWidget = config.displayWorldCompassWidget;
        final UICheckBox checkboxWorldCompassWidget = new UICheckBox(this);
        checkboxWorldCompassWidget.setText(TextFormatting.WHITE + "Display World Compass Widget");
        checkboxWorldCompassWidget.setChecked(displayWorldCompassWidget);
        checkboxWorldCompassWidget.setName("checkbox.world_compass_widget");
        checkboxWorldCompassWidget.register(this);
        this.updatePosition(checkboxWorldCompassWidget, Anchor.LEFT);

        final boolean displayLocationWidget = config.displayLocationWidget;
        final UICheckBox checkboxLocationWidget = new UICheckBox(this);
        checkboxLocationWidget.setText(TextFormatting.WHITE + "Display Location Widget");
        checkboxLocationWidget.setChecked(displayLocationWidget);
        checkboxLocationWidget.setName("checkbox.location_widget");
        checkboxLocationWidget.register(this);
        this.updatePosition(checkboxLocationWidget, Anchor.LEFT);

        final boolean displayNumericHUDValues = config.displayNumericHUDValues;
        final UICheckBox checkboxNumericHUDValues = new UICheckBox(this);
        checkboxNumericHUDValues.setText(TextFormatting.WHITE + "Display Numeric HUD Values");
        checkboxNumericHUDValues.setChecked(displayNumericHUDValues);
        checkboxNumericHUDValues.setName("checkbox.numeric_hud_values");
        checkboxNumericHUDValues.register(this);
        this.updatePosition(checkboxNumericHUDValues, Anchor.LEFT);

        final boolean displayNames = config.displayNames;
        final UICheckBox checkboxDisplayNames = new UICheckBox(this);
        checkboxDisplayNames.setText(TextFormatting.WHITE + "Display Entity Names");
        checkboxDisplayNames.setChecked(displayNames);
        checkboxDisplayNames.setName("checkbox.display_names");
        checkboxDisplayNames.register(this);
        this.updatePosition(checkboxDisplayNames, Anchor.LEFT);

        final boolean displayHealthbars = config.displayHealthbars;
        final UICheckBox checkboxDisplayHealthbars = new UICheckBox(this);
        checkboxDisplayHealthbars.setText(TextFormatting.WHITE + "Display Entity Healthbars");
        checkboxDisplayHealthbars.setChecked(displayHealthbars);
        checkboxDisplayHealthbars.setName("checkbox.display_healthbars");
        checkboxDisplayHealthbars.register(this);
        this.updatePosition(checkboxDisplayHealthbars, Anchor.LEFT);

        final boolean disableOffhandTorchPlacement = config.disableOffhandTorchPlacement;
        final UICheckBox checkboxDisableOffhandTorchPlacement = new UICheckBox(this);
        checkboxDisableOffhandTorchPlacement.setText(TextFormatting.WHITE + "Disable Offhand Torch Placement");
        checkboxDisableOffhandTorchPlacement.setChecked(disableOffhandTorchPlacement);
        checkboxDisableOffhandTorchPlacement.setName("checkbox.disable_offhand_torch_placement");
        checkboxDisableOffhandTorchPlacement.register(this);
        this.updatePosition(checkboxDisableOffhandTorchPlacement, Anchor.LEFT);

        // Reset for new column
        this.lastComponent = null;

        /*
         * RIGHT COLUMN
         */
        final boolean isOrigin = config.hud.equalsIgnoreCase(HUDType.ORIGIN);
        this.sliderOriginHudOpacity = new UISliderBuilder(this, Converter.<Float, Integer>from(f -> (int) (f * 255), i -> (float) i / 255))
                .text("HUD Opacity: %d")
                .value(config.originHudOpacity)
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .listener(this)
                .enabled(isOrigin)
                .build("slider.originHudOpacity");
        this.sliderOriginHudOpacity.setAlpha(isOrigin ? 255 : 128);
        this.updatePosition(this.sliderOriginHudOpacity, Anchor.RIGHT);

        final UISlider<OptionsConverter.Options> sliderChestDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Chest Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.chestRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .listener(this)
                .build("slider.chestRenderDistance");
        this.updatePosition(sliderChestDistance, Anchor.RIGHT);

        final UISlider<OptionsConverter.Options> sliderSignTextDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Sign Text Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.signTextRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .listener(this)
                .build("slider.signTextRenderDistance");
        this.updatePosition(sliderSignTextDistance, Anchor.RIGHT);

        final UISlider<OptionsConverter.Options> sliderItemFrameDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Item Frame Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.itemFrameRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .listener(this)
                .build("slider.itemFrameRenderDistance");
        this.updatePosition(sliderItemFrameDistance, Anchor.RIGHT);

        final UISlider<OptionsConverter.Options> sliderPlayerNameRenderDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Player Name Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.playerNameRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .listener(this)
                .build("slider.playerNameRenderDistance");
        this.updatePosition(sliderPlayerNameRenderDistance, Anchor.RIGHT);

        final UISlider<OptionsConverter.Options> sliderEnemyNameRenderDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Enemy Name Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.enemyNameRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .listener(this)
                .build("slider.enemyNameRenderDistance");
        this.updatePosition(sliderEnemyNameRenderDistance, Anchor.RIGHT);

        final UISlider<OptionsConverter.Options> sliderAnimalNameRenderDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Animal Name Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.animalNameRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .listener(this)
                .build("slider.animalNameRenderDistance");
        this.updatePosition(sliderAnimalNameRenderDistance, Anchor.RIGHT);


        final UIButton buttonDone = new UIButtonBuilder(this)
                .text(I18n.format("gui.done"))
                .size(200, CONTROL_HEIGHT)
                .position(0, this.lastComponent == null ? 10 : getPaddedY(this.lastComponent, 10))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.done");

        optionsContainer.add(this.buttonOptimized, this.buttonHudType, this.sliderOriginHudOpacity, checkboxWorldCompassWidget, checkboxLocationWidget,
                checkboxNumericHUDValues, checkboxDisplayNames, checkboxDisplayHealthbars, checkboxDisableOffhandTorchPlacement,
                sliderChestDistance, sliderSignTextDistance, sliderItemFrameDistance, sliderPlayerNameRenderDistance,
                sliderEnemyNameRenderDistance, sliderAnimalNameRenderDistance, buttonDone);
        addToScreen(optionsContainer);
    }

    private void updatePosition(UIComponent component, int horizontalAnchor) {
        component.setPosition(0,
                this.lastComponent == null ? 0 : getPaddedY(this.lastComponent, CONTROL_PADDING),
                Anchor.TOP | horizontalAnchor);
        this.lastComponent = component;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    private void saveAndLoad() {
        StaticAccess.config.save();
        StaticAccess.config.load();
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName()) {
            case "button.optimized":
                FirstLaunchOptimization.optimizeGame(false);
                this.close();
                break;
            case "button.hudType":
                // Check if the current HUD is the Origin HUD
                boolean isOrigin = StaticAccess.config.get().client.hud.equalsIgnoreCase(HUDType.ORIGIN);
                StaticAccess.config.get().client.hud = isOrigin ? HUDType.VANILLA : HUDType.ORIGIN;
                // Flip the boolean since we're now on the vanilla HUD
                isOrigin = !isOrigin;

                this.buttonHudType.setText("HUD: " + (isOrigin ? "Origin" : "Vanilla"));
                this.sliderOriginHudOpacity.setAlpha(isOrigin ? 255 : 128);
                this.sliderOriginHudOpacity.setEnabled(isOrigin);

                if (isOrigin) {
                    this.sliderOriginHudOpacity.setTooltip((UITooltip) null);
                } else {
                    this.sliderOriginHudOpacity.setTooltip("Only available when Origin HUD is in use.");
                }

                break;
            case "button.done":
                this.close();
                break;
        }

        saveAndLoad();
    }

    @Subscribe
    public void onValueChange(ComponentEvent.ValueChange event) {
        switch (event.getComponent().getName()) {
            case "slider.originHudOpacity":
                StaticAccess.config.get().client.originHudOpacity = (int) event.getNewValue();
                break;
            case "slider.chestRenderDistance" :
                StaticAccess.config.get().client.chestRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
                break;
            case "slider.signTextRenderDistance" :
                StaticAccess.config.get().client.signTextRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
                break;
            case "slider.itemFrameRenderDistance" :
                StaticAccess.config.get().client.itemFrameRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
                break;
            case "slider.playerNameRenderDistance" :
                StaticAccess.config.get().client.playerNameRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
                break;
            case "slider.enemyNameRenderDistance" :
                StaticAccess.config.get().client.enemyNameRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
                break;
            case "slider.animalNameRenderDistance" :
                StaticAccess.config.get().client.animalNameRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
                break;
            case "checkbox.world_compass_widget" :
                StaticAccess.config.get().client.displayWorldCompassWidget = (boolean) event.getNewValue();
                break;
            case "checkbox.location_widget" :
                StaticAccess.config.get().client.displayLocationWidget = (boolean) event.getNewValue();
                break;
            case "checkbox.numeric_hud_values" :
                StaticAccess.config.get().client.displayNumericHUDValues = (boolean) event.getNewValue();
                break;
            case "checkbox.display_names" :
                StaticAccess.config.get().client.displayNames = (boolean) event.getNewValue();
                break;
            case "checkbox.display_healthbars" :
                StaticAccess.config.get().client.displayHealthbars = (boolean) event.getNewValue();
                break;
            case "checkbox.disable_offhand_torch_placement":
                StaticAccess.config.get().client.disableOffhandTorchPlacement = (boolean) event.getNewValue();
                break;
        }

        saveAndLoad();
    }


    private static class OptionsConverter extends Converter<Float, OptionsConverter.Options> {
        @Override
        protected Options doForward(Float val) {
            final int i = Math.round(val * (Options.values().length - 1));
            return Arrays.stream(Options.values())
                    .filter(o -> o.index == i)
                    .findFirst()
                    .orElse(Options.DEFAULT);
        }

        @Override
        protected Float doBackward(Options option) {
            if (Options.values().length == 0){
                return 0f;
            } else {
                return (float) option.index / (Options.values().length - 1);
            }
        }

        protected enum Options {
            DEFAULT("Disabled", 0, 0),
            V4("4", 1, 4),
            V8("8", 2, 8),
            V16("16", 3, 16),
            V32("32", 4, 32),
            V64("64", 5, 64),
            V128("128", 6, 128);

            public final String name;
            public final int index;
            public final int value;

            Options(String name, int index, int value) {
                this.name = name;
                this.index = index;
                this.value = value;
            }

            @Override
            public String toString() {
                return this.name;
            }
        }
    }
}
