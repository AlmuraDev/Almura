/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ChatColor;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.GuiModList;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
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

import org.lwjgl.input.Keyboard;

import java.util.Calendar;
import java.util.Random;

public class AlmuraMainMenu extends AlmuraGui {

    private static final GuiTexture ALMURA_2_LOGO = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/background/almura2b.png"));
    private static final Random RANDOM = new Random();
    private static int imageNum = 0;
    public UIBackgroundContainer window;
    public UIButton singlePlayerButton, optionsButton, liveServerButton, devServerButton, closeButton;
    public int screenH, screenW;
    public UIImage backgroundImage;
    public int tick = 1;
    int timer = 1;

    @SuppressWarnings("rawtypes")
    public AlmuraMainMenu() {

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
        randomBackground();

        // Buttons Window.
        window = new UIBackgroundContainer(this);
        window.setSize(200, 225);
        window.setPosition(5, 0, Anchor.CENTER | Anchor.MIDDLE);
        window.setColor(Integer.MIN_VALUE);
        window.setTopAlpha(100);
        window.setBottomAlpha(100);

        //Message contents
        UIContainer panel = new UIContainer(this, 75, 100);
        panel.setPosition(0, 10, Anchor.CENTER | Anchor.TOP);

        final UIImage almuraLogo = new UIImage(this, ALMURA_2_LOGO, null);
        almuraLogo.setSize(panel.getWidth(), panel.getHeight());

        singlePlayerButton = (new UIButton(this, ChatColor.WHITE + "B").setPosition(0, -90, Anchor.BOTTOM | Anchor.CENTER).register(this));
        singlePlayerButton.setSize(180, 15);
        singlePlayerButton.setName("BTNSINGLEPLAYER");

        devServerButton = (new UIButton(this, ChatColor.WHITE + "Logon to " + ChatColor.GOLD + "ObsidianBox" + ChatColor.WHITE + " Server").setPosition(0, -70, Anchor.BOTTOM | Anchor.CENTER).register(this));
        devServerButton.setSize(180, 15);
        devServerButton.setTooltip(new UITooltip(this, "Logon to ObsidianBox Server", 5));
        devServerButton.setName("BTNDEVSERVER");

        liveServerButton = (new UIButton(this, ChatColor.WHITE + "Logon to " + ChatColor.AQUA + "Almura Test" + ChatColor.WHITE + " Server").setPosition(0, -50, Anchor.CENTER | Anchor.BOTTOM).register(this));
        liveServerButton.setSize(180, 15);
        liveServerButton.setTooltip(new UITooltip(this, "Logon to Almura 2 Live Server", 5));
        liveServerButton.setName("BTNLIVESERVER");

        optionsButton = (new UIButton(this, ChatColor.WHITE + "Options").setPosition(-30, -20, Anchor.CENTER | Anchor.BOTTOM).register(this));
        optionsButton.setSize(50, 15);
        optionsButton.setName("BTNOPTIONS");

        closeButton = (new UIButton(this, ChatColor.WHITE + "Quit").setPosition(30, -20, Anchor.CENTER | Anchor.BOTTOM).register(this));
        closeButton.setSize(50, 15);
        closeButton.setName("BTNCLOSE");

        final UILabel version = new UILabel(this, ChatColor.WHITE + Almura.GUI_VERSION);
        version.setPosition(0, -5, Anchor.CENTER | Anchor.BOTTOM);
        version.setFontScale(0.75F);

        panel.add(almuraLogo);
        window.add(panel, singlePlayerButton, devServerButton, liveServerButton, optionsButton, closeButton, version);

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
            case "BTNSINGLEPLAYER":
                this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiSelectWorld(this));
                break;
            case "BTNMULTIPLAYER":
                this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiMultiplayer(this));
                break;
            case "BTNDEVSERVER":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, new ServerData("ObsidianBox", "obsidianbox.org"));
                break;
            case "BTNLIVESERVER":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, new ServerData("LocalHost", "69.4.96.139"));
                break;
            case "BTNLOCALHOSTSERVER":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, new ServerData("LocalHost", "localhost"));
                break;
            case "BTNOPTIONS":
                this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiOptions(this, this.mc.gameSettings));
                break;
            case "BTNMODS":                
                this.mc.displayGuiScreen(new GuiModList(this));
                break;
            case "BTNCLOSE":
                this.mc.shutdown();
                break;

        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (screenH != Minecraft.getMinecraft().currentScreen.height || screenW != Minecraft.getMinecraft().currentScreen.width) {
            randomBackground();
            screenH = Minecraft.getMinecraft().currentScreen.height;
            screenW = Minecraft.getMinecraft().currentScreen.width;
        }
        animate();
        renderer.enableBlending();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            singlePlayerButton.setText("Multiplayer");
            singlePlayerButton.setName("BTNMULTIPLAYER");
            singlePlayerButton.setSize(180, 15);
            singlePlayerButton.setTooltip(new UITooltip(this, "Play Multiplayer using Almura 2", 5));
            
            liveServerButton.setText("Localhost");
            liveServerButton.setName("BTNLOCALHOSTSERVER");
            liveServerButton.setSize(180, 15);
            liveServerButton.setTooltip(new UITooltip(this, "Logon to localhost server", 5));
            
            optionsButton.setText("Mods");
            optionsButton.setSize(50, 15);
            optionsButton.setName("BTNMODS");
            
            
        } else {
            singlePlayerButton.setText("Singleplayer");
            singlePlayerButton.setName("BTNSINGLEPLAYER");
            singlePlayerButton.setSize(180, 15);
            singlePlayerButton.setTooltip(new UITooltip(this, "Play Singleplayer using Almura 2", 5));
            
            liveServerButton.setText(ChatColor.WHITE + "Logon to " + ChatColor.AQUA + "Almura Test" + ChatColor.WHITE + " Server");
            liveServerButton.setSize(180, 15);
            liveServerButton.setTooltip(new UITooltip(this, "Logon to Almura 2 Test Server", 5));
            liveServerButton.setName("BTNLIVESERVER");
            
            optionsButton.setText("Options");
            optionsButton.setSize(50, 15);
            optionsButton.setName("BTNOPTIONS");
        }
    }

    public void animate() {
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

    private void randomBackground() {
        String qualifier = getTime().toLowerCase();
        GuiTexture background = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/background/" + qualifier + "/" + qualifier + imageNum + ".jpg"));
        backgroundImage.setIcon(background, null);
        backgroundImage.setZIndex(-1);
        backgroundImage.setSize(0, 0);
        backgroundImage.setPosition(0, 0, Anchor.TOP | Anchor.LEFT);
    }
}

