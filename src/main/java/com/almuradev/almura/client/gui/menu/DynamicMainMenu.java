/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.client.gui.DynamicBackgroundGui;
import com.almuradev.almurasdk.FileSystem;
import com.almuradev.almurasdk.client.gui.SimpleGui;
import com.almuradev.almurasdk.client.gui.components.UIForm;
import com.almuradev.almurasdk.util.Colors;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class DynamicMainMenu extends DynamicBackgroundGui {

    protected static final ResourceLocation ALMURA_LOGO_LOCATION;

    static {
        try {
            ALMURA_LOGO_LOCATION = FileSystem.registerTexture(Almura.MOD_ID, "textures/gui/almura.png", Filesystem.CONFIG_GUI_LOGO_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load logo.", e);
        }
    }

    public DynamicMainMenu(SimpleGui parent) {
        super(parent);
        construct();
    }

    @Override
    public void construct() {
        super.construct();

        // Create the form
        final UIForm form = new UIForm(this, 200, 225, "Almura", false);
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the logo
        final UIImage logoImage = new UIImage(this, new GuiTexture(ALMURA_LOGO_LOCATION), null);
        logoImage.setAnchor(Anchor.CENTER | Anchor.TOP);
        logoImage.setSize(65, 95);

        final int padding = 4;

        // Create the build label
        final UILabel buildLabel = new UILabel(this, Colors.GRAY + Almura.GUI_VERSION);
        buildLabel.setPosition(0, getPaddedY(logoImage, 0), Anchor.CENTER | Anchor.TOP);
        buildLabel.getFontRenderOptions().fontScale = 0.65f;

        // Create the singleplayer button
        final UIButton singleplayerButton = new UIButton(this, Colors.AQUA + "Singleplayer");
        singleplayerButton.setSize(180, 16);
        singleplayerButton.setPosition(0, getPaddedY(logoImage, padding * 3), Anchor.CENTER | Anchor.TOP);
        singleplayerButton.setName("button.singleplayer");
        singleplayerButton.register(this);

        // Create the multiplayer button
        final UIButton multiplayerButton = new UIButton(this, Colors.AQUA + "Multiplayer");
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
        final UILabel copyrightLabel = new UILabel(this, Colors.GRAY + "Copyright AlmuraDev 2012 - 2015 ");
        copyrightLabel.setPosition(0, -9, Anchor.CENTER | Anchor.BOTTOM);
        copyrightLabel.getFontRenderOptions().fontScale = 0.7f;

        // Create the trademark label
        final UILabel trademarkLabel = new UILabel(this, Colors.GRAY + "Minecraft is a registered trademark of Mojang AB");
        trademarkLabel.setPosition(0, -1, Anchor.CENTER | Anchor.BOTTOM);
        trademarkLabel.getFontRenderOptions().fontScale = 0.7f;

        form.getContentContainer().add(logoImage, buildLabel, singleplayerButton, multiplayerButton, optionsButton, configurationButton,
                aboutButton, quitButton, copyrightLabel, trademarkLabel);

        addToScreen(form);
    }

    @Override
    public void onClose() {
        super.onClose();
        mc.shutdown();
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName()) {
            case "button.singleplayer":
                mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case "button.multiplayer":
                mc.displayGuiScreen(new DynamicServerMenu(this));
                break;
            case "button.options":
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                break;
            case "button.configuration":
                mc.displayGuiScreen(new DynamicConfigurationMenu(this));
                break;
            case "button.about":
                mc.displayGuiScreen(new DynamicAboutMenu(this));
                break;
            case "button.quit":
                close();
        }
    }
}
