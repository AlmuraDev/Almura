/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.multiplayer;

import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.screen.PanoramicScreen;
import com.almuradev.almura.shared.util.Query;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class ServerMenu extends PanoramicScreen {
    private static final ServerData DATA_LIVE_SERVER_PING = new ServerData("Almura", "srv1.almuramc.com", false);
    private static final ServerData DATA_DEV_SERVER_PING = new ServerData("Almura (Dev)", "dev.almuramc.com", false);
    private static final ServerData DATA_LIVE_SERVER_CONNECT = new ServerData("Almura", "srv1.almuramc.com:25566", false);
    private static final ServerData DATA_DEV_SERVER_CONNECT = new ServerData("Almura (Dev)", "dev.almuramc.com:25566", false);
    private static final Query QUERY_LIVE_SERVER = new Query(DATA_LIVE_SERVER_PING, 25566), QUERY_DEV_SERVER = new Query(DATA_DEV_SERVER_PING, 25566);
    private UIButton almuraLiveButton, almuraDevButton;
    private UILabel devServerOnline, devPlayersOnline, liveServerOnline, livePlayersOnline, devServerTitle, liveServerTitle;
    private UIImage logoImage;

    Timer timer = null;
    boolean isRunning = false;

    private static final int padding = 4;

    private UIBackgroundContainer buttonContainer;

    public ServerMenu(@Nullable PanoramicScreen parent) {
        super(parent);
    }

    @Override
    public void construct() {

        // Create the form
        final UIBackgroundContainer form = new UIBackgroundContainer(this, "", 225, 225);
        //form.setTitle(TextFormatting.WHITE + "Multiplayer");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the logo
        logoImage = new UIImage(this, new GuiTexture(GuiConfig.Location.ALMURA_LOGO), null);
        logoImage.setSize(60, 99);
        logoImage.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        liveServerOnline = new UILabel(this, TextFormatting.YELLOW + "Updating...");
        liveServerOnline.setPosition(0, getPaddedY(logoImage, padding) + 7, Anchor.TOP | Anchor.CENTER);

        liveServerTitle = new UILabel(this, TextFormatting.WHITE + "Public Server : ");
        liveServerTitle.setPosition(-liveServerTitle.getWidth()/2 + padding, liveServerOnline.getY(), Anchor.TOP | Anchor.CENTER);

        // Create the live Almura button
        almuraLiveButton = new UIButton(this, "Join ");
        almuraLiveButton.setSize(40, 16);
        almuraLiveButton.setName("button.server.almura.live");
        almuraLiveButton.setPosition(liveServerTitle.getWidth()/2 + padding, getPaddedY(logoImage, padding) + 3, Anchor.TOP | Anchor.CENTER);
        almuraLiveButton.setVisible(false);
        almuraLiveButton.register(this);

        livePlayersOnline = new UILabel(this, TextFormatting.DARK_BLUE + "(0/50)");
        livePlayersOnline.setPosition(0, liveServerOnline.getY(), Anchor.TOP | Anchor.CENTER);
        livePlayersOnline.setVisible(false);

        devServerOnline = new UILabel(this, TextFormatting.YELLOW + "Updating...");
        devServerOnline.setPosition(0, getPaddedY(almuraLiveButton, padding) + 8, Anchor.TOP | Anchor.CENTER);

        devServerTitle = new UILabel(this, TextFormatting.WHITE + "Dev Server : ");
        devServerTitle.setPosition(26, devServerTitle.getY(), Anchor.TOP | Anchor.CENTER);

        // Create the beta Almura button
        almuraDevButton = new UIButton(this, "Join ");
        almuraDevButton.setSize(40, 16);
        almuraDevButton.setName("button.server.almura.dev");
        almuraDevButton.setPosition(devServerOnline.getX() + devServerOnline.getWidth() + 5, getPaddedY(almuraLiveButton, padding) + 3, Anchor.TOP
                | Anchor.LEFT);
        almuraDevButton.setVisible(false);
        almuraDevButton.register(this);

        devPlayersOnline = new UILabel(this, TextFormatting.DARK_BLUE + "(0/50)");
        devPlayersOnline.setPosition(0, getPaddedY(almuraLiveButton, padding) + 8, Anchor.TOP | Anchor.CENTER);
        devPlayersOnline.setVisible(false);

        centerAlign();

        // Create the join another server button
        UIButton anotherButton = new UIButton(this, "Other Multiplayer");
        anotherButton.setPosition(0, getPaddedY(almuraDevButton, padding) + 13, Anchor.TOP | Anchor.CENTER);
        anotherButton.setSize(GuiConfig.Button.WIDTH_LONG, GuiConfig.Button.HEIGHT);
        anotherButton.setName("button.server.another");
        anotherButton.register(this);

        // Create the back button
        UIButton backButton = new UIButton(this, "Back");
        backButton.setPosition(0, getPaddedY(anotherButton, padding) + 3, Anchor.TOP | Anchor.CENTER);
        backButton.setSize(GuiConfig.Button.WIDTH_LONG, GuiConfig.Button.HEIGHT);
        backButton.setName("button.back");
        backButton.register(this);

        final UIButton forumsButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .icon(GuiConfig.Icon.ENJIN)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(-padding, -padding)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .tooltip(I18n.format("almura.menu.forums"))
                .build("button.forums");

        final UIButton issuesButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .icon(GuiConfig.Icon.FA_GITHUB)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(BasicScreen.getPaddedX(forumsButton, padding, Anchor.RIGHT), forumsButton.getY())
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .tooltip(I18n.format(I18n.format("almura.menu.issues")))
                .build("button.issues");

        final UIButton shopButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .icon(GuiConfig.Icon.FA_SHOPPING_BAG)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(BasicScreen.getPaddedX(issuesButton, padding, Anchor.RIGHT), issuesButton.getY())
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .tooltip(I18n.format("almura.menu.shop"))
                .build("button.shop");

        final UILabel trademarkLabel = new UILabel(this, TextFormatting.YELLOW + I18n.format("almura.menu.main.trademark"));
        trademarkLabel.setPosition(padding, -padding, Anchor.BOTTOM | Anchor.LEFT);

        final UILabel copyrightLabel = new UILabel(this, TextFormatting.YELLOW + I18n.format("almura.menu.main.copyright"));
        copyrightLabel.setPosition(trademarkLabel.getX(), BasicScreen.getPaddedY(trademarkLabel, padding, Anchor.BOTTOM), trademarkLabel.getAnchor());

        form.add(logoImage, liveServerTitle, liveServerOnline, almuraLiveButton, devServerTitle,
                devServerOnline, almuraDevButton, devPlayersOnline, livePlayersOnline, anotherButton, backButton);

        form.setColor(Integer.MIN_VALUE);
        form.setBackgroundAlpha(0);

        addToScreen(form, forumsButton, issuesButton, shopButton, trademarkLabel, copyrightLabel);
    }

    private void centerAlign() {
        liveServerOnline.setPosition(0, getPaddedY(logoImage, padding) + 7, Anchor.CENTER | Anchor.TOP);
        liveServerTitle.setPosition(-((liveServerTitle.getWidth()/2) + (liveServerOnline.getWidth()/2) + padding), liveServerOnline.getY(), Anchor.TOP | Anchor.CENTER);
        almuraLiveButton.setPosition((liveServerOnline.getWidth()/2) + (almuraLiveButton.getWidth()/2) + padding * 2, getPaddedY(logoImage, padding) + 3, Anchor.TOP | Anchor.CENTER);
        livePlayersOnline.setPosition((liveServerOnline.getWidth()/2) + (almuraLiveButton.getWidth()) + (livePlayersOnline.getWidth()-4), liveServerOnline.getY(), Anchor.TOP | Anchor.CENTER);

        devServerOnline.setPosition(0, getPaddedY(almuraLiveButton, padding) + 8, Anchor.CENTER | Anchor.TOP);
        devServerTitle.setPosition(-((devServerTitle.getWidth()/2) + (devServerOnline.getWidth()/2) + padding), devServerOnline.getY(), Anchor.TOP | Anchor.CENTER);
        almuraDevButton.setPosition((devServerOnline.getWidth()/2) + (almuraDevButton.getWidth()/2) + padding * 2, getPaddedY(almuraLiveButton, padding) + 3, Anchor.TOP | Anchor.CENTER);
        devPlayersOnline.setPosition((devServerOnline.getWidth()/2) + (almuraDevButton.getWidth()) + (devPlayersOnline.getWidth()-4), devServerOnline.getY(), Anchor.TOP | Anchor.CENTER);
    }

    @Override
    public void onClose() {
        // Future note, this window isn't technically closed when you try and connect to a server.  This is a chicken and egg issue.  Hence the need for the below update method to restart timer.
        super.onClose();
        isRunning = false;
        if (timer != null) {
            timer.cancel();
        }
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException {
        if (!event.getComponent().getName().toLowerCase().equals("button.server.web")
                || (!event.getComponent().getName().toLowerCase().equals("button.server.map"))) {
            close();  //This doesn't close this screen when connecting to a server.
        }
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.server.almura.live":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, DATA_LIVE_SERVER_CONNECT);
                break;
            case "button.server.almura.dev":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, DATA_DEV_SERVER_CONNECT);
                break;
            case "button.server.another":
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case "button.server.map":
                Desktop.getDesktop().browse(new URI("http://srv1.almuramc.com:8123"));
                break;
            case "button.server.web":
                Desktop.getDesktop().browse(new URI("http://www.almuramc.com"));
                break;
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        startTimer();
    }

    /**
     * You must enable query ability within server.properties in order for this method to function.
     * query.port=25566
     * enable-query=true
     */

    public void queryServers() {
        try {
            // Live Server
            if (QUERY_LIVE_SERVER.pingServer()) {
                QUERY_LIVE_SERVER.sendQuery();
                if (QUERY_LIVE_SERVER.getPlayers() == null || QUERY_LIVE_SERVER.getMaxPlayers() == null) {
                    liveServerOnline.setText(TextFormatting.YELLOW + "Restarting...");
                    almuraDevButton.setVisible(false);
                    livePlayersOnline.setVisible(false);
                    centerAlign();
                } else {
                    liveServerOnline.setText(TextFormatting.GREEN + "Online");
                    livePlayersOnline.setText(TextFormatting.DARK_BLUE + "(" + QUERY_LIVE_SERVER.getPlayers() + "/" + QUERY_LIVE_SERVER.getMaxPlayers() + ")");
                    centerAlign();
                }
                almuraLiveButton.setVisible(true);
                livePlayersOnline.setVisible(true);
                centerAlign();
            } else {
                liveServerOnline.setText(TextFormatting.RED + "Offline");
                almuraLiveButton.setVisible(false);
                livePlayersOnline.setVisible(false);
                centerAlign();
            }

            // Dev Server
            if (QUERY_DEV_SERVER.pingServer()) {
                QUERY_DEV_SERVER.sendQuery();
                if (QUERY_DEV_SERVER.getPlayers() == null || QUERY_DEV_SERVER.getMaxPlayers() == null) {
                    devServerOnline.setText(TextFormatting.YELLOW + "Restarting...");
                    almuraDevButton.setVisible(false);
                    devPlayersOnline.setVisible(false);
                    centerAlign();
                } else {
                    devServerOnline.setText(TextFormatting.GREEN + "Online");
                    devPlayersOnline.setText(TextFormatting.DARK_BLUE + "(" + QUERY_DEV_SERVER.getPlayers() + "/" + QUERY_DEV_SERVER.getMaxPlayers() + ")");
                    almuraDevButton.setVisible(true);
                    devPlayersOnline.setVisible(true);
                    centerAlign();
                }
            } else {
                devServerOnline.setText(TextFormatting.RED + "Offline");
                almuraDevButton.setVisible(false);
                devPlayersOnline.setVisible(false);
                centerAlign();
            }



        } catch (Exception ignored) {
        }
    }

    public void startTimer() {
        if (!isRunning) {
            timer = new Timer ();
            TimerTask updateListTask = new TimerTask () {
                @Override
                public void run () {
                    isRunning = true;
                    queryServers();
                }
            };
            timer.schedule(updateListTask, 0L, 3000L);
        }
    }
}
