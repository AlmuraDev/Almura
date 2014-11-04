package com.almuradev.almura.client.gui;

import com.almuradev.almura.Almura;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraft.util.ResourceLocation;

public class AlmuraGameUI extends MalisisGui{

    private Minecraft mc;
    public UIWindow window;
    public UIButton singlePlayerButton, multiPlayerButton, optionsButton, logonButton, closeButton;
    public UIImage almuraMan;
    
    public AlmuraGameUI(Minecraft minecraft) {
        super();
        this.mc = mc;
    }
    
    @ForgeSubscribe(priority = EventPriority.NORMAL)
    public void onRender() {
        UIContainer gameWindow = new UIContainer(this);
        
        GuiTexture par1 = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(),"textures/gui/barwidget.png"));
        UIImage healthBarImage = new UIImage(this, par1, null);
        healthBarImage.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);
        
        gameWindow.add(healthBarImage); 
    }
}
