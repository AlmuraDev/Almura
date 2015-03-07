/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.Configuration;
import com.almuradev.almurasdk.client.gui.SimpleGui;
import com.almuradev.almurasdk.client.gui.components.UIForm;
import com.almuradev.almurasdk.util.Colors;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;

import java.io.IOException;
import java.util.Arrays;

public class AlmuraConfigurationMenu extends SimpleGui {

    private UICheckBox almuraGuiCheckBox, residenceHudCheckBox, almuraDebugGuiCheckBox, debugModeCheckBox, debugLanguagesCheckBox, debugPacksCheckBox,
            debugMappingsCheckBox, debugRecipesCheckBox, chatNotificationCheckBox;

    public AlmuraConfigurationMenu(SimpleGui parent) {
        super(parent);
        buildGui();
    }

    @Override
    protected void buildGui() {
        // Create the form
        final UIForm form = new UIForm(this, 300, 225, "Configuration");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        final int padding = 4;

        // Create the almura GUI checkbox
        almuraGuiCheckBox = new UICheckBox(this, Colors.WHITE + "Enhanced In-Game HUD");
        almuraGuiCheckBox.setPosition(padding, padding * 2, Anchor.LEFT | Anchor.TOP);
        almuraGuiCheckBox.setChecked(Configuration.DISPLAY_ENHANCED_GUI);
        almuraGuiCheckBox.setName("checkbox.gui.enhanced_gui");
        almuraGuiCheckBox.register(this);

        residenceHudCheckBox = new UICheckBox(this, Colors.WHITE + "Residence HUD");
        residenceHudCheckBox.setPosition(padding, almuraGuiCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        residenceHudCheckBox.setChecked(Configuration.DISPLAY_RESIDENCE_HUD);
        residenceHudCheckBox.setName("checkbox.gui.residence_hud");
        residenceHudCheckBox.register(this);

        final UILabel chestRenderDistance = new UILabel(this, Colors.WHITE + "Chest Distance:");
        chestRenderDistance.setPosition(-55, padding * 2, Anchor.RIGHT | Anchor.TOP);

        final UILabel signRenderDistance = new UILabel(this, Colors.WHITE + "Sign Distance:");
        signRenderDistance.setPosition(-55, almuraGuiCheckBox.getY() + (padding * 4), Anchor.RIGHT | Anchor.TOP);

        final UILabel itemFrameRenderDistance = new UILabel(this, Colors.WHITE + "Item Frame Distance:");
        itemFrameRenderDistance.setPosition(-55, signRenderDistance.getY() + (padding * 4), Anchor.RIGHT | Anchor.TOP);

        // Chest Render Distance
        final UISelect chestDistanceDownMenu = new UISelect(this, 30, UISelect.Option.fromList(Arrays.asList("16", "32", "64")));
        chestDistanceDownMenu.setPosition(-15, padding * 2, Anchor.TOP | Anchor.RIGHT);
        chestDistanceDownMenu.setMaxExpandedWidth(30);
        if (Configuration.DISTANCE_RENDER_CHEST == 16) {
            chestDistanceDownMenu.select(0);
        } else if (Configuration.DISTANCE_RENDER_CHEST == 32) {
            chestDistanceDownMenu.select(1);
        } else if (Configuration.DISTANCE_RENDER_CHEST == 64) {
            chestDistanceDownMenu.select(2);
        }
        chestDistanceDownMenu.setName("select.chest");
        chestDistanceDownMenu.register(this);

        // Sign Render Distance
        final UISelect signDistanceDownMenu = new UISelect(this, 30, UISelect.Option.fromList(Arrays.asList("16", "32", "64")));
        signDistanceDownMenu.setPosition(-15, chestDistanceDownMenu.getY() + (padding * 4), Anchor.TOP | Anchor.RIGHT);
        signDistanceDownMenu.setMaxExpandedWidth(30);
        if (Configuration.DISTANCE_RENDER_ITEM_FRAME == 16) {
            signDistanceDownMenu.select(0);
        } else if (Configuration.DISTANCE_RENDER_ITEM_FRAME == 32) {
            signDistanceDownMenu.select(1);
        } else if (Configuration.DISTANCE_RENDER_ITEM_FRAME == 64) {
            signDistanceDownMenu.select(2);
        }
        signDistanceDownMenu.setName("select.sign");
        signDistanceDownMenu.register(this);

        // Sign Render Distance
        final UISelect itemFrameDistanceDownMenu = new UISelect(this, 30, UISelect.Option.fromList(Arrays.asList("16", "32", "64")));
        itemFrameDistanceDownMenu.setPosition(-15, signDistanceDownMenu.getY() + (padding * 4), Anchor.TOP | Anchor.RIGHT);
        itemFrameDistanceDownMenu.setMaxExpandedWidth(30);
        if (Configuration.DISTANCE_RENDER_SIGN == 16) {
            itemFrameDistanceDownMenu.select(0);
        } else if (Configuration.DISTANCE_RENDER_ITEM_FRAME == 32) {
            itemFrameDistanceDownMenu.select(1);
        } else if (Configuration.DISTANCE_RENDER_ITEM_FRAME == 64) {
            itemFrameDistanceDownMenu.select(2);
        }
        itemFrameDistanceDownMenu.setName("select.itemFrame");
        itemFrameDistanceDownMenu.register(this);

        // Create the almura GUI checkbox
        almuraDebugGuiCheckBox = new UICheckBox(this, Colors.WHITE + "Enhanced F3 Debug Menu");
        almuraDebugGuiCheckBox.setPosition(padding, almuraGuiCheckBox.getY() + (padding * 4 + 15), Anchor.LEFT | Anchor.TOP);
        almuraDebugGuiCheckBox.setChecked(Configuration.DISPLAY_ENHANCED_DEBUG);
        almuraDebugGuiCheckBox.setName("checkbox.gui.enhanced_debug");
        almuraDebugGuiCheckBox.register(this);

        // Chat Notifications Checkbox
        chatNotificationCheckBox = new UICheckBox(this, Colors.WHITE + "Chat Notifications");
        chatNotificationCheckBox.setPosition(padding, almuraDebugGuiCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        chatNotificationCheckBox.setChecked(Configuration.CHAT_NOTIFICATIONS);
        chatNotificationCheckBox.setName("checkbox.gui.chat_notification");

        // Create the debug mode checkbox
        debugModeCheckBox = new UICheckBox(this, Colors.WHITE + "Debug Mode (All)");
        debugModeCheckBox.setPosition(padding, chatNotificationCheckBox.getY() + (padding * 4) + 40, Anchor.LEFT | Anchor.TOP);
        debugModeCheckBox.setChecked(Configuration.DEBUG_ALL);
        debugModeCheckBox.setName("checkbox.gui.debug_mode");

        // Create the debug languages checkbox
        debugLanguagesCheckBox = new UICheckBox(this, Colors.WHITE + "Debug Languages");
        debugLanguagesCheckBox.setPosition(padding, debugModeCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        debugLanguagesCheckBox.setChecked(Configuration.DEBUG_LANGUAGES);
        debugLanguagesCheckBox.setName("checkbox.gui.debug_languages");

        // Create the debug packs checkbox
        debugPacksCheckBox = new UICheckBox(this, Colors.WHITE + "Debug Content Loading");
        debugPacksCheckBox.setPosition(padding, debugLanguagesCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        debugPacksCheckBox.setChecked(Configuration.DEBUG_PACKS);
        debugPacksCheckBox.setName("checkbox.gui.debug_packs");

        // Create the debug mappings checkbox
        debugMappingsCheckBox = new UICheckBox(this, Colors.WHITE + "Debug Entity / Item Mappings");
        debugMappingsCheckBox.setPosition(padding, debugPacksCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        debugMappingsCheckBox.setChecked(Configuration.DEBUG_MAPPINGS);
        debugMappingsCheckBox.setName("checkbox.gui.debug_mappings");

        // Create the debug recipes checkbox
        debugRecipesCheckBox = new UICheckBox(this, Colors.WHITE + "Debug Recipe Loading");
        debugRecipesCheckBox.setPosition(padding, debugMappingsCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        debugRecipesCheckBox.setChecked(Configuration.DEBUG_RECIPES);
        debugRecipesCheckBox.setName("checkbox.gui.debug_recipes");

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

        // Create the cancel button
        final UIButton cancelButton = new UIButton(this, "Cancel");
        cancelButton.setSize(50, 16);
        cancelButton.setPosition(-((padding * 2) + saveButton.getWidth()), -padding, Anchor.RIGHT | Anchor.BOTTOM);
        cancelButton.setName("button.cancel");
        cancelButton.register(this);

        form.getContentContainer().add(signRenderDistance, itemFrameRenderDistance, chestRenderDistance, almuraGuiCheckBox,
                residenceHudCheckBox,
                almuraDebugGuiCheckBox, debugModeCheckBox, debugLanguagesCheckBox,
                debugPacksCheckBox, debugMappingsCheckBox, debugRecipesCheckBox, graphicsButton, cancelButton, saveButton,
                chatNotificationCheckBox, itemFrameDistanceDownMenu, signDistanceDownMenu, chestDistanceDownMenu);

        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.graphics":
                Configuration.setOptimizedConfig();
                mc.displayGuiScreen(new AlmuraConfirmMenu(parent.isPresent() ? parent.get() : null, "Changes Saved.",
                        "Please restart your game to apply settings.", "Almura"));
                break;
            case "button.cancel":
                close();
                break;
            case "button.save":
                try {
                    Configuration.toggleEnhancedGUI(almuraGuiCheckBox.isChecked());
                    Configuration.toggleResidenceHUD(residenceHudCheckBox.isChecked());
                    Configuration.toggleEnhancedDebug(almuraDebugGuiCheckBox.isChecked());
                    Configuration.toggleDebugMode(debugModeCheckBox.isChecked());
                    Configuration.toggleDebugLanguageMode(debugLanguagesCheckBox.isChecked());
                    Configuration.toggleDebugPacksMode(debugPacksCheckBox.isChecked());
                    Configuration.toggleDebugMappingsMode(debugMappingsCheckBox.isChecked());
                    Configuration.toggleDebugRecipesMode(debugRecipesCheckBox.isChecked());
                    Configuration.setChatNotifications(chatNotificationCheckBox.isChecked());
                    Configuration.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                close();
        }
    }

    @Subscribe
    public void onSelection(UISelect.SelectEvent event) {
        String type = event.getComponent().getName().toLowerCase();
        switch (event.getComponent().getName()) {
            case "select.chest":
                switch (event.getNewValue().getLabel()) {
                    case "16":
                        Configuration.setChestRenderDistance(16);
                        break;
                    case "32":
                        Configuration.setChestRenderDistance(32);
                        break;
                    case "64":
                        Configuration.setChestRenderDistance(64);
                        break;
                }
                break;
            case "select.itemframe":
                switch (event.getNewValue().getLabel()) {
                    case "16":
                        Configuration.setItemFrameRenderDistance(16);
                        break;
                    case "32":
                        Configuration.setItemFrameRenderDistance(32);
                        break;
                    case "64":
                        Configuration.setItemFrameRenderDistance(64);
                        break;
                }
                break;
            case "select.sign":
                switch (event.getNewValue().getLabel()) {
                    case "16":
                        Configuration.setSignRenderDistance(16);
                        break;
                    case "32":
                        Configuration.setSignRenderDistance(32);
                        break;
                    case "64":
                        Configuration.setSignRenderDistance(64);
                        break;
                }
                break;

        }
    }
}
