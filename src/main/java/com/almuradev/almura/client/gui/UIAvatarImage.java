/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UIAvatarImage extends UIImage {

    private static final int FACE_X_BASE = 8;
    private static final int FACE_Y_BASE = 8;
    private static final int FACE_X_OVERLAY = 40;
    private static final int FACE_Y_OVERLAY = 8;
    private static final int FACE_WIDTH = 8;
    private static final int FACE_HEIGHT = 8;
    private final NetworkPlayerInfo player;

    public UIAvatarImage(MalisisGui gui, NetworkPlayerInfo player) {
        super(gui, null, null);
        this.player = player;
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        final GuiTexture texture = new GuiTexture(this.player.getLocationSkin(), 64, 64);

        // Draw base face
        ((GuiIconProvider) this.iconProvider).setIcon(texture.createIcon(FACE_X_BASE, FACE_Y_BASE, FACE_WIDTH, FACE_HEIGHT));
        renderer.bindTexture(texture);
        renderer.drawShape(this.shape, this.rp);

        // Reset shape
        this.shape.resetState();
        this.shape.setSize(this.width, this.height);
        this.shape.setPosition(0, 0);

        // Draw overlay face
        ((GuiIconProvider) this.iconProvider).setIcon(texture.createIcon(FACE_X_OVERLAY, FACE_Y_OVERLAY, FACE_WIDTH, FACE_HEIGHT));
        renderer.drawShape(this.shape, this.rp);
    }
}
