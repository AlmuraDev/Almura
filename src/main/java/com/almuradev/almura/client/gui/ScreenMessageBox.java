package com.almuradev.almura.client.gui;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.control.UICloseHandle;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UIButton.ClickEvent;
import net.minecraft.client.Minecraft;

public class ScreenMessageBox extends MalisisGui{
    
    public UIWindow window;
    public int tick = 1;
    
    public ScreenMessageBox(String title, String message) {
        //Title bar
        
        window = new UIWindow(this, Float.toString(Minecraft.getMinecraft().thePlayer.getHealth()), 200, 75).setPosition(0, -10, Anchor.CENTER | Anchor.MIDDLE);
        window.setClipContent(false);        
        window.setTitle("YourSuck");
        this.guiscreenBackground = false;

        //Message contents
        UIPanel panel = new UIPanel(this, window.getWidth() - 11, window.getHeight() - 43);
        panel.setPosition(0, 10, Anchor.LEFT | Anchor.TOP);
        panel.setBackgroundColor(0x7767AE);

        panel.add(new UILabel(this, message).setAnchor(Anchor.LEFT | Anchor.TOP));
        window.add(panel);
        window.add(new UIButton(this, "Close").setSize(8, 14).setAnchor(Anchor.RIGHT | Anchor.BOTTOM).register(this));

        new UIMoveHandle(this, window);

        new UICloseHandle(this, window);
        
        addToScreen(window);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
    
    @Override
    public void update(int mouseX, int mouseY, float partialTick) {  //Every Frame
        
    }
    
    @Override
    public void updateScreen() {  //Every Tick
        
    }
    
    @Subscribe  //@EventHandler == Bukkit //Forge or FML, not main class == @SubscribeEvents
    public void onButtonClick(ClickEvent event) {
        MalisisGui.currentGui().close();
    }
}
