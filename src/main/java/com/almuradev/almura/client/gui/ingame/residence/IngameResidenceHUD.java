package com.almuradev.almura.client.gui.ingame.residence;

import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.gui.AlmuraGui;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

public class IngameResidenceHUD extends AlmuraGui {
    public static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    public IngameResidenceHUD() {
        super(null);
        setup();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void setup() {
        guiscreenBackground = false;

        // TODO Create GUI Here
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        // TODO Need Config Option soley for Residence
        if (!Configuration.DISPLAY_ENHANCED_GUI || !ResidenceData.WITHIN_RESIDENCE) {
            return;
        }

        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            setWorldAndResolution(MINECRAFT, event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
        }
    }

    public void refreshFromData() {
        // TODO Set values from ResidenceData here
    }
}
