/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.ingame.IngameConfig;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

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
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;

public class AlmuraMainMenu extends AlmuraGui {

    private static final GuiTexture
            ALMURA_2_LOGO =
            new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/background/almura2b.png"));
    private static final Random RANDOM = new Random();
    private static final Map<Integer, GuiTexture> BACKGROUNDS = Maps.newHashMap();
    private static int imageNum = 0;
    public UIBackgroundContainer window;
    public UIButton singlePlayerButton, optionsButton, modsButton, configButton, serverButton, closeButton;
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

        singlePlayerButton = (new UIButton(this, ChatColor.AQUA + "Singleplayer").setPosition(0, -105, Anchor.BOTTOM | Anchor.CENTER).register(this));
        singlePlayerButton.setSize(180, 15);
        singlePlayerButton.setTooltip(new UITooltip(this, "Play Singleplayer using Almura 2", 20));
        singlePlayerButton.setName("BTNSINGLEPLAYER");

        serverButton = (new UIButton(this, ChatColor.AQUA + "Multiplayer").setPosition(0, -85, Anchor.BOTTOM | Anchor.CENTER).register(this));
        serverButton.setSize(180, 15);
        serverButton.setTooltip(new UITooltip(this, "Access Multiplayer Server List", 20));
        serverButton.setName("BTNSERVER");

        optionsButton = (new UIButton(this, ChatColor.WHITE + "Options").setPosition(-60, -60, Anchor.CENTER | Anchor.BOTTOM).register(this));
        optionsButton.setSize(50, 15);
        optionsButton.setName("BTNOPTIONS");

        configButton = (new UIButton(this, ChatColor.WHITE + "Config").setPosition(0, -60, Anchor.CENTER | Anchor.BOTTOM).register(this));
        configButton.setSize(50, 15);
        configButton.setName("BTNCONFIG");
        
        modsButton = (new UIButton(this, ChatColor.WHITE + "Mods").setPosition(60, -60, Anchor.CENTER | Anchor.BOTTOM).register(this));
        modsButton.setSize(50, 15);
        modsButton.setName("BTNMODS");
        
        closeButton = (new UIButton(this, ChatColor.WHITE + "Quit").setPosition(0, -40, Anchor.CENTER | Anchor.BOTTOM).register(this));
        closeButton.setSize(50, 15);
        closeButton.setName("BTNCLOSE");

        final UILabel copyright1 = new UILabel(this, ChatColor.WHITE + "AlmuraDev � 2015.");
        copyright1.setPosition(0, -22, Anchor.CENTER | Anchor.BOTTOM);
        copyright1.setFontScale(0.7F);

        final UILabel copyright2 = new UILabel(this, ChatColor.WHITE + "Minecarft and all other products are ");
        copyright2.setPosition(0, -12, Anchor.CENTER | Anchor.BOTTOM);
        copyright2.setFontScale(0.7F);

        final UILabel copyright3 = new UILabel(this, ChatColor.WHITE + "trademarks of their respective holders.");
        copyright3.setPosition(0, -5, Anchor.CENTER | Anchor.BOTTOM);
        copyright3.setFontScale(0.7F);

        panel.add(almuraLogo);
        window.add(panel, singlePlayerButton, serverButton, optionsButton, modsButton, configButton, closeButton, version, copyright1, copyright2, copyright3);

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
            case "BTNSERVER":
                this.mc.displayGuiScreen(new AlmuraServerMenu());
                break;
            case "BTNOPTIONS":
                this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiOptions(this, this.mc.gameSettings));
                break;
            case "BTNMODS":
                this.mc.displayGuiScreen(new GuiModList(this));
                break;
            case "BTNCONFIG":
                this.mc.displayGuiScreen(new IngameConfig());
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
        SlotCrafting
        backgroundImage.setIcon(background, null);
        backgroundImage.setZIndex(-1);
        backgroundImage.setSize(0, 0);
        backgroundImage.setPosition(0, 0, Anchor.TOP | Anchor.LEFT);
    }
}

