/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.client.ChatColor;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.GuiModList;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UIButton.ClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;

public class AlmuraServerMenu extends AlmuraGui {

    private static final GuiTexture
            ALMURA_2_LOGO =
            new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/background/almura2b.png"));
    private static final Random RANDOM = new Random();
    private static final Map<Integer, GuiTexture> BACKGROUNDS = Maps.newHashMap();
    private static int imageNum = 0;
    public UIBackgroundContainer window;
    public UIButton backButton, betaServerButton, liveServerButton, devServerButton, otherServerButton;
    public int screenH, screenW;
    public UIImage backgroundImage;
    public int tick = 1;
    int timer = 1;

    @SuppressWarnings("rawtypes")
    public AlmuraServerMenu() {

        guiscreenBackground = false; // prevent full screen black background.       
        screenH = Minecraft.getMinecraft().displayHeight;
        screenW = Minecraft.getMinecraft().displayWidth;

        // Main Container
        UIContainer main = new UIContainer(this);

        // Container background image.
        GuiTexture background = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/background/evening/evening1.jpg"));
        backgroundImage = new UIImage(this, background, null);
        backgroundImage.setZIndex(-1);
        backgroundImage.setSize(0, 0);
        try {
            randomBackground();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Buttons Window.
        window = new UIBackgroundContainer(this);
        window.setSize(200, 240);
        window.setPosition(5, 0, Anchor.CENTER | Anchor.MIDDLE);
        window.setColor(Integer.MIN_VALUE);
        window.setTopAlpha(100);
        window.setBottomAlpha(100);

        //Message contents
        UIContainer panel = new UIContainer(this, 75, 100);
        panel.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);

        final UIImage almuraLogo = new UIImage(this, ALMURA_2_LOGO, null);
        almuraLogo.setSize(panel.getWidth(), panel.getHeight());

        final UILabel version = new UILabel(this, ChatColor.WHITE + Almura.GUI_VERSION);
        version.setPosition(0, 105, Anchor.CENTER | Anchor.TOP);
        version.setFontScale(0.75F);

        liveServerButton =
                (new UIButton(this, ChatColor.WHITE + "Logon to " + ChatColor.GOLD + "Almura Live" + ChatColor.WHITE + " Server").setPosition(
                        0, -100, Anchor.CENTER | Anchor.BOTTOM).register(this));
        liveServerButton.setSize(180, 15);
        liveServerButton.setTooltip(new UITooltip(this, "Logon to Almura 2 Live Server", 20));
        liveServerButton.setName("BTNLIVESERVER");

        devServerButton =
                (new UIButton(this, ChatColor.WHITE + "Logon to " + ChatColor.GREEN + "ObsidianBox" + ChatColor.WHITE + " Server").setPosition(
                        0, -80, Anchor.BOTTOM | Anchor.CENTER).register(this));
        devServerButton.setSize(180, 15);
        devServerButton.setTooltip(new UITooltip(this, "Logon to ObsidianBox Server", 20));
        devServerButton.setName("BTNDEVSERVER");

        betaServerButton =
                (new UIButton(this, ChatColor.WHITE + "Logon to " + ChatColor.AQUA + "Almura Beta" + ChatColor.WHITE + " Server").setPosition(
                        0, -60, Anchor.CENTER | Anchor.BOTTOM).register(this));
        betaServerButton.setSize(180, 15);
        betaServerButton.setTooltip(new UITooltip(this, "Logon to Almura 2 Live Server", 5));
        betaServerButton.setName("BTNLIVESERVER");

        otherServerButton =
                (new UIButton(this, ChatColor.WHITE + "Other " + ChatColor.LIGHT_PURPLE + "Multiplayer" + ChatColor.WHITE + " Servers").setPosition(
                        0, -40, Anchor.CENTER | Anchor.BOTTOM).register(this));
        otherServerButton.setSize(180, 15);
        otherServerButton.setTooltip(new UITooltip(this, "Logon to Almura 2 Live Server", 5));
        otherServerButton.setName("BTNMULTIPLAYER");

        backButton = (new UIButton(this, ChatColor.WHITE + "Back to MainMenu").setPosition(0, -10, Anchor.CENTER | Anchor.BOTTOM).register(this));
        backButton.setSize(100, 15);
        backButton.setName("BTNBACK");

        panel.add(almuraLogo);
        window.add(panel, devServerButton, betaServerButton, liveServerButton, otherServerButton, backButton, version);

        main.add(backgroundImage, window);

        new UIMoveHandle(this, window);

        addToScreen(main);
    }

