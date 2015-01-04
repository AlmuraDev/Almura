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

import cpw.mods.fml.client.FMLClientHandler;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.ServerData;

public class AlmuraServerMenu extends AlmuraBackgroundGui {

    private static final ServerData ALMURA_LIVE_SERVER_DATA = new ServerData("Almura", "srv1.almuramc.com");
    private static final ServerData ALMURA_DEV_SERVER_DATA = new ServerData("Almura (Beta)", "69.4.96.139");
    private static final ServerData OBSIDIANBOX_LIVE_SERVER_DATA = new ServerData("ObsidianBox", "obsidianbox.org");
    private UIBackgroundContainer window;
    private UIButton almuraLiveButton, almuraDevButton, obsidianboxLiveButton, anotherButton, backButton;
    private UIImage logoImage;
    private UILabel buildLabel;

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added

     * @param parent the {@link AlmuraGui} that we came from
     */
    public AlmuraServerMenu(AlmuraGui parent) {
        super(parent);
    }

    @Override
    protected void setup() {
        // Create the window container
        window = new UIBackgroundContainer(this);
        window.setSize(225, 225);
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
        xButton.setName("button.back");
        xButton.register(this);

        
        // Create the logo
        logoImage = new UIImage(this, new GuiTexture(AlmuraMainMenu.ALMURA_LOGO_LOCATION), null);
        logoImage.setAnchor(Anchor.CENTER | Anchor.TOP);
        logoImage.setSize(65, 95);
        logoImage.setPosition(0, 15);

        // Create the build label
        buildLabel = new UILabel(this, ChatColor.GRAY + Almura.GUI_VERSION);
        buildLabel.setPosition(0, getPaddedY(logoImage, 0), Anchor.CENTER | Anchor.TOP);
        buildLabel.setFontScale(0.65f);

        final int padding = 4;

        // Create the live Almura button
        almuraLiveButton = new UIButton(this, "Join " + ChatColor.GOLD + ALMURA_LIVE_SERVER_DATA.serverName);
        almuraLiveButton.setPosition(0, getPaddedY(logoImage, padding * 3), Anchor.CENTER | Anchor.TOP);
        almuraLiveButton.setSize(205, 16);
        almuraLiveButton.setName("button.server.almura.live");
        almuraLiveButton.setDisabled(false);
        almuraLiveButton.register(this);

        // Create the dev Almura button
        almuraDevButton = new UIButton(this, "Join " + ChatColor.AQUA + ALMURA_DEV_SERVER_DATA.serverName);
        almuraDevButton.setPosition(10, getPaddedY(almuraLiveButton, padding), Anchor.LEFT | Anchor.TOP);
        almuraDevButton.setSize(70, 16);
        almuraDevButton.setName("button.server.almura.dev");
        almuraDevButton.register(this);

        // Create the live ObsidianBox button
        obsidianboxLiveButton = new UIButton(this, "Join " + ChatColor.LIGHT_PURPLE + OBSIDIANBOX_LIVE_SERVER_DATA.serverName);
        obsidianboxLiveButton.setPosition(-10, getPaddedY(almuraLiveButton, padding), Anchor.RIGHT | Anchor.TOP);
        obsidianboxLiveButton.setSize(70, 16);
        obsidianboxLiveButton.setName("button.server.obsidianbox.live");
        obsidianboxLiveButton.register(this);

        // Create the join another server button
        anotherButton = new UIButton(this, "Join another server");
        anotherButton.setPosition(0, getPaddedY(obsidianboxLiveButton, padding), Anchor.CENTER | Anchor.TOP);
        anotherButton.setSize(205, 16);
        anotherButton.setName("button.server.another");
        anotherButton.register(this);

        // Create the back button
        backButton = new UIButton(this, "Back");
        backButton.setPosition(0, -padding, Anchor.CENTER | Anchor.BOTTOM);
        backButton.setSize(50, 16);
        backButton.setName("button.back");
        backButton.register(this);

        window.add(titleLabel, uiTitleBar, xButton, logoImage, buildLabel, almuraLiveButton, almuraDevButton, obsidianboxLiveButton, anotherButton, backButton);

        // Allow the window to move
        new UIMoveHandle(this, window);

        addToScreen(window);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.server.almura.live":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, ALMURA_LIVE_SERVER_DATA);
                break;
            case "button.server.almura.dev":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, ALMURA_DEV_SERVER_DATA);
                break;
            case "button.server.obsidianbox.live":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, OBSIDIANBOX_LIVE_SERVER_DATA);
                break;
            case "button.server.another":
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case "button.back":
                displayParent();
        }
    }
}

