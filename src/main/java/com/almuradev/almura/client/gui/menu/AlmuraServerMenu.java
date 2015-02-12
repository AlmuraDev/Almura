/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraBackgroundGui;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.almuradev.almura.util.Query;
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

import java.awt.*;

public class AlmuraServerMenu extends AlmuraBackgroundGui {

    private static final ServerData ALMURA_LIVE_SERVER_DATA = new ServerData("Almura", "srv1.almuramc.com");
    private static final ServerData ALMURA_DEV_SERVER_DATA = new ServerData("Almura (Dev)", "69.4.96.139");
    private static final ServerData OBSIDIANBOX_LIVE_SERVER_DATA = new ServerData("ObsidianBox", "obsidianbox.org");
    private UIBackgroundContainer window;
    private UIButton almuraLiveButton, almuraDevButton, anotherButton, backButton;
    private UIImage logoImage;
    private UILabel buildLabel, liveServerTitle, liveServerOnline, devServerTitle, devServerOnline;
    private Query liveServerQuery, devServerQuery = null;
    private int padding = 4;
    
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



        // Create the live Almura button
        almuraLiveButton = new UIButton(this, "Join ");
        almuraLiveButton.setPosition(-15, getPaddedY(logoImage, padding) + 10, Anchor.RIGHT | Anchor.TOP);
        almuraLiveButton.setSize(40, 16);
        almuraLiveButton.setName("button.server.almura.live");
        almuraLiveButton.setDisabled(false);
        almuraLiveButton.register(this);
        
        liveServerTitle = new UILabel(this, ChatColor.WHITE + "Public Server : ");
        liveServerTitle.setPosition(20, getPaddedY(logoImage, padding) + 14, Anchor.LEFT | Anchor.TOP);

        devServerTitle = new UILabel(this, ChatColor.WHITE + "Dev Server : ");
        devServerTitle.setPosition(26, getPaddedY(almuraLiveButton, padding) + 8, Anchor.LEFT | Anchor.TOP);
        
        // Create the beta Almura button
        almuraDevButton = new UIButton(this, "Join ");
        almuraDevButton.setPosition(-15, getPaddedY(almuraLiveButton, padding) + 5, Anchor.RIGHT | Anchor.TOP);
        almuraDevButton.setSize(40, 16);
        almuraDevButton.setName("button.server.almura.dev");
        almuraDevButton.register(this);
        
        // Create the join another server button
        anotherButton = new UIButton(this, "Join another server");
        anotherButton.setPosition(0, getPaddedY(almuraDevButton, padding) + 15, Anchor.CENTER | Anchor.TOP);
        anotherButton.setSize(100, 16);
        anotherButton.setName("button.server.another");
        anotherButton.register(this);

        // Create the back button
        backButton = new UIButton(this, "Back");
        backButton.setPosition(0, -10, Anchor.CENTER | Anchor.BOTTOM);
        backButton.setSize(50, 16);
        backButton.setName("button.back");
        backButton.register(this);

        queryServers();

        window.add(titleLabel, uiTitleBar, xButton, logoImage, buildLabel, liveServerTitle, liveServerOnline, almuraLiveButton, devServerTitle, devServerOnline, almuraDevButton, anotherButton,
                   backButton);

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
                close();
        }
    }

    public void queryServers() {
        try {
            // Live Server
            liveServerQuery = new Query("srv1.almuramc.com", 25565);
            if (liveServerQuery.pingServer()) {
                liveServerQuery.sendQuery();
                if (liveServerQuery.getPlayers() == null || liveServerQuery.getMaxPlayers() == null) {
                    liveServerOnline = new UILabel(this, ChatColor.YELLOW + "Restarting...");
                } else {
                    liveServerOnline = new UILabel(this, ChatColor.GREEN + "Online " + ChatColor.BLUE + "(" + liveServerQuery.getPlayers() + "/" + liveServerQuery.getMaxPlayers() + ")");
                }
                almuraLiveButton.setDisabled(false);
            } else {
                liveServerOnline = new UILabel(this, ChatColor.RED + "Offline");
                almuraLiveButton.setDisabled(true);
            }
            liveServerOnline.setPosition(85, liveServerTitle.getY(), Anchor.LEFT | Anchor.TOP);
            
            // Dev Server
            devServerQuery = new Query("69.4.96.139", 25565);
            if (devServerQuery.pingServer()) {
                devServerQuery.sendQuery();
                if (devServerQuery.getPlayers() == null || devServerQuery.getMaxPlayers() == null) {
                    devServerOnline = new UILabel(this, ChatColor.YELLOW + "Restarting...");
                } else {
                    devServerOnline = new UILabel(this, ChatColor.GREEN + "Online " + ChatColor.BLUE + "(" + devServerQuery.getPlayers() + "/" + devServerQuery.getMaxPlayers() + ")");
                }
                almuraDevButton.setDisabled(false);
            } else {
                devServerOnline = new UILabel(this, ChatColor.RED + "Offline");
                almuraDevButton.setDisabled(true);
            }
            devServerOnline.setPosition(85, devServerTitle.getY(), Anchor.LEFT | Anchor.TOP);

        } catch (Exception e) {
             // System.out.println("Exception: " + e);
        } finally {
           // Maybe do something eventually.
           
        }
    }
}

