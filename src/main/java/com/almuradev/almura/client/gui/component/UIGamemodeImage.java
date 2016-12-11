package com.almuradev.almura.client.gui.component;

import com.almuradev.almura.client.gui.GuiConstants;
import com.almuradev.almura.client.gui.screen.ingame.hud.HUDData;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;

public class UIGamemodeImage extends UIImage {

    public UIGamemodeImage(MalisisGui gui) {
        super(gui, null, GuiConstants.ICON_ORB_BROWN);
    }


    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        renderer.bindTexture(GuiConstants.TEXTURE_SPRITESHEET);
        switch (HUDData.GAME_TYPE) {
            case ADVENTURE:
                setIcon(GuiConstants.ICON_ORB_BLUE);
                break;
            case CREATIVE:
                setIcon(GuiConstants.ICON_ORB_GREEN);
                break;
            case SPECTATOR:
                setIcon(GuiConstants.ICON_ORB_GRAY);
                break;
            default:
                setIcon(GuiConstants.ICON_ORB_BROWN);
        }

        super.drawForeground(renderer, mouseX, mouseY, partialTick);
    }
}
