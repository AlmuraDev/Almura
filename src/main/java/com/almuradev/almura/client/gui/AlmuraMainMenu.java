package com.almuradev.almura.client.gui;

import com.almuradev.almura.Almura;
import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UIButton.ClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

public class AlmuraMainMenu extends MalisisGui{
    
    public UIWindow window;
    public UIButton singlePlayerButton, multiPlayerButton, optionsButton, logonButton, closeButton;
    public int tick = 1;
    
    public AlmuraMainMenu(String title, String message) {
        
        // Main Container
        @SuppressWarnings("rawtypes")
        UIContainer main = new UIContainer(this);
        
        // Container background image.
        GuiTexture background = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(),"textures/background/background_1.jpg"));       
        UIImage backgroundImage = new UIImage(this, background, null);
        backgroundImage.setZIndex(-1);
        backgroundImage.setSize(0,0);
        
        // Buttons Window.
        window = new UIWindow(this, title, 200, 175).setPosition(0, -10, Anchor.CENTER | Anchor.MIDDLE);
        window.setClipContent(false);        
        window.setTitle("Welcome to Almura Client!");        
        
        //Message contents
        @SuppressWarnings("rawtypes")
        UIContainer panel = new UIContainer(this, window.getWidth() - 11, window.getHeight() - 43);
        panel.setPosition(0, 10, Anchor.LEFT | Anchor.TOP);
        
        GuiTexture almuraGuy = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(),"textures/background/almuraman.jpg"));       
        UIImage almuraMan = new UIImage(this, almuraGuy, null);
        almuraMan.setSize(panel.getWidth(), panel.getHeight());
        panel.add(almuraMan);
        
        singlePlayerButton = (new UIButton(this, "Single-Player").setSize(8, 18).setPosition(0, -20, Anchor.MIDDLE | Anchor.CENTER).register(this));
        singlePlayerButton.setSize(Minecraft.getMinecraft().fontRenderer.getStringWidth(singlePlayerButton.getText() + 25),18);
        singlePlayerButton.setName("singlePlayerButton");
        
        multiPlayerButton = (new UIButton(this, "Multi-Player").setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER).register(this));
        multiPlayerButton.setSize(Minecraft.getMinecraft().fontRenderer.getStringWidth(multiPlayerButton.getText() + 25),18);        
        multiPlayerButton.setName("multiPlayerButton");
        
        logonButton = (new UIButton(this, "Logon").setPosition(0, 0, Anchor.CENTER | Anchor.BOTTOM).register(this));
        logonButton.setSize(Minecraft.getMinecraft().fontRenderer.getStringWidth(logonButton.getText() + 25),16);        
        logonButton.setName("logonButton");
        
        optionsButton = (new UIButton(this, "Options").setPosition(0, 0, Anchor.LEFT | Anchor.BOTTOM).register(this));        
        optionsButton.setSize(Minecraft.getMinecraft().fontRenderer.getStringWidth(optionsButton.getText() + 25),16);
        optionsButton.setName("optionsButton");        
        
        closeButton = (new UIButton(this, "Quit").setPosition(0, 0, Anchor.RIGHT | Anchor.BOTTOM).register(this));//.setAnchor(Anchor.RIGHT - 25 | Anchor.BOTTOM - 25).register(this));
        closeButton.setSize(Minecraft.getMinecraft().fontRenderer.getStringWidth(closeButton.getText() + 25),16);
        closeButton.setName("closeButton");
        
        //panel.add(singlePlayerButton);
        //panel.add(multiPlayerButton);        
        
        main.add(backgroundImage);
        window.add(optionsButton);
        window.add(logonButton);
        window.add(closeButton);
        
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
        
        if (event.getComponent().getName().equalsIgnoreCase("multiPlayerButton")) {
            this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiMultiplayer(this)); 
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
        renderer.enableBlending();
        super.drawScreen(mouseX, mouseY, partialTicks);        
    }
}
