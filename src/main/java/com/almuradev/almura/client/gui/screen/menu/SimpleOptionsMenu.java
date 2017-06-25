/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import com.almuradev.almura.client.gui.util.builder.UIButtonBuilder;
import com.almuradev.almura.client.gui.util.builder.UISliderBuilder;
import com.almuradev.almura.configuration.type.ClientConfiguration;
import com.google.common.base.Converter;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISlider;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.resources.I18n;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.util.Arrays;

public class SimpleOptionsMenu extends SimpleScreen {

    private static final OptionsConverter OPTIONS_CONVERTER = new OptionsConverter();
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_WIDTH = 150;
    private static final int CONTROL_PADDING = 5;

    private final ClientConfiguration config = (ClientConfiguration) Almura.proxy.getPlatformConfigAdapter().getConfig();

    private UIButton buttonHudType;

    public SimpleOptionsMenu(GuiOptions parentOptions) {
        super(parentOptions);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void construct() {
        this.saveAndLoad();

        final UILabel titleLabel = new UILabel(this, I18n.format("almura.menu.options"));
        titleLabel.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE);
        titleLabel.setPosition(0, 20, Anchor.TOP | Anchor.CENTER);

        this.addToScreen(titleLabel);

        buttonHudType = new UIButtonBuilder(this)
                .text("HUD: " + config.client.hud.substring(0, 1).toUpperCase() + config.client.hud.substring(1))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .position(-(CONTROL_WIDTH / 2 + CONTROL_PADDING), getPaddedY(titleLabel, CONTROL_PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.hudType");

        final UISlider<OptionsConverter.Options> sliderChestDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Chest Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.client.chestRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .position(CONTROL_WIDTH / 2 + CONTROL_PADDING,  getPaddedY(titleLabel, CONTROL_PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("slider.chestRenderDistance");

        final UISlider<OptionsConverter.Options> sliderSignTextDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Sign Text Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.client.signTextRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .position(CONTROL_WIDTH / 2 + CONTROL_PADDING, getPaddedY(sliderChestDistance, CONTROL_PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("slider.signTextRenderDistance");

        final UISlider<OptionsConverter.Options> sliderItemFrameDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Item Frame Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.client.itemFrameRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .position(CONTROL_WIDTH / 2 + CONTROL_PADDING, getPaddedY(sliderSignTextDistance, CONTROL_PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("slider.itemFrameRenderDistance");

        final UIButton buttonDone = new UIButtonBuilder(this)
                .text(I18n.format("gui.done"))
                .size(200, CONTROL_HEIGHT)
                .position(0, -12)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .listener(this)
                .build("button.done");

        addToScreen(buttonHudType, sliderChestDistance, sliderSignTextDistance, sliderItemFrameDistance, buttonDone);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    private void saveAndLoad() {
        try {
            Almura.proxy.getPlatformConfigAdapter().save();
        } catch (IOException | ObjectMappingException e) {
            Almura.instance.logger.error("Unable to save to configuration!", e);
        }

        try {
            Almura.proxy.getPlatformConfigAdapter().load();
        } catch (IOException | ObjectMappingException e) {
            Almura.instance.logger.error("Unable to load from configuration!", e);
        }
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName()) {
            case "button.hudType":
                final boolean isOrigin = config.client.hud.equalsIgnoreCase("origin");
                config.client.hud = isOrigin ? "vanilla" : "origin";
                buttonHudType.setText("HUD: " + (!isOrigin ? "Origin" : "Vanilla"));
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
            case "slider.chestRenderDistance" :
                config.client.chestRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
                break;
            case "slider.signTextRenderDistance" :
                config.client.signTextRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
                break;
            case "slider.itemFrameRenderDistance" :
                config.client.itemFrameRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
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
            DEFAULT("Default", 0, 0),
            V16("16", 1, 16),
            V32("32", 2, 32),
            V64("64", 3, 64),
            V128("128", 4, 128);

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
