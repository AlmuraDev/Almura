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
import com.flowpowered.cerealization.config.ConfigurationException;
import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;

public class AlmuraConfigurationMenu extends AlmuraBackgroundGui {

    private UIBackgroundContainer window;
    private UIButton cancelButton, saveButton;
    private UICheckBox almuraGuiCheckBox, debugModeCheckBox, debugLanguagesCheckBox, debugPacksCheckBox, debugMappingsCheckBox, debugRecipesCheckBox;
    private UILabel titleLabel;

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added

     * @param parent the {@link AlmuraGui} that we came from
     */
    public AlmuraConfigurationMenu(AlmuraGui parent) {
        super(parent);
    }

    @Override
    protected void setup() {
        // Create the window container
        window = new UIBackgroundContainer(this);
        window.setSize(300, 225);
        window.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        window.setColor(Integer.MIN_VALUE);
        window.setBackgroundAlpha(125);

        final int padding = 4;

        // Create the title label
        titleLabel = new UILabel(this, ChatColor.GRAY + "Almura Configuration");
        titleLabel.setPosition(0, padding, Anchor.CENTER | Anchor.TOP);

        // Create the almura GUI checkbox
        almuraGuiCheckBox = new UICheckBox(this, ChatColor.WHITE + "Use enhanced GUI");
        almuraGuiCheckBox.setPosition(padding, titleLabel.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
        almuraGuiCheckBox.setChecked(Configuration.DISPLAY_ENHANCED_GUI);
        almuraGuiCheckBox.setName("checkbox.gui.enhanced_gui");
        almuraGuiCheckBox.register(this);
        
        // Create the debug mode checkbox
        debugModeCheckBox = new UICheckBox(this, ChatColor.WHITE + "Debug Mode (All)");
        debugModeCheckBox.setPosition(padding, almuraGuiCheckBox.getY() + (padding * 4) + 20, Anchor.LEFT | Anchor.TOP);
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
        
        // Create the save button
        saveButton = new UIButton(this, "Save");
        saveButton.setSize(50, 16);
        saveButton.setPosition(-padding, -padding, Anchor.RIGHT | Anchor.BOTTOM);
        saveButton.setName("button.save");
        saveButton.register(this);

        // Create the cancel button
        cancelButton = new UIButton(this, "Cancel");
        cancelButton.setSize(50, 16);
        cancelButton.setPosition(-((padding * 2) + saveButton.getWidth()), -padding, Anchor.RIGHT | Anchor.BOTTOM);
        cancelButton.setName("button.cancel");
        cancelButton.register(this);

        window.add(titleLabel, almuraGuiCheckBox, debugModeCheckBox, debugLanguagesCheckBox, debugPacksCheckBox, debugMappingsCheckBox, debugRecipesCheckBox, cancelButton, saveButton);

        // Allow the window to move
        new UIMoveHandle(this, window);

        addToScreen(window);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.cancel":
                displayParent();
                break;
            case "button.save":
            try {
                Configuration.toggleEnhancedGUI(almuraGuiCheckBox.isChecked());
                Configuration.toggleDebugMode(debugModeCheckBox.isChecked());
                Configuration.toggleDebugLanguageMode(debugLanguagesCheckBox.isChecked());
                Configuration.toggleDebugPacksMode(debugPacksCheckBox.isChecked());
                Configuration.toggleDebugMappingsMode(debugMappingsCheckBox.isChecked());
                Configuration.toggleDebugRecipesMode(debugRecipesCheckBox.isChecked());
                Configuration.save();
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
                displayParent();
        }
    }
}
