package com.almuradev.almura.client.gui;

import com.almuradev.almura.Almura;
import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UIButton.ClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class AlmuraMainMenu extends MalisisGui {

    public UIBackgroundContainer window;
    public UIButton singlePlayerButton, optionsButton, liveServerButton, devServerButton, closeButton;
    public UIImage almuraMan;
    public int tick = 1;
    static final int PAN_TIME = 600;
    static final int EXTRA_PAN_TIME = 150;
    static final int HEIGHT_PERCENT = 70;
    static final int WIDTH_PERCENT = 75;
    final Random rand = new Random();
    int maxPanTime = PAN_TIME;
    int panTime = PAN_TIME;
    int picture = -1;
    boolean zoomIn = false;

    public AlmuraMainMenu(String title, String message) {

        // Main Container
        @SuppressWarnings("rawtypes")
        UIContainer main = new UIContainer(this);

        // Container background image.
        GuiTexture background = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/background/background_1.jpg"));
        UIImage backgroundImage = new UIImage(this, background, null);
        backgroundImage.setZIndex(-1);
        backgroundImage.setSize(0, 0);

        // Buttons Window.
        window = new UIBackgroundContainer(this);
        window.setSize(300, 175);
        window.setPosition(5, 0, Anchor.CENTER | Anchor.MIDDLE);
        window.setColor(Integer.MIN_VALUE);
        window.setTopAlpha(180);
        window.setBottomAlpha(180);

        //Message contents
        @SuppressWarnings("rawtypes")
        UIContainer panel = new UIContainer(this, window.getWidth() - 41, window.getHeight() - 73);
        panel.setPosition(0, 10, Anchor.CENTER | Anchor.TOP);

        GuiTexture almuraGuy = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/background/almuraman.jpg"));
        almuraMan = new UIImage(this, almuraGuy, null);
        almuraMan.setSize(panel.getWidth(), panel.getHeight());
        panel.add(almuraMan);

        singlePlayerButton = (new UIButton(this, "Singleplayer").setSize(8, 18).setPosition(-60, -45, Anchor.BOTTOM | Anchor.CENTER).register(this));
        singlePlayerButton.setSize(60, 10);        
        singlePlayerButton.setName("singlePlayerButton");
        singlePlayerButton.setVisible(true);

        devServerButton = (new UIButton(this, "Logon to Dev Server").setPosition(0, -30, Anchor.BOTTOM | Anchor.CENTER).register(this));
        devServerButton.setSize(60, 18);
        devServerButton.setName("devServerButton");
        devServerButton.setVisible(false);

        liveServerButton = (new UIButton(this, "Logon to Live Server").setPosition(0, -20, Anchor.CENTER | Anchor.BOTTOM).register(this));
        liveServerButton.setSize(60, 20);
        liveServerButton.setTooltip(new UITooltip(this, "Logon to Almura 2.0 Server"));
        liveServerButton.setName("liveServerButton");

        optionsButton = (new UIButton(this, "Options").setPosition(-30, 0, Anchor.CENTER | Anchor.BOTTOM).register(this));
        optionsButton.setSize(50, 16);
        optionsButton.setName("optionsButton");

        closeButton =
                (new UIButton(this, "Quit").setPosition(30, 0, Anchor.CENTER | Anchor.BOTTOM)
                         .register(this));//.setAnchor(Anchor.RIGHT - 25 | Anchor.BOTTOM - 25).register(this));
        closeButton.setSize(50, 16);
        closeButton.setName("closeButton");

        main.add(backgroundImage);
        window.add(optionsButton);
        window.add(singlePlayerButton);
        window.add(devServerButton);
        window.add(liveServerButton);
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
        renderer.enableBlending();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void animate() {

        int adjustedX = ((100 - HEIGHT_PERCENT) / 2) * almuraMan.getHeight() * panTime;
        adjustedX /= maxPanTime;
        adjustedX /= 100;

        int adjustedY = ((100 - WIDTH_PERCENT) / 2) * almuraMan.getWidth() * panTime;
        adjustedY /= maxPanTime;
        adjustedY /= 100;

        int adjustedHeight = almuraMan.getHeight() - adjustedX;
        int adjustedWidth = almuraMan.getWidth() - adjustedY;
        //GL11.glScaled(super.width / (adjustedWidth - adjustedX), super.height / (adjustedHeight - adjustedY), 1D);
        //GL11.glTranslatef(-adjustedX, -adjustedY, 0F);
        almuraMan.setPosition(adjustedWidth, adjustedHeight);
        if (zoomIn && panTime < maxPanTime) {
            panTime++;
        } else if (!zoomIn && panTime > 0) {
            panTime--;
        } else {
            //cycleBackground();
        }
    }
}

