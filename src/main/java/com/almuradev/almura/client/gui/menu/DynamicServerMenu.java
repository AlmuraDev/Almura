/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.DynamicBackgroundGui;
import com.almuradev.almurasdk.client.gui.SimpleGui;
import com.almuradev.almurasdk.client.gui.components.UIForm;
import com.almuradev.almurasdk.util.Colors;
import com.almuradev.almurasdk.util.Query;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.client.FMLClientHandler;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.ServerData;

public class DynamicServerMenu extends DynamicBackgroundGui {

    private static final ServerData DATA_LIVE_SERVER = new ServerData("Almura", "srv1.almuramc.com");
    private static final ServerData DATA_DEV_SERVER = new ServerData("Almura (Dev)", "dev.almuramc.com");
    private static final Query QUERY_LIVE_SERVER = new Query(DATA_LIVE_SERVER, 25565), QUERY_DEV_SERVER = new Query(DATA_DEV_SERVER, 25565);

    private final Thread QUERIER = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (!Thread.interrupted()) {
                    queryServers();
                }

                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    });

    private UIButton almuraLiveButton;
    private UIButton almuraDevButton;
    private UILabel liveServerOnline;
    private UILabel devServerOnline;

    public DynamicServerMenu(SimpleGui parent) {
        super(parent);
        construct();
    }

    @Override
    public void construct() {
        super.construct();

        // Create the form
        final UIForm form = new UIForm(this, 200, 225, "Multiplayer");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the logo
        final UIImage logoImage = new UIImage(this, new GuiTexture(DynamicMainMenu.ALMURA_LOGO_LOCATION), null);
        logoImage.setAnchor(Anchor.CENTER | Anchor.TOP);
        logoImage.setSize(65, 95);

        // Create the build label
        final UILabel buildLabel = new UILabel(this, Colors.GRAY + Almura.GUI_VERSION);
        buildLabel.setPosition(0, getPaddedY(logoImage, 0), Anchor.CENTER | Anchor.TOP);
        buildLabel.getFontRenderOptions().fontScale = 0.65f;

        final int padding = 4;

        // Create the live Almura button
        almuraLiveButton = new UIButton(this, "Join ");
        almuraLiveButton.setPosition(-15, getPaddedY(logoImage, padding) + 10, Anchor.RIGHT | Anchor.TOP);
        almuraLiveButton.setSize(40, 16);
        almuraLiveButton.setName("button.server.almura.live");
        almuraLiveButton.setDisabled(true);
        almuraLiveButton.register(this);

        UILabel liveServerTitle = new UILabel(this, Colors.WHITE + "Public Server : ");
        liveServerTitle.setPosition(20, getPaddedY(logoImage, padding) + 14, Anchor.LEFT | Anchor.TOP);

        liveServerOnline = new UILabel(this, Colors.YELLOW + "Updating...");
        liveServerOnline.setPosition(85, liveServerTitle.getY(), Anchor.LEFT | Anchor.TOP);

        UILabel devServerTitle = new UILabel(this, Colors.WHITE + "Dev Server : ");
        devServerTitle.setPosition(26, getPaddedY(almuraLiveButton, padding) + 8, Anchor.LEFT | Anchor.TOP);

        devServerOnline = new UILabel(this, Colors.YELLOW + "Updating...");
        devServerOnline.setPosition(85, devServerTitle.getY(), Anchor.LEFT | Anchor.TOP);

        // Create the beta Almura button
        almuraDevButton = new UIButton(this, "Join ");
        almuraDevButton.setPosition(-15, getPaddedY(almuraLiveButton, padding) + 5, Anchor.RIGHT | Anchor.TOP);
        almuraDevButton.setSize(40, 16);
        almuraDevButton.setName("button.server.almura.dev");
        almuraDevButton.setDisabled(true);
        almuraDevButton.register(this);

        // Create the join another server button
        UIButton anotherButton = new UIButton(this, "Join another server");
        anotherButton.setPosition(0, getPaddedY(almuraDevButton, padding) + 5, Anchor.CENTER | Anchor.TOP);
        anotherButton.setSize(100, 16);
        anotherButton.setName("button.server.another");
        anotherButton.register(this);

        // Create the back button
        UIButton backButton = new UIButton(this, "Back");
        backButton.setPosition(0, -10, Anchor.CENTER | Anchor.BOTTOM);
        backButton.setSize(50, 16);
        backButton.setName("button.back");
        backButton.register(this);

        form.getContentContainer().add(logoImage, buildLabel, liveServerTitle, liveServerOnline, almuraLiveButton, devServerTitle,
                devServerOnline, almuraDevButton, anotherButton, backButton);

        addToScreen(form);

        QUERIER.start();
    }

    @Override
    public void onClose() {
        super.onClose();
        QUERIER.interrupt();
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.server.almura.live":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, DATA_LIVE_SERVER);
                break;
            case "button.server.almura.dev":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, DATA_DEV_SERVER);
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
            if (QUERY_LIVE_SERVER.pingServer()) {
                QUERY_LIVE_SERVER.sendQuery();
                if (QUERY_LIVE_SERVER.getPlayers() == null || QUERY_LIVE_SERVER.getMaxPlayers() == null) {
                    liveServerOnline.setText(Colors.YELLOW + "Restarting...");
                } else {
                    liveServerOnline
                            .setText(Colors.GREEN + "Online " + Colors.BLUE + "(" + QUERY_LIVE_SERVER.getPlayers() + "/" + QUERY_LIVE_SERVER
                                    .getMaxPlayers() + ")");
                }
                almuraLiveButton.setDisabled(false);
            } else {
                liveServerOnline.setText(Colors.RED + "Offline");
                almuraLiveButton.setDisabled(true);
            }

            // Dev Server
            if (QUERY_DEV_SERVER.pingServer()) {
                QUERY_DEV_SERVER.sendQuery();
                if (QUERY_DEV_SERVER.getPlayers() == null || QUERY_DEV_SERVER.getMaxPlayers() == null) {
                    devServerOnline.setText(Colors.YELLOW + "Restarting...");
                } else {
                    devServerOnline
                            .setText(Colors.GREEN + "Online " + Colors.BLUE + "(" + QUERY_DEV_SERVER.getPlayers() + "/" + QUERY_DEV_SERVER
                                    .getMaxPlayers() + ")");
                }
                almuraDevButton.setDisabled(false);
            } else {
                devServerOnline.setText(Colors.RED + "Offline");
                almuraDevButton.setDisabled(true);
            }

        } catch (Exception ignored) {
        }
    }
}

