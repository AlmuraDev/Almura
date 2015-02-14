/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraBackgroundGui;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;

import java.util.Arrays;

public class AlmuraConfigurationMenu extends AlmuraBackgroundGui {

    private UICheckBox almuraGuiCheckBox, residenceHudCheckBox, almuraDebugGuiCheckBox, debugModeCheckBox, debugLanguagesCheckBox, debugPacksCheckBox,
            debugMappingsCheckBox, debugRecipesCheckBox;

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added
     *
     * @param parent the {@link AlmuraGui} that we came from
     */
    public AlmuraConfigurationMenu(AlmuraGui parent) {
        super(parent);
        setup();
    }

    @Override
    protected void setup() {
        // Create the form
        final UIForm form = new UIForm(this, 300, 225, "Configuration");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        final int padding = 4;

        // Create the almura GUI checkbox
        almuraGuiCheckBox = new UICheckBox(this, ChatColor.WHITE + "Enhanced In-Game HUD");
        almuraGuiCheckBox.setPosition(padding, padding * 2, Anchor.LEFT | Anchor.TOP);
        almuraGuiCheckBox.setChecked(Configuration.DISPLAY_ENHANCED_GUI);
        almuraGuiCheckBox.setName("checkbox.gui.enhanced_gui");
        almuraGuiCheckBox.register(this);

        residenceHudCheckBox = new UICheckBox(this, ChatColor.WHITE + "Residence HUD");
        residenceHudCheckBox.setPosition(padding, almuraGuiCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        residenceHudCheckBox.setChecked(Configuration.DISPLAY_RESIDENCE_HUD);
        residenceHudCheckBox.setName("checkbox.gui.residence_hud");
        residenceHudCheckBox.register(this);

        final UILabel chestRenderDistance = new UILabel(this, ChatColor.WHITE + "Chest Distance:");
        chestRenderDistance.setPosition(-55, padding * 2, Anchor.RIGHT | Anchor.TOP);

        final UILabel signRenderDistance = new UILabel(this, ChatColor.WHITE + "Sign Distance:");
        signRenderDistance.setPosition(-55, almuraGuiCheckBox.getY() + (padding * 4 + 25), Anchor.RIGHT | Anchor.TOP);

        final UILabel itemFrameRenderDistance = new UILabel(this, ChatColor.WHITE + "Item Frame Distance:");
        itemFrameRenderDistance.setPosition(-55, signRenderDistance.getY() + (padding * 4 + 25), Anchor.RIGHT | Anchor.TOP);

        // Chest Render Distance
        final UISelect chestDistanceDownMenu = new UISelect(this, 30, UISelect.Option.fromList(Arrays.asList("16", "32", "64")));
        chestDistanceDownMenu.setPosition(-15, padding * 2, Anchor.TOP | Anchor.RIGHT);
        chestDistanceDownMenu.setMaxExpandedWidth(30);
        if (Configuration.CHEST_RENDER_DISTANCE == 16) {
            chestDistanceDownMenu.select(0);
        } else if (Configuration.CHEST_RENDER_DISTANCE == 32) {
            chestDistanceDownMenu.select(1);
        } else if (Configuration.CHEST_RENDER_DISTANCE == 64) {
            chestDistanceDownMenu.select(2);
        }
        chestDistanceDownMenu.setName("select.chest");
        chestDistanceDownMenu.register(this);

        // Sign Render Distance
        final UISelect signDistanceDownMenu = new UISelect(this, 30, UISelect.Option.fromList(Arrays.asList("16", "32", "64")));
        signDistanceDownMenu.setPosition(-15, chestDistanceDownMenu.getY() + (padding * 4 + 25), Anchor.TOP | Anchor.RIGHT);
        signDistanceDownMenu.setMaxExpandedWidth(30);
        if (Configuration.SIGN_RENDER_DISTANCE == 16) {
            signDistanceDownMenu.select(0);
        } else if (Configuration.SIGN_RENDER_DISTANCE == 32) {
            signDistanceDownMenu.select(1);
        } else if (Configuration.SIGN_RENDER_DISTANCE == 64) {
            signDistanceDownMenu.select(2);
        }
        signDistanceDownMenu.setName("select.sign");
        signDistanceDownMenu.register(this);

        // Sign Render Distance
        final UISelect itemFrameDistanceDownMenu = new UISelect(this, 30, UISelect.Option.fromList(Arrays.asList("16", "32", "64")));
        itemFrameDistanceDownMenu.setPosition(-15, signDistanceDownMenu.getY() + (padding * 4 + 25), Anchor.TOP | Anchor.RIGHT);
        itemFrameDistanceDownMenu.setMaxExpandedWidth(30);
        if (Configuration.ITEM_FRAME_RENDER_DISTANCE == 16) {
            itemFrameDistanceDownMenu.select(0);
        } else if (Configuration.SIGN_RENDER_DISTANCE == 32) {
            itemFrameDistanceDownMenu.select(1);
        } else if (Configuration.SIGN_RENDER_DISTANCE == 64) {
            itemFrameDistanceDownMenu.select(2);
        }
        itemFrameDistanceDownMenu.setName("select.itemFrame");
        itemFrameDistanceDownMenu.register(this);

        // Create the almura GUI checkbox
        almuraDebugGuiCheckBox = new UICheckBox(this, ChatColor.WHITE + "Enhanced F3 Debug Menu");
        almuraDebugGuiCheckBox.setPosition(padding, almuraGuiCheckBox.getY() + (padding * 4 + 15), Anchor.LEFT | Anchor.TOP);
        almuraDebugGuiCheckBox.setChecked(Configuration.DISPLAY_ENHANCED_DEBUG);
        almuraDebugGuiCheckBox.setName("checkbox.gui.enhanced_debug");
        almuraDebugGuiCheckBox.register(this);

        // Create the debug mode checkbox
        debugModeCheckBox = new UICheckBox(this, ChatColor.WHITE + "Debug Mode (All)");
        debugModeCheckBox.setPosition(padding, almuraDebugGuiCheckBox.getY() + (padding * 4) + 20, Anchor.LEFT | Anchor.TOP);
        debugModeCheckBox.setChecked(Configuration.DEBUG_MODE);
        debugModeCheckBox.setName("checkbox.gui.debug_mode");

        // Create the debug languages checkbox
        debugLanguagesCheckBox = new UICheckBox(this, ChatColor.WHITE + "Debug Languages");
        debugLanguagesCheckBox.setPosition(padding, debugModeCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        debugLanguagesCheckBox.setChecked(Configuration.DEBUG_LANGUAGES_MODE);
        debugLanguagesCheckBox.setName("checkbox.gui.debug_languages");

        // Create the debug packs checkbox
        debugPacksCheckBox = new UICheckBox(this, ChatColor.WHITE + "Debug Content Loading");
        debugPacksCheckBox.setPosition(padding, debugLanguagesCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        debugPacksCheckBox.setChecked(Configuration.DEBUG_PACKS_MODE);
        debugPacksCheckBox.setName("checkbox.gui.debug_packs");

        // Create the debug mappings checkbox
        debugMappingsCheckBox = new UICheckBox(this, ChatColor.WHITE + "Debug Entity / Item Mappings");
        debugMappingsCheckBox.setPosition(padding, debugPacksCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        debugMappingsCheckBox.setChecked(Configuration.DEBUG_MAPPINGS_MODE);
        debugMappingsCheckBox.setName("checkbox.gui.debug_mappings");

        // Create the debug recipes checkbox
        debugRecipesCheckBox = new UICheckBox(this, ChatColor.WHITE + "Debug Recipe Loading");
        debugRecipesCheckBox.setPosition(padding, debugMappingsCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        debugRecipesCheckBox.setChecked(Configuration.DEBUG_RECIPES_MODE);
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
                signDistanceDownMenu,
                chestDistanceDownMenu, itemFrameDistanceDownMenu);

        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.graphics":
                setOptimizedConfig();
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
                    Configuration.save();
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                }
                close();
        }
    }

    @Subscribe
    public void onSelection(UISelect.SelectEvent event) {
        String type = event.getComponent().getName().toLowerCase();
        if (type.equalsIgnoreCase("select.sign")) {
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
        }

        if (type.equalsIgnoreCase("select.chest")) {
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
        }

        if (type.equalsIgnoreCase("select.itemFrame")) {
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
        }
    }

    public void setOptimizedConfig() {
        mc.gameSettings.ambientOcclusion = 0;
        mc.gameSettings.mipmapLevels = 0;
        mc.gameSettings.guiScale = 3;
        mc.gameSettings.advancedOpengl = true;
        mc.gameSettings.anisotropicFiltering = 0;
        mc.gameSettings.limitFramerate = 120;
        mc.gameSettings.enableVsync = false;
        mc.gameSettings.clouds = false;
        mc.gameSettings.snooperEnabled = false;
        mc.gameSettings.renderDistanceChunks = 12;
        mc.gameSettings.viewBobbing = false;
        mc.gameSettings.saveOptions();

        mc.displayGuiScreen(new AlmuraConfirmMenu(parent.isPresent() ? parent.get() : null, "Changes Saved.", "Please restart your game to apply settings.", "Almura"));
    }
}
