/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import java.awt.Color;

import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraBackgroundGui;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;

public class AlmuraConfigurationMenu extends AlmuraBackgroundGui {

    private UIBackgroundContainer window, uiTitleBar;
    private UIButton graphicsButton, xButton, cancelButton, saveButton;
    private UICheckBox almuraGuiCheckBox, almuraDebugGuiCheckBox, debugModeCheckBox, debugLanguagesCheckBox, debugPacksCheckBox, debugMappingsCheckBox, debugRecipesCheckBox;
    private UILabel titleLabel;
    private AlmuraGui parent;

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added

     * @param parent the {@link AlmuraGui} that we came from
     */
    public AlmuraConfigurationMenu(AlmuraGui parent) {
        super(parent);
        this.parent = parent;
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

        // Create the title & Window layout 
        titleLabel = new UILabel(this, ChatColor.WHITE + "Almura Configuration");
        titleLabel.setPosition(0, padding + 1, Anchor.CENTER | Anchor.TOP);

        uiTitleBar = new UIBackgroundContainer(this);
        uiTitleBar.setSize(300, 1);
        uiTitleBar.setPosition(0, 17, Anchor.CENTER | Anchor.TOP);
        uiTitleBar.setColor(Color.gray.getRGB());
        
        xButton = new UIButton(this, ChatColor.BOLD + "X");
        xButton.setSize(5, 1);
        xButton.setPosition(-3, 1, Anchor.RIGHT | Anchor.TOP);
        xButton.setName("button.cancel");
        xButton.register(this);
        
        // Create the almura GUI checkbox
        almuraGuiCheckBox = new UICheckBox(this, ChatColor.WHITE + "Enhanced In-Game HUD");
        almuraGuiCheckBox.setPosition(padding, titleLabel.getY() + (padding * 4 + 10), Anchor.LEFT | Anchor.TOP);
        almuraGuiCheckBox.setChecked(Configuration.DISPLAY_ENHANCED_GUI);
        almuraGuiCheckBox.setName("checkbox.gui.enhanced_gui");
        almuraGuiCheckBox.register(this);
        
        // Create the almura GUI checkbox
        almuraDebugGuiCheckBox = new UICheckBox(this, ChatColor.WHITE + "Enhanced F3 Debug Menu");
        almuraDebugGuiCheckBox.setPosition(padding, almuraGuiCheckBox.getY() + (padding * 4), Anchor.LEFT | Anchor.TOP);
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
        graphicsButton = new UIButton(this, "Load Optimized Settings");
        graphicsButton.setSize(50, 16);
        graphicsButton.setPosition(padding, -padding, Anchor.LEFT | Anchor.BOTTOM);
        graphicsButton.setName("button.graphics");
        graphicsButton.register(this);
        
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

        window.add(titleLabel, uiTitleBar, xButton, almuraGuiCheckBox, almuraDebugGuiCheckBox, debugModeCheckBox, debugLanguagesCheckBox, debugPacksCheckBox, debugMappingsCheckBox, debugRecipesCheckBox, graphicsButton, cancelButton, saveButton);

        // Allow the window to move
        new UIMoveHandle(this, window);

        addToScreen(window);
    }
    
    public void setOptimizedConfig() {
        this.mc.gameSettings.ambientOcclusion = 0;
        this.mc.gameSettings.mipmapLevels = 0;
        this.mc.gameSettings.guiScale = 3;
        this.mc.gameSettings.advancedOpengl = true;
        this.mc.gameSettings.anisotropicFiltering = 0;
        this.mc.gameSettings.limitFramerate = 120;
        this.mc.gameSettings.enableVsync = false;
        this.mc.gameSettings.clouds = false;
        this.mc.gameSettings.snooperEnabled = false;
        this.mc.gameSettings.renderDistanceChunks = 12;
        this.mc.gameSettings.viewBobbing = false;
                
        this.mc.scheduleResourcesRefresh();
        this.mc.gameSettings.saveOptions();
        
        mc.displayGuiScreen(new AlmuraConfirmMenu(parent, "Optimized Configuration Applied.", "Almura 2.0"));
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.graphics":
                setOptimizedConfig();
                break;
            case "button.cancel":
                displayParent();
                break;
            case "button.save":
            try {
                Configuration.toggleEnhancedGUI(almuraGuiCheckBox.isChecked());
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
                displayParent();
        }
    }
}
