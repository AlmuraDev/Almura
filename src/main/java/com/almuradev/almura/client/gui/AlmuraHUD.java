package com.almuradev.almura.client.gui;

import com.almuradev.almura.Almura;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class AlmuraHUD extends MalisisGui {
    public AlmuraHUD() {
        guiscreenBackground = false;

        final UIContainer hudContainer = new UIContainer(this);

        GuiTexture barTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/barwidget.png"));
        UIImage healthBarImage = new UIImage(this, barTexture, null);
        healthBarImage.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);

        hudContainer.add(healthBarImage);
        addToScreen(hudContainer);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Minecraft.getMinecraft().currentScreen != null) {
            return;
        }

        renderer.enableBlending();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        //Almura HUD replaces these GUI elements of Vanilla
        switch (event.type) {
            case HEALTH:
            case FOOD:
            case ARMOR:
            case EXPERIENCE:
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
        }
    }
}
