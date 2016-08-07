/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIAnimatedBackground;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.configuration.ConfigurationAdapter;
import com.almuradev.almura.configuration.category.ClientCategory;
import com.almuradev.almura.configuration.category.DebugCategory;
import com.almuradev.almura.configuration.type.ClientConfiguration;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class DynamicConfigurationMenu extends SimpleGui {

    private UICheckBox animalHeatCheckBox, almuraDebugGuiCheckBox, debugModeCheckBox, debugLanguagesCheckBox,
            debugPacksCheckBox, debugMappingsCheckBox, debugRecipesCheckBox, chatNotificationCheckBox, optimizedLightingCheckbox;

    public DynamicConfigurationMenu(SimpleGui parent) {
        super(parent);
    }

    @Override
    public void construct() {
        guiscreenBackground = true;
        final ClientConfiguration configuration = ((ClientProxy) Almura.proxy).getConfigAdapter().getConfig();
        final ClientCategory clientCategory = configuration.client;
        final DebugCategory debugCategory = configuration.debug;

        // Create the form
        final UIForm form = new UIForm(this, 300, 225, "Configuration");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        final int padding = 4;

        final UILabel hudTypeLabel = new UILabel(this, TextFormatting.WHITE + "HUD:");
        hudTypeLabel.setPosition(padding, padding * 2, Anchor.LEFT | Anchor.TOP);

        final UISelect<String> hudTypeSelect = new UISelect<>(this, 53, Arrays.asList("Default", "Almura", "Minimal"));
        hudTypeSelect.setPosition(getPaddedX(hudTypeLabel, padding), hudTypeLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        hudTypeSelect.select(WordUtils.capitalize(clientCategory.hud));
        hudTypeSelect.setName("select.hud");
        hudTypeSelect.register(this);

        animalHeatCheckBox = new UICheckBox(this, TextFormatting.WHITE + "Show Animal Names with Status");
        animalHeatCheckBox.setPosition(padding, getPaddedY(hudTypeLabel, padding), Anchor.LEFT | Anchor.TOP);
        animalHeatCheckBox.setChecked(clientCategory.displayAnimalHeatStatus);
        animalHeatCheckBox.setName("checkbox.animalHeat");
        animalHeatCheckBox.register(this);

        final UILabel chestRenderDistance = new UILabel(this, TextFormatting.WHITE + "Chest Distance:");
        chestRenderDistance.setPosition(-55, padding * 2, Anchor.RIGHT | Anchor.TOP);

        final UILabel signRenderDistance = new UILabel(this, TextFormatting.WHITE + "Sign Distance:");
        signRenderDistance.setPosition(-55, hudTypeLabel.getY() + (padding * 4), Anchor.RIGHT | Anchor.TOP);

        final UILabel itemFrameRenderDistance = new UILabel(this, TextFormatting.WHITE + "Item Frame Distance:");
        itemFrameRenderDistance.setPosition(-55, signRenderDistance.getY() + (padding * 4), Anchor.RIGHT | Anchor.TOP);

        // Chest Render Distance
        final UISelect<Integer> chestDistanceDownMenu = new UISelect<>(this, 30, Arrays.asList(16, 32, 64));
        chestDistanceDownMenu.setPosition(-15, padding * 2 - 2, Anchor.TOP | Anchor.RIGHT);
        chestDistanceDownMenu.setMaxExpandedWidth(30);
        chestDistanceDownMenu.select(clientCategory.chestRenderDistance);
        chestDistanceDownMenu.setName("select.chest");
        chestDistanceDownMenu.register(this);

        // Sign Render Distance
        final UISelect<Integer> signDistanceDownMenu = new UISelect<>(this, 30, Arrays.asList(16, 32, 64));
        signDistanceDownMenu.setPosition(-15, chestDistanceDownMenu.getY() + (padding * 4 - 2), Anchor.TOP | Anchor.RIGHT);
        signDistanceDownMenu.setMaxExpandedWidth(30);
        signDistanceDownMenu.select(clientCategory.signTextRenderDistance);
        signDistanceDownMenu.setName("select.sign");
        signDistanceDownMenu.register(this);

        // Sign Render Distance
        final UISelect<Integer> itemFrameDistanceDownMenu = new UISelect<>(this, 30, Arrays.asList(16, 32, 64));
        itemFrameDistanceDownMenu.setPosition(-15, signDistanceDownMenu.getY() + (padding * 4 - 2), Anchor.TOP | Anchor.RIGHT);
        itemFrameDistanceDownMenu.setMaxExpandedWidth(30);
        itemFrameDistanceDownMenu.select(clientCategory.itemFrameRenderDistance);
        itemFrameDistanceDownMenu.setName("select.itemFrame");
        itemFrameDistanceDownMenu.register(this);

        // Create the almura GUI checkbox
        almuraDebugGuiCheckBox = new UICheckBox(this, TextFormatting.WHITE + "Enhanced F3 Debug Menu");
        almuraDebugGuiCheckBox.setPosition(padding, getPaddedY(animalHeatCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        almuraDebugGuiCheckBox.setChecked(clientCategory.enhancedDebug);
        almuraDebugGuiCheckBox.setName("checkbox.enhanced_debug");
        almuraDebugGuiCheckBox.register(this);

        // Chat Notifications Checkbox
        chatNotificationCheckBox = new UICheckBox(this, TextFormatting.WHITE + "Chat Notifications");
        chatNotificationCheckBox.setPosition(padding, getPaddedY(almuraDebugGuiCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        chatNotificationCheckBox.setChecked(clientCategory.chatNotifications);
        chatNotificationCheckBox.setName("checkbox.chat_notification");

        // Optimized Lighting Checkbox
        optimizedLightingCheckbox = new UICheckBox(this, TextFormatting.WHITE + "Optimized Lighting");
        optimizedLightingCheckbox.setPosition(padding, getPaddedY(chatNotificationCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        optimizedLightingCheckbox.setChecked(clientCategory.optimizedLighting);
        optimizedLightingCheckbox.setName("checkbox.optimized_lighting");

        // Create the debug mode checkbox
        debugModeCheckBox = new UICheckBox(this, TextFormatting.WHITE + "Debug Mode (All)");
        debugModeCheckBox.setPosition(padding, getPaddedY(chatNotificationCheckBox, padding + 40), Anchor.LEFT | Anchor.TOP);
        debugModeCheckBox.setChecked(debugCategory.all);
        debugModeCheckBox.setName("checkbox.debug_mode");

        // Create the debug languages checkbox
        debugLanguagesCheckBox = new UICheckBox(this, TextFormatting.WHITE + "Debug Languages");
        debugLanguagesCheckBox.setPosition(padding, getPaddedY(debugModeCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        debugLanguagesCheckBox.setChecked(debugCategory.languages);
        debugLanguagesCheckBox.setName("checkbox.debug_languages");

        // Create the debug packs checkbox
        debugPacksCheckBox = new UICheckBox(this, TextFormatting.WHITE + "Debug Content Loading");
        debugPacksCheckBox.setPosition(padding, getPaddedY(debugLanguagesCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        debugPacksCheckBox.setChecked(debugCategory.packs);
        debugPacksCheckBox.setName("checkbox.debug_packs");

        // Create the debug mappings checkbox
        debugMappingsCheckBox = new UICheckBox(this, TextFormatting.WHITE + "Debug Entity / Item Mappings");
        debugMappingsCheckBox.setPosition(padding, getPaddedY(debugPacksCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        debugMappingsCheckBox.setChecked(debugCategory.mappings);
        debugMappingsCheckBox.setName("checkbox.debug_mappings");

        // Create the debug recipes checkbox
        debugRecipesCheckBox = new UICheckBox(this, TextFormatting.WHITE + "Debug Recipe Loading");
        debugRecipesCheckBox.setPosition(padding, getPaddedY(debugMappingsCheckBox, padding), Anchor.LEFT | Anchor.TOP);
        debugRecipesCheckBox.setChecked(debugCategory.recipes);
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

        form.getContentContainer().add(signRenderDistance, itemFrameRenderDistance, chestRenderDistance, animalHeatCheckBox,
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
        final ConfigurationAdapter<ClientConfiguration> configAdapter = ((ClientProxy) Almura.proxy).getConfigAdapter();
        final ClientConfiguration configuration = configAdapter.getConfig();
        switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH)) {
            case "button.graphics":
                configuration.client.optimizeGame();
                new DynamicConfirmMenu(parent.isPresent() ? parent.get() : null, "Changes Saved.", "Please restart your game to"
                        + " apply settings.", "Almura").display();
                break;
            case "button.back":
                close();
                break;
            case "button.save":
                try {
                    final ClientCategory clientCategory = configuration.client;
                    clientCategory.displayAnimalHeatStatus = animalHeatCheckBox.isChecked();
                    clientCategory.enhancedDebug = almuraDebugGuiCheckBox.isChecked();
                    clientCategory.chatNotifications = chatNotificationCheckBox.isChecked();
                    clientCategory.optimizedLighting = optimizedLightingCheckbox.isChecked();

                    final DebugCategory debugCategory = configuration.debug;
                    debugCategory.all = debugModeCheckBox.isChecked();
                    debugCategory.languages = debugLanguagesCheckBox.isChecked();
                    debugCategory.packs = debugPacksCheckBox.isChecked();
                    debugCategory.mappings = debugMappingsCheckBox.isChecked();
                    debugCategory.recipes = debugRecipesCheckBox.isChecked();
                    Almura.proxy.getConfigAdapter().save();
                } catch (IOException | ObjectMappingException e) {
                    throw new RuntimeException("Failed to save config for class [" + configAdapter.getConfigClass() + "] from [" + configAdapter.
                            getConfigPath() + "]!", e);
                }
                close();
        }
    }

    @Subscribe
    public void onSelection(@SuppressWarnings("rawtypes") UISelect.SelectEvent event) {
        final ClientCategory clientCategory = ((ClientProxy) Almura.proxy).getConfigAdapter().getConfig().client;
        final Object newValue = event.getNewValue();

        switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH)) {
            case "select.chest":
                clientCategory.chestRenderDistance = (Integer) newValue;
                break;
            case "select.itemFrame":
                clientCategory.itemFrameRenderDistance = (Integer) newValue;
                break;
            case "select.sign":
                clientCategory.signTextRenderDistance = (Integer) newValue;
                break;
            case "select.hud":
                clientCategory.hud = newValue.toString();
        }
    }
}
