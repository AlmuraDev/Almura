/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ChatColor;
import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UIButton.ClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ResourceLocation;

import java.util.Calendar;
import java.util.Random;

public class AlmuraMainMenu extends MalisisGui {

    public UIBackgroundContainer window;
    //public GuiTexture background;
    public UIButton singlePlayerButton, optionsButton, liveServerButton, devServerButton, closeButton;
    public UIImage almuraMan;
    public int screenH, screenW;
    public UIImage backgroundImage;
    public int tick = 1;
    static final int PAN_TIME = 600;
    static final int EXTRA_PAN_TIME = 150;
    static final int HEIGHT_PERCENT = 70;
    static final int WIDTH_PERCENT = 75;
    final static Random rand = new Random();
    int maxPanTime = PAN_TIME;
    int panTime = PAN_TIME;
    int picture = -1;
    int timer = 1;
    public static int imageNum = 0;
    boolean zoomIn = true;
    boolean zoomOut = false;    

    public AlmuraMainMenu() {

        guiscreenBackground = false; // prevent full screen black background.       
        screenH = Minecraft.getMinecraft().displayHeight;
        screenW = Minecraft.getMinecraft().displayWidth;
        // Main Container
        @SuppressWarnings("rawtypes")
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
        window.setTopAlpha(75);
        window.setBottomAlpha(75);

        //Message contents
        @SuppressWarnings("rawtypes")
        UIContainer panel = new UIContainer(this, 75, 100);
        panel.setPosition(0, 10, Anchor.CENTER | Anchor.TOP);

        GuiTexture almuraGuy = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/background/almura2b.png"));
        almuraMan = new UIImage(this, almuraGuy, null);
        almuraMan.setSize(panel.getWidth(), panel.getHeight());
        
        panel.add(almuraMan);

        singlePlayerButton = (new UIButton(this, ChatColor.WHITE + "Singleplayer").setPosition(0, -90, Anchor.BOTTOM | Anchor.CENTER).register(this));
        singlePlayerButton.setSize(150, 16);
        //singlePlayerButton.setTooltip(new UITooltip(this, "Play Singleplayer using Almura 2.0"));
        singlePlayerButton.setName("singlePlayerButton");        

        devServerButton = (new UIButton(this, ChatColor.WHITE + "Logon to " + ChatColor.GOLD + "Dev" + ChatColor.WHITE + " Server").setPosition(0, -70, Anchor.BOTTOM | Anchor.CENTER).register(this));
        devServerButton.setSize(150, 16);
        //devServerButton.setTooltip(new UITooltip(this, "Logon to Almura 2.0 Dev Server"));
        devServerButton.setName("devServerButton");        

        liveServerButton = (new UIButton(this, ChatColor.WHITE + "Logon to " + ChatColor.AQUA + "Live" + ChatColor.WHITE + " Server").setPosition(0, -50, Anchor.CENTER | Anchor.BOTTOM).register(this));
        liveServerButton.setSize(150, 15);
        //liveServerButton.setTooltip(new UITooltip(this, "Logon to Almura 2.0 Live Server"));
        liveServerButton.setName("liveServerButton");

        optionsButton = (new UIButton(this, ChatColor.WHITE + "Options").setPosition(-30, -20, Anchor.CENTER | Anchor.BOTTOM).register(this));
        optionsButton.setSize(50, 15);
        optionsButton.setName("optionsButton");

        closeButton = (new UIButton(this, ChatColor.WHITE + "Quit").setPosition(30, -20, Anchor.CENTER | Anchor.BOTTOM).register(this));
        closeButton.setSize(50, 15);
        closeButton.setName("closeButton");
        
        UILabel version = new UILabel(this, ChatColor.WHITE + Almura.VERSION_STRING);
        version.setPosition(0,-5, Anchor.CENTER | Anchor.BOTTOM);
        version.setFontScale(0.75F);
        
        main.add(backgroundImage);
        window.add(optionsButton);
        window.add(singlePlayerButton);
        window.add(devServerButton);
        window.add(liveServerButton);
        window.add(closeButton);
        window.add(version);

        window.add(panel);

        main.add(window);

        new UIMoveHandle(this, window);

        addToScreen(main);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {  //Every Frame

    }

    @Override
    public void updateScreen() {  //Every Tick
        
    }

    @Subscribe  //@EventHandler == Bukkit //Forge or FML, not main class == @SubscribeEvents
    public void onButtonClick(ClickEvent event) {

        if (event.getComponent().getName().equalsIgnoreCase("singlePlayerButton")) {
            this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiSelectWorld(this));
        }

        if (event.getComponent().getName().equalsIgnoreCase("devServerButton")) {
            this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, new ServerData("DevServer", "localhost")));
        }
        
        if (event.getComponent().getName().equalsIgnoreCase("liveServerButton")) {
            this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, new ServerData("LiveServer", "localhost")));
        }

        if (event.getComponent().getName().equalsIgnoreCase("optionsButton")) {
            this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiOptions(this, this.mc.gameSettings));
        }
        
        if (event.getComponent().getName().equalsIgnoreCase("closeButton")) {
            this.mc.shutdown();
        }
        MalisisGui.currentGui().close();
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
    }

    public void animate() {
        if (tick++ % 2 == 0) {       
            backgroundImage.setSize(backgroundImage.getWidth()+1, backgroundImage.getHeight()+1);
            timer++;            
            
            if (timer == 60) {
                randomBackground();
                backgroundImage.setPosition(0, 0, Anchor.BOTTOM | Anchor.RIGHT);
                backgroundImage.setSize(0, 0);
             }
             
             if (timer ==120) {
                 randomBackground();
                 backgroundImage.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);
                 backgroundImage.setSize(0, 0);                 
             }
             
             if (timer ==180) {
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
    
    private static String getTime() {
        int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hours < 6) {
            imageNum = rand.nextInt(13 - 1)+ 1;
            return "night";
        }
        if (hours < 12) {
            imageNum = rand.nextInt(29 - 1)+ 1;
            return "day";
        }
        if (hours < 20) {
            imageNum = rand.nextInt(16 - 1)+ 1;
            return "evening";
        }
        imageNum = rand.nextInt(13 - 1)+ 1;
        return "night";
    }
    
    private void randomBackground() {       
        if (getTime().equalsIgnoreCase("night")) {
            GuiTexture background = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/background/night/night"+imageNum+".jpg"));            
            backgroundImage.setIcon(background,null);
            backgroundImage.setZIndex(-1);
            backgroundImage.setSize(0, 0);
        }

        if (getTime().equalsIgnoreCase("day")) {
            GuiTexture background = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/background/day/day"+imageNum+".jpg"));            
            backgroundImage.setIcon(background,null);
            backgroundImage.setZIndex(-1);
            backgroundImage.setSize(0, 0);
        }

        if (getTime().equalsIgnoreCase("evening")) {
            GuiTexture background = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/background/evening/evening"+imageNum+".jpg"));            
            backgroundImage.setIcon(background,null);
            backgroundImage.setZIndex(-1);
            backgroundImage.setSize(0, 0);
        }
        backgroundImage.setPosition(0, 0, Anchor.TOP | Anchor.LEFT);        
    }    
}

