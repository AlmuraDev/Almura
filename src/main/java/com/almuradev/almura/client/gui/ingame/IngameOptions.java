/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.client.gui.guide.ViewPagesGui;
import com.almuradev.almura.client.gui.menu.DynamicConfigurationMenu;
import com.almuradev.almura.util.Colors;
import com.almuradev.almura.util.FileSystem;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;

import java.awt.AWTException;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class IngameOptions extends SimpleGui {

    protected static final ResourceLocation ALMURA_LOGO_LOCATION;

    static {
        try {
            ALMURA_LOGO_LOCATION = FileSystem.registerTexture(Almura.MOD_ID, "textures/gui/almura.png", FileSystem.CONFIG_GUI_LOGO_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load logo.", e);
        }
    }

    public IngameOptions() {
    }

    @Override
    public void construct() {
        guiscreenBackground = true;
        // Create the form
        final UIForm form = new UIForm(this, 225, 225, "Almura", true);
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the logo
        final UIImage logoImage = new UIImage(this, new GuiTexture(ALMURA_LOGO_LOCATION), null);
        logoImage.setAnchor(Anchor.CENTER | Anchor.TOP);
        logoImage.setSize(65, 95);

        final int padding = 4;

        // Create the Back to Game button
        final UIButton backButton = new UIButton(this, Colors.AQUA + "Back to game");
        backButton.setSize(180, 16);
        backButton.setPosition(0, getPaddedY(logoImage, padding * 3), Anchor.CENTER | Anchor.TOP);
        backButton.setName("button.backbutton");
        backButton.register(this);

        // Create the Open to Lan / Guide button
        final UIButton lanButton = new UIButton(this, Colors.WHITE + "Open to Lan");
        if (this.mc.isSingleplayer() && !this.mc.getIntegratedServer().getPublic()) {
            lanButton.setText("Open to Lan");
            lanButton.setSize(80, 16);
            lanButton.setPosition(30, getPaddedY(backButton, padding), Anchor.LEFT | Anchor.TOP);
            lanButton.setName("button.lan");
        } else {
            lanButton.setText("Open Guide");
            lanButton.setSize(80, 16);
            lanButton.setPosition(30, getPaddedY(backButton, padding), Anchor.LEFT | Anchor.TOP);
            lanButton.setName("button.guide");
        }
        lanButton.register(this);

        // Create the Open to Mod Help Button
        final UIButton helpButton = new UIButton(this, Colors.WHITE + "Open Mod Help");
        helpButton.setSize(80, 16);
        helpButton.setPosition(-30, getPaddedY(backButton, padding), Anchor.RIGHT | Anchor.TOP);
        helpButton.setName("button.help");
        helpButton.register(this);

        // Create the options button
        final UIButton optionsButton = new UIButton(this, "Options");
        optionsButton.setSize(65, 16);
        optionsButton.setPosition(10, getPaddedY(lanButton, padding), Anchor.LEFT | Anchor.TOP);
        optionsButton.setName("button.options");
        optionsButton.register(this);

        // Create the Achievements button
        final UIButton achievementsButton = new UIButton(this, "Achievements");
        achievementsButton.setSize(65, 16);
        achievementsButton.setPosition(0, getPaddedY(lanButton, padding), Anchor.CENTER | Anchor.TOP);
        achievementsButton.setName("button.achievements");
        achievementsButton.register(this);

        // Create the Statistics button
        final UIButton statisticsButton = new UIButton(this, "Statistics");
        statisticsButton.setSize(65, 16);
        statisticsButton.setPosition(-10, getPaddedY(lanButton, padding), Anchor.RIGHT | Anchor.TOP);
        statisticsButton.setName("button.stats");
        statisticsButton.register(this);

        // Create the Configuration button
        final UIButton configButton = new UIButton(this, "Configuration");
        configButton.setSize(65, 16);
        configButton.setPosition(10, getPaddedY(optionsButton, padding), Anchor.LEFT | Anchor.TOP);
        configButton.setName("button.config");
        configButton.register(this);

        // Create the Live Map button
        final UIButton mapButton = new UIButton(this, "Live Map");
        mapButton.setSize(65, 16);
        mapButton.setPosition(0, getPaddedY(optionsButton, padding), Anchor.CENTER | Anchor.TOP);
        mapButton.setName("button.map");
        mapButton.register(this);

        // Create the Visit Website button
        final UIButton webstatisticsButton = new UIButton(this, "Visit Website");
        webstatisticsButton.setSize(65, 16);
        webstatisticsButton.setPosition(-10, getPaddedY(optionsButton, padding), Anchor.RIGHT | Anchor.TOP);
        webstatisticsButton.setName("button.website");
        webstatisticsButton.register(this);

        // Create the quit button
        final UIButton quitButton = new UIButton(this, "Quit");
        quitButton.setSize(50, 16);
        quitButton.setPosition(0, getPaddedY(mapButton, padding), Anchor.CENTER | Anchor.TOP);
        quitButton.setName("button.quit");
        quitButton.register(this);

        form.getContentContainer().add(logoImage, backButton, lanButton, helpButton, optionsButton, achievementsButton,
                statisticsButton, configButton, webstatisticsButton, mapButton, quitButton);
        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException, AWTException {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.backbutton":
                close();
                this.mc.displayGuiScreen((GuiScreen) null);
                this.mc.setIngameFocus();
                break;
            case "button.achievements":
                close();
                if (this.mc.thePlayer != null) {
                    this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
                }
                break;
            case "button.options":
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case "button.config":
                new DynamicConfigurationMenu(this).display();
                break;
            case "button.map":
                Desktop.getDesktop().browse(new URI("http://srv1.almuramc.com:8123"));
                break;
            case "button.website":
                Desktop.getDesktop().browse(new URI("http://www.almuramc.com"));
                break;
            case "button.stats":
                Desktop.getDesktop().browse(new URI("http://srv1.almuramc.com:8080"));
                break;
            case "button.lan":
                this.mc.displayGuiScreen(new GuiShareToLan(this));
                break;
            case "button.guide":
                if (this.mc.thePlayer != null) {
                    close();
                    new ViewPagesGui().display();
                }
                break;
            case "button.help":
                new IngameModHelp().display();
                break;
            case "button.quit":
                close();
                this.mc.theWorld.sendQuittingDisconnectingPacket();
                this.mc.loadWorld((WorldClient) null);
                this.mc.displayGuiScreen(new GuiMainMenu());
                break;
        }
    }
}
