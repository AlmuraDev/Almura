/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import java.awt.Color;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraBackgroundGui;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.client.GuiModList;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.util.ResourceLocation;

public class AlmuraMainMenu extends AlmuraBackgroundGui {

    protected static final ResourceLocation ALMURA_LOGO_LOCATION = new ResourceLocation(Almura.MOD_ID, "textures/gui/almura.png");

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added

     * @param parent the {@link AlmuraGui} that we came from
     */
    public AlmuraMainMenu(AlmuraGui parent) {
        super(parent);
    }

    @Override
    protected void setup() {
        // Create the window container
        final UIBackgroundContainer window = new UIBackgroundContainer(this);
        window.setSize(200, 225);
        window.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        window.setColor(Integer.MIN_VALUE);
        window.setBackgroundAlpha(125);

        // Create the title & Window layout 
        final UILabel titleLabel = new UILabel(this, ChatColor.WHITE + "Welcome to Almura 2.0");
        titleLabel.setPosition(0, 5, Anchor.CENTER | Anchor.TOP);

        final UIBackgroundContainer uiTitleBar = new UIBackgroundContainer(this);
        uiTitleBar.setSize(300, 1);
        uiTitleBar.setPosition(0, 17, Anchor.CENTER | Anchor.TOP);
        uiTitleBar.setColor(Color.gray.getRGB());
        
        final UIButton xButton = new UIButton(this, ChatColor.BOLD + "X");
        xButton.setSize(5, 1);
        xButton.setPosition(-3, 1, Anchor.RIGHT | Anchor.TOP);
        xButton.setName("button.quit");
        xButton.register(this);
        
        // Create the logo
        final UIImage logoImage = new UIImage(this, new GuiTexture(ALMURA_LOGO_LOCATION), null);
        logoImage.setAnchor(Anchor.CENTER | Anchor.TOP);
        logoImage.setPosition(0,15);
        logoImage.setSize(65, 95);

        final int padding = 4;

        // Create the build label
        final UILabel buildLabel = new UILabel(this, ChatColor.GRAY + Almura.GUI_VERSION);
        buildLabel.setPosition(0, getPaddedY(logoImage, 0), Anchor.CENTER | Anchor.TOP);
        buildLabel.setFontScale(0.65f);

        // Create the singleplayer button
        final UIButton singleplayerButton = new UIButton(this, ChatColor.AQUA + "Singleplayer");
        singleplayerButton.setSize(180, 16);
        singleplayerButton.setPosition(0, getPaddedY(logoImage, padding * 3), Anchor.CENTER | Anchor.TOP);
        singleplayerButton.setName("button.singleplayer");
        singleplayerButton.register(this);

        // Create the multiplayer button
        final UIButton multiplayerButton = new UIButton(this, ChatColor.AQUA + "Multiplayer");
        multiplayerButton.setSize(180, 16);
        multiplayerButton.setPosition(0, getPaddedY(singleplayerButton, padding), Anchor.CENTER | Anchor.TOP);
        multiplayerButton.setName("button.multiplayer");
        multiplayerButton.register(this);

        // Create the options button
        final UIButton optionsButton = new UIButton(this, "Options");
        optionsButton.setSize(50, 16);
        optionsButton.setPosition(10, getPaddedY(multiplayerButton, padding), Anchor.LEFT | Anchor.TOP);
        optionsButton.setName("button.options");
        optionsButton.register(this);

        // Create the Configuration button
        final UIButton configurationButton = new UIButton(this, "Configuration");
        configurationButton.setSize(76, 16);
        configurationButton.setPosition(0, getPaddedY(multiplayerButton, padding), Anchor.CENTER | Anchor.TOP);
        configurationButton.setName("button.configuration");
        configurationButton.register(this);

        // Create the Mods button
        final UIButton aboutButton = new UIButton(this, "About");
        aboutButton.setSize(50, 16);
        aboutButton.setPosition(-10, getPaddedY(multiplayerButton, padding), Anchor.RIGHT | Anchor.TOP);
        aboutButton.setName("button.about");
        aboutButton.register(this);

        // Create the quit button
        final UIButton quitButton = new UIButton(this, "Quit");
        quitButton.setSize(50, 16);
        quitButton.setPosition(0, getPaddedY(configurationButton, padding + 10), Anchor.CENTER | Anchor.TOP);
        quitButton.setName("button.quit");
        quitButton.register(this);

        // Create the copyright label
        final UILabel copyrightLabel = new UILabel(this, ChatColor.GRAY + "Copyright AlmuraDev 2012 - 2015 ");
        copyrightLabel.setPosition(0, -9, Anchor.CENTER | Anchor.BOTTOM);
        copyrightLabel.setFontScale(0.7f);

        // Create the trademark label
        final UILabel trademarkLabel = new UILabel(this, ChatColor.GRAY + "Minecraft is a registered trademark of Mojang AB");
        trademarkLabel.setPosition(0, -1, Anchor.CENTER | Anchor.BOTTOM);
        trademarkLabel.setFontScale(0.7f);

        window.add(titleLabel, uiTitleBar, xButton, logoImage, buildLabel, singleplayerButton, multiplayerButton, optionsButton, configurationButton,
                   aboutButton, quitButton, copyrightLabel, trademarkLabel);

        // Allow the window to move
        new UIMoveHandle(this, window);

        addToScreen(window);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.singleplayer":
                mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case "button.multiplayer":
                mc.displayGuiScreen(new AlmuraServerMenu(this));
                break;
            case "button.options":
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                break;
            case "button.configuration":
                mc.displayGuiScreen(new AlmuraConfigurationMenu(this));
                break;
            case "button.about":
                mc.displayGuiScreen(new AlmuraAboutMenu(this));
                break;
            case "button.quit":
                mc.shutdown();
        }
    }
}
