package com.almuradev.almura.client.gui.ingame;

import org.lwjgl.input.Keyboard;

import com.almuradev.almura.Almura;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class IngameDebugHUD extends MalisisGui {
    
    public UIWindow debugWindow;
    public boolean renderDebugGUI;
    
    public IngameDebugHUD() {
       
        guiscreenBackground = false;
        debugWindow = new UIWindow(this, "Almura Debug", 200, 200).setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
        debugWindow.setClipContent(false);
        debugWindow.setTitle("Almura Debug");
        
        new UIMoveHandle(this, debugWindow);

        addToScreen(debugWindow);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        renderer.enableBlending();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.DEBUG) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event) {
        setWorldAndResolution(Minecraft.getMinecraft(), event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
        //updateWidgets();
        if (Almura.showDebugGUI) {
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
        }
    }
}
