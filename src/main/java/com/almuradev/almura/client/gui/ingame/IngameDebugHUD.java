package com.almuradev.almura.client.gui.ingame;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class IngameDebugHUD extends MalisisGui {

    public UIWindow window;
    
    public IngameDebugHUD() {
        UIContainer main = new UIContainer(this);
        
        window = new UIWindow(this, "Almura Debug", 200, 200).setPosition(0, -10, Anchor.CENTER | Anchor.MIDDLE);
        window.setClipContent(false);
        window.setTitle("Almura Debug");
        main.add(window);

        new UIMoveHandle(this, window);

        addToScreen(main);
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

        if (event.type == RenderGameOverlayEvent.ElementType.DEBUG) {
            //updateWidgets();
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
        }
    }
}
