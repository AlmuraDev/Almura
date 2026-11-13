/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.GuiTexture;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * The face of a player's skin, with its overlay (hat) layer.
 */
@SideOnly(Side.CLIENT)
public class UIAvatarImage extends UIComponent {

    private static final int FACE_X_BASE = 8;
    private static final int FACE_Y_BASE = 8;
    private static final int FACE_X_OVERLAY = 40;
    private static final int FACE_Y_OVERLAY = 8;
    private static final int FACE_WIDTH = 8;
    private static final int FACE_HEIGHT = 8;
    @Nullable private NetworkPlayerInfo playerInfo;
    // The face icons of the last skin drawn
    @Nullable private ResourceLocation skin;
    @Nullable private GuiIcon baseIcon, overlayIcon;

    public UIAvatarImage(MalisisGui gui, @Nullable NetworkPlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
        this.setSize(16, 16);

        final GuiShape base = GuiShape.builder(this).icon(() -> this.baseIcon).build();
        final GuiShape overlay = GuiShape.builder(this).icon(() -> this.overlayIcon).build();
        this.setForeground(renderer -> {
            if (this.playerInfo == null) {
                return;
            }
            this.updateIcons(this.playerInfo.getLocationSkin());
            base.render(renderer);
            overlay.render(renderer);
        });
    }

    @Nullable
    public NetworkPlayerInfo getPlayerInfo() {
        return this.playerInfo;
    }

    public void setPlayerInfo(@Nullable NetworkPlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    private void updateIcons(final ResourceLocation skin) {
        if (Objects.equals(skin, this.skin)) {
            return;
        }
        this.skin = skin;
        final GuiTexture texture = new GuiTexture(skin, 64, 64);
        this.baseIcon = new GuiIcon(texture, FACE_X_BASE, FACE_Y_BASE, FACE_WIDTH, FACE_HEIGHT);
        this.overlayIcon = new GuiIcon(texture, FACE_X_OVERLAY, FACE_Y_OVERLAY, FACE_WIDTH, FACE_HEIGHT);
    }
}
