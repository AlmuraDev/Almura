/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.almuradev.almura.client.gui.AlmuraMainMenu;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UIButton.ClickEvent;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;

public class IngameConfig extends AlmuraGui {

    private static final Random RANDOM = new Random();
    private static final Map<Integer, GuiTexture> BACKGROUNDS = Maps.newHashMap();
    private static int imageNum = 0;
    public UIButton saveButton, closeButton;
    public UIBackgroundContainer window;

    public int screenH, screenW;
    public UIImage backgroundImage;
    public int tick = 1;
    int timer = 1;

    public IngameConfig() {
        guiscreenBackground = false; // prevent full screen black background.
        screenH = Minecraft.getMinecraft().displayHeight;
        screenW = Minecraft.getMinecraft().displayWidth;

        //Main Container
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

        // Construct Hud with all elements
        final UIBackgroundContainer configPanel = new UIBackgroundContainer(this);
        configPanel.setSize(285, 175);
        configPanel.setPosition(5, 0, Anchor.CENTER | Anchor.MIDDLE);
        configPanel.setColor(Integer.MIN_VALUE);
        configPanel.setTopAlpha(180);
        configPanel.setBottomAlpha(180);
        configPanel.setClipContent(false);

        // Title
        UILabel configTitle = new UILabel(this, ChatColor.AQUA + "Almura Client Configuration");
        configTitle.setPosition(0, 3, Anchor.CENTER | Anchor.TOP);
        configTitle.setFontScale(1.1F);

        UICheckBox showAlmuraGUI = new UICheckBox(this, ChatColor.WHITE + "  Use Almura 2.0 GUI");
        showAlmuraGUI.setPosition(0, 20, Anchor.CENTER | Anchor.TOP);
        showAlmuraGUI.setChecked(Configuration.DISPLAY_ENHANCED_GUI);

        saveButton = (new UIButton(this, ChatColor.WHITE + "Save").setPosition(-45, -10, Anchor.RIGHT | Anchor.BOTTOM).register(this));
        saveButton.setSize(40, 15);
        saveButton.setName("BTNSAVE");

        closeButton = (new UIButton(this, ChatColor.WHITE + "Cancel").setPosition(0, -10, Anchor.RIGHT | Anchor.BOTTOM).register(this));
        closeButton.setSize(40, 15);
        closeButton.setName("BTNCLOSE");

        configPanel.add(configTitle);
        configPanel.add(showAlmuraGUI);
        configPanel.add(saveButton);
        configPanel.add(closeButton);

        new UIMoveHandle(this, configPanel);

        if (Minecraft.getMinecraft().thePlayer == null) {
            main.add(backgroundImage, configPanel);
            addToScreen(main);
        } else {
            addToScreen(configPanel);
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (ClientProxy.BINDING_CONFIG_GUI.getKeyCode() == keyCode) {
            close();
        }
    }

    @Subscribe
    public void onButtonClick(ClickEvent event) {
        switch (event.getComponent().getName().toUpperCase()) {
            case "BTNCLOSE":
                if (Minecraft.getMinecraft().thePlayer == null) {
                    this.mc.displayGuiScreen(new AlmuraMainMenu());
                }
                this.close();
                break;
            case "BTNSAVE":
                //TODO: Save
                this.close();
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Minecraft.getMinecraft().thePlayer == null) {
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
        }
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
