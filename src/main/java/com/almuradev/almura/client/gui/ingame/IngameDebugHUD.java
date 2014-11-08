package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ChatColor;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class IngameDebugHUD extends MalisisGui {
    
    private final UIBackgroundContainer debugPanel;    
    public UILabel debugTitle, fpsLabel, fps, rendererLabel, rendererDebug, memoryLabel, memoryDebug;
    public boolean renderDebugGUI;
    
    public IngameDebugHUD() {
       
        guiscreenBackground = false;
        // Construct Hud with all elements
        debugPanel = new UIBackgroundContainer(this);
        debugPanel.setSize(300, 90);
        debugPanel.setPosition(5, 0, Anchor.LEFT | Anchor.MIDDLE);
        debugPanel.setColor(Integer.MIN_VALUE);
        debugPanel.setTopAlpha(180);
        debugPanel.setBottomAlpha(180);

        debugTitle = new UILabel(this, ChatColor.AQUA + "Almura Debug");
        debugTitle.setPosition(0, 3, Anchor.CENTER | Anchor.TOP);
        debugTitle.setFontScale(1.1F);
        
        fpsLabel = new UILabel(this, ChatColor.WHITE + "FPS: ");
        fpsLabel.setPosition(5,20, Anchor.LEFT | Anchor.TOP);
        
        fps = new UILabel(this, "DebugHere");
        fps.setPosition(32,20, Anchor.LEFT | Anchor.TOP);
        
        rendererLabel = new UILabel(this, ChatColor.WHITE + "Renderer:");
        rendererLabel.setPosition(5,35, Anchor.LEFT | Anchor.TOP);
        
        rendererDebug = new UILabel(this, ChatColor.GRAY + "debug");
        rendererDebug.setPosition(60, 35, Anchor.LEFT | Anchor.TOP);
        
        memoryLabel = new UILabel(this, ChatColor.WHITE + "Memory: ");
        memoryLabel.setPosition(5,50, Anchor.LEFT | Anchor.TOP);
        
        memoryDebug = new UILabel(this, ChatColor.GRAY + "memoryDebug");
        memoryDebug.setPosition(60, 50, Anchor.LEFT | Anchor.TOP);
        
        debugPanel.add(debugTitle);
        debugPanel.add(fpsLabel);
        debugPanel.add(fps);
        debugPanel.add(rendererLabel);
        debugPanel.add(rendererDebug);
        debugPanel.add(memoryLabel);
        debugPanel.add(memoryDebug);
        
        addToScreen(debugPanel);
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
        updateWidgets();
        if (Almura.showDebugGUI) {
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
        }
    }
    
    public void updateWidgets() {
        long var41 = Runtime.getRuntime().maxMemory();
        long var34 = Runtime.getRuntime().totalMemory();
        long var42 = Runtime.getRuntime().freeMemory();
        long var43 = var34 - var42;
        
        String displayFps = mc.debug.split("fps")[0];
        fps.setText(ChatColor.GOLD + displayFps);
        rendererDebug.setText(ChatColor.GRAY + mc.debugInfoRenders());
        memoryDebug.setText(ChatColor.GRAY + "Used: " + var43 * 100L / var41 + "% (" + var43 / 1024L / 1024L + "MB) of " + var41 / 1024L / 1024L + "MB");
    }
}