    @Override
    public void close() {
    }

    @Subscribe
    public void onButtonClick(ClickEvent event) {
        switch (event.getComponent().getName().toUpperCase()) {
            case "BTNDEVSERVER":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, new ServerData("ObsidianBox", "obsidianbox.org"));
                break;
            case "BTNBETASERVER":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, new ServerData("BetaServer", "69.4.96.139"));
                break;
            case "BTNLIVESERVER":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, new ServerData("LiveServer", "srv1.almuramc.com"));
                break;
            case "BTNMULTIPLAYER":
                this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiMultiplayer(this));
                break;
            case "BTNBACK":
                this.mc.displayGuiScreen(new AlmuraMainMenu());
                break;
            case "BTNMODS":
                this.mc.displayGuiScreen(new GuiModList(this));
                break;
            case "BTNCLOSE":
                this.mc.shutdown();
                break;
        }
        this.close();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (screenH != Minecraft.getMinecraft().currentScreen.height || screenW != Minecraft.getMinecraft().currentScreen.width) {
            try {
                randomBackground();
            } catch (IOException e) {
                e.printStackTrace();
            }
            screenH = Minecraft.getMinecraft().currentScreen.height;
            screenW = Minecraft.getMinecraft().currentScreen.width;
        }
        try {
            animate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        renderer.enableBlending();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void animate() throws IOException {
        if (tick++ % 2 == 0) {
            backgroundImage.setSize(backgroundImage.getWidth() + 1, backgroundImage.getHeight() + 1);
            timer++;

            if (timer == 60) {
                randomBackground();
                backgroundImage.setPosition(0, 0, Anchor.BOTTOM | Anchor.RIGHT);
                backgroundImage.setSize(0, 0);
            }

            if (timer == 120) {
                randomBackground();
                backgroundImage.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);
                backgroundImage.setSize(0, 0);
            }

            if (timer == 180) {
                randomBackground();
                backgroundImage.setPosition(0, 0, Anchor.BOTTOM | Anchor.LEFT);
                backgroundImage.setSize(0, 0);
            }

            if (timer > 240) {
                randomBackground();
                backgroundImage.setPosition(0, 0, Anchor.TOP | Anchor.LEFT);
                timer = 0;
            }
        }
    }

    private String getTime() {
        int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hours < 12) {
            imageNum = RANDOM.nextInt(29 - 1) + 1;
            return "day";
        } else if (hours < 20) {
            imageNum = RANDOM.nextInt(16 - 1) + 1;
            return "evening";
        } else {
            imageNum = RANDOM.nextInt(13 - 1) + 1;
            return "night";
        }
    }

    private void randomBackground() throws IOException {
        final String qualifier = getTime().toLowerCase();
        GuiTexture background = BACKGROUNDS.get(imageNum);
        if (background == null) {
            final String location = "textures/background/" + qualifier + "/" + qualifier + imageNum + ".jpg";
            final Path path = Paths.get(Filesystem.CONFIG_BACKGROUNDS_PATH.toString(), qualifier, qualifier + imageNum + ".jpg");
            background = new GuiTexture(Filesystem.registerTexture(Almura.MOD_ID, location, path));
            BACKGROUNDS.put(imageNum, background);
        }
        backgroundImage.setIcon(background, null);
        backgroundImage.setZIndex(-1);
        backgroundImage.setSize(0, 0);
        backgroundImage.setPosition(0, 0, Anchor.TOP | Anchor.LEFT);
    }
}

