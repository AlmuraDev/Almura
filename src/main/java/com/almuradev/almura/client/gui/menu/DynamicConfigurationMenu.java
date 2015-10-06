/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIAnimatedBackground;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.util.Colors;
import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;

import java.io.IOException;
import java.util.Arrays;

public class DynamicConfigurationMenu extends SimpleGui {

    private UICheckBox residenceHudCheckBox, animalHeatCheckBox, almuraDebugGuiCheckBox, debugModeCheckBox, debugLanguagesCheckBox,
            debugPacksCheckBox, debugMappingsCheckBox, debugRecipesCheckBox, chatNotificationCheckBox, optimizedLightingCheckbox;

    public DynamicConfigurationMenu(SimpleGui parent) {
        super(parent);
    }

    @Override
    public void construct() {
        guiscreenBackground = true;
        // Create the form
        final UIForm form = new UIForm(this, 300, 225, "Configuration");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        final int padding = 4;

        final UILabel hudTypeLabel = new UILabel(this, Colors.WHITE + "HUD:");
        hudTypeLabel.setPosition(padding, padding * 2, Anchor.LEFT | Anchor.TOP);

        final UISelect<String> hudTypeSelect = new UISelect<>(this, 50, Arrays.asList("Vanilla", "Almura", "LESS"));
        hudTypeSelect.setPosition(getPaddedX(hudTypeLabel, padding), hudTypeLabel.getY()-2, Anchor.LEFT | Anchor.TOP);
        switch (Configuration.HUD_TYPE.toLowerCase()) {
            case "almura":
                hudTypeSelect.select("Almura");
                break;
            case "less":
                hudTypeSelect.select("LESS");
                break;
            default:
                hudTypeSelect.select("Vanilla");
                break;
        }
        hudTypeSelect.setName("select.hud");
        hudTypeSelect.register(this);

        residenceHudCheckBox = new UICheckBox(this, Colors.WHITE + "Residence HUD");
        residenceHudCheckBox.setPosition(padding, getPaddedY(hudTypeLabel, padding), Anchor.LEFT | Anchor.TOP);
        residenceHudCheckBox.setChecked(Configuration.DISPLAY_RESIDENCE_HUD);
        residenceHudCheckBox.setName("checkbox.residence_hud");
        residenceHudCheckBox.register(this);

        animalHeatCheckBox = new UICheckBox(this, Colors.WHITE + "Show Animal Names with Status");
        animalHeatCheckBox.setPosition(padding, getPaddedY(residenceHudCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        animalHeatCheckBox.setChecked(Configuration.DISPLAY_ANIMAL_HEAT);
        animalHeatCheckBox.setName("checkbox.animalHeat");
        animalHeatCheckBox.register(this);

        final UILabel chestRenderDistance = new UILabel(this, Colors.WHITE + "Chest Distance:");
        chestRenderDistance.setPosition(-55, padding * 2, Anchor.RIGHT | Anchor.TOP);

        final UILabel signRenderDistance = new UILabel(this, Colors.WHITE + "Sign Distance:");
        signRenderDistance.setPosition(-55, hudTypeLabel.getY() + (padding * 4), Anchor.RIGHT | Anchor.TOP);

        final UILabel itemFrameRenderDistance = new UILabel(this, Colors.WHITE + "Item Frame Distance:");
        itemFrameRenderDistance.setPosition(-55, signRenderDistance.getY() + (padding * 4), Anchor.RIGHT | Anchor.TOP);

        // Chest Render Distance
        final UISelect<Integer> chestDistanceDownMenu = new UISelect<>(this, 30, Arrays.asList(16, 32, 64));
        chestDistanceDownMenu.setPosition(-15, padding * 2 - 2, Anchor.TOP | Anchor.RIGHT);
        chestDistanceDownMenu.setMaxExpandedWidth(30);
        if (Configuration.DISTANCE_RENDER_CHEST == 16) {
            chestDistanceDownMenu.select(16);
        } else if (Configuration.DISTANCE_RENDER_CHEST == 32) {
            chestDistanceDownMenu.select(32);
        } else if (Configuration.DISTANCE_RENDER_CHEST == 64) {
            chestDistanceDownMenu.select(64);
        }
        chestDistanceDownMenu.setName("select.chest");
        chestDistanceDownMenu.register(this);

        // Sign Render Distance
        final UISelect<Integer> signDistanceDownMenu = new UISelect<>(this, 30, Arrays.asList(16, 32, 64));
        signDistanceDownMenu.setPosition(-15, chestDistanceDownMenu.getY() + (padding * 4 - 2), Anchor.TOP | Anchor.RIGHT);
        signDistanceDownMenu.setMaxExpandedWidth(30);
        if (Configuration.DISTANCE_RENDER_SIGN == 16) {
            signDistanceDownMenu.select(16);
        } else if (Configuration.DISTANCE_RENDER_SIGN == 32) {
            signDistanceDownMenu.select(32);
        } else if (Configuration.DISTANCE_RENDER_SIGN == 64) {
            signDistanceDownMenu.select(64);
        }
        signDistanceDownMenu.setName("select.sign");
        signDistanceDownMenu.register(this);

        // Sign Render Distance
        final UISelect<Integer> itemFrameDistanceDownMenu = new UISelect<>(this, 30, Arrays.asList(16, 32, 64));
        itemFrameDistanceDownMenu.setPosition(-15, signDistanceDownMenu.getY() + (padding * 4 - 2), Anchor.TOP | Anchor.RIGHT);
        itemFrameDistanceDownMenu.setMaxExpandedWidth(30);
        if (Configuration.DISTANCE_RENDER_ITEM_FRAME == 16) {
            itemFrameDistanceDownMenu.select(16);
        } else if (Configuration.DISTANCE_RENDER_ITEM_FRAME == 32) {
            itemFrameDistanceDownMenu.select(32);
        } else if (Configuration.DISTANCE_RENDER_ITEM_FRAME == 64) {
            itemFrameDistanceDownMenu.select(64);
        }
        itemFrameDistanceDownMenu.setName("select.itemFrame");
        itemFrameDistanceDownMenu.register(this);

        // Create the almura GUI checkbox
        almuraDebugGuiCheckBox = new UICheckBox(this, Colors.WHITE + "Enhanced F3 Debug Menu");
        almuraDebugGuiCheckBox.setPosition(padding, getPaddedY(animalHeatCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        almuraDebugGuiCheckBox.setChecked(Configuration.DISPLAY_ENHANCED_DEBUG);
        almuraDebugGuiCheckBox.setName("checkbox.enhanced_debug");
        almuraDebugGuiCheckBox.register(this);

        // Chat Notifications Checkbox
        chatNotificationCheckBox = new UICheckBox(this, Colors.WHITE + "Chat Notifications");
        chatNotificationCheckBox.setPosition(padding, getPaddedY(almuraDebugGuiCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        chatNotificationCheckBox.setChecked(Configuration.CHAT_NOTIFICATIONS);
        chatNotificationCheckBox.setName("checkbox.chat_notification");

        // Optimized Lighting Checkbox
        optimizedLightingCheckbox = new UICheckBox(this, Colors.WHITE + "Optimized Lighting");
        optimizedLightingCheckbox.setPosition(padding, getPaddedY(chatNotificationCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        optimizedLightingCheckbox.setChecked(Configuration.USE_OPTIMIZED_LIGHTING);
        optimizedLightingCheckbox.setName("checkbox.optimized_lighting");

        // Create the debug mode checkbox
        debugModeCheckBox = new UICheckBox(this, Colors.WHITE + "Debug Mode (All)");
        debugModeCheckBox.setPosition(padding, getPaddedY(chatNotificationCheckBox, padding + 40), Anchor.LEFT | Anchor.TOP);
        debugModeCheckBox.setChecked(Configuration.DEBUG_ALL);
        debugModeCheckBox.setName("checkbox.debug_mode");

        // Create the debug languages checkbox
        debugLanguagesCheckBox = new UICheckBox(this, Colors.WHITE + "Debug Languages");
        debugLanguagesCheckBox.setPosition(padding, getPaddedY(debugModeCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        debugLanguagesCheckBox.setChecked(Configuration.DEBUG_LANGUAGES);
        debugLanguagesCheckBox.setName("checkbox.debug_languages");

        // Create the debug packs checkbox
        debugPacksCheckBox = new UICheckBox(this, Colors.WHITE + "Debug Content Loading");
        debugPacksCheckBox.setPosition(padding, getPaddedY(debugLanguagesCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        debugPacksCheckBox.setChecked(Configuration.DEBUG_PACKS);
        debugPacksCheckBox.setName("checkbox.debug_packs");

        // Create the debug mappings checkbox
        debugMappingsCheckBox = new UICheckBox(this, Colors.WHITE + "Debug Entity / Item Mappings");
        debugMappingsCheckBox.setPosition(padding, getPaddedY(debugPacksCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        debugMappingsCheckBox.setChecked(Configuration.DEBUG_MAPPINGS);
        debugMappingsCheckBox.setName("checkbox.debug_mappings");

        // Create the debug recipes checkbox
        debugRecipesCheckBox = new UICheckBox(this, Colors.WHITE + "Debug Recipe Loading");
        debugRecipesCheckBox.setPosition(padding, getPaddedY(debugMappingsCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        debugRecipesCheckBox.setChecked(Configuration.DEBUG_RECIPES);
        debugRecipesCheckBox.setName("checkbox.debug_recipes");

        // Set Optimized Client Settings
        final UIButton graphicsButton = new UIButton(this, "Load Optimized Settings");
        graphicsButton.setSize(50, 16);
        graphicsButton.setPosition(padding, -padding, Anchor.LEFT | Anchor.BOTTOM);
        graphicsButton.setName("button.graphics");
        graphicsButton.register(this);

        // Create the save button
        final UIButton saveButton = new UIButton(this, "Save");
        saveButton.setSize(50, 16);
        saveButton.setPosition(-padding, -padding, Anchor.RIGHT | Anchor.BOTTOM);
        saveButton.setName("button.save");
        saveButton.register(this);

        // Create the back button
        final UIButton backButton = new UIButton(this, "Back");
        backButton.setSize(50, 16);
        backButton.setPosition(-((padding * 2) + saveButton.getWidth()), -padding, Anchor.RIGHT | Anchor.BOTTOM);
        backButton.setName("button.back");
        backButton.register(this);

        form.getContentContainer().add(signRenderDistance, itemFrameRenderDistance, chestRenderDistance, residenceHudCheckBox, animalHeatCheckBox,
                almuraDebugGuiCheckBox, debugModeCheckBox, debugLanguagesCheckBox, debugPacksCheckBox, debugMappingsCheckBox, debugRecipesCheckBox,
                graphicsButton, backButton, saveButton, chatNotificationCheckBox, itemFrameDistanceDownMenu, signDistanceDownMenu,
                chestDistanceDownMenu, hudTypeLabel, hudTypeSelect, optimizedLightingCheckbox);

        if (this.mc.thePlayer == null) {
            addToScreen(new UIAnimatedBackground(this));
        }
        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.graphics":
                Configuration.setOptimizedConfig();
                mc.displayGuiScreen(new DynamicConfirmMenu(parent.isPresent() ? parent.get() : null, "Changes Saved.", "Please restart your game to"
                        + " apply settings.", "Almura"));
                break;
            case "button.back":
                close();
                break;
            case "button.save":
                try {
                    Configuration.toggleResidenceHUD(residenceHudCheckBox.isChecked());
                    Configuration.toggleAnimalHeat(animalHeatCheckBox.isChecked());
                    Configuration.toggleEnhancedDebug(almuraDebugGuiCheckBox.isChecked());
                    Configuration.toggleDebugMode(debugModeCheckBox.isChecked());
                    Configuration.toggleDebugLanguageMode(debugLanguagesCheckBox.isChecked());
                    Configuration.toggleDebugPacksMode(debugPacksCheckBox.isChecked());
                    Configuration.toggleDebugMappingsMode(debugMappingsCheckBox.isChecked());
                    Configuration.toggleDebugRecipesMode(debugRecipesCheckBox.isChecked());
                    Configuration.setChatNotifications(chatNotificationCheckBox.isChecked());
                    Configuration.toggleOptimizedLighting(optimizedLightingCheckbox.isChecked());
                    Configuration.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                close();
        }
    }

    @Subscribe
    public void onSelection(@SuppressWarnings("rawtypes") UISelect.SelectEvent event) {

        switch (event.getComponent().getName()) {
            case "select.chest":
                Configuration.setChestRenderDistance((Integer) event.getNewValue());
                break;
            case "select.itemFrame":
                Configuration.setItemFrameRenderDistance((Integer) event.getNewValue());
                break;
            case "select.sign":
                Configuration.setSignRenderDistance((Integer) event.getNewValue());
                break;
            case "select.hud":
                Configuration.setHUDType((String) event.getNewValue());
        }
    }
}
