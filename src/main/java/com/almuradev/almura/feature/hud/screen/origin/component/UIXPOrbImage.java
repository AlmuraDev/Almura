/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.GuiTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The experience orb, tinted green.
 */
@SideOnly(Side.CLIENT)
public class UIXPOrbImage extends UIImage {

    private static final GuiTexture TEXTURE = new GuiTexture(new ResourceLocation("textures/entity/experience_orb.png"), 64, 64);
    private static final GuiIcon ICON = new GuiIcon(TEXTURE, 4, 4, 8, 8);

    public UIXPOrbImage(MalisisGui gui) {
        super(ICON);
        // The image's shape uses the component color
        this.setColor(org.spongepowered.api.util.Color.ofRgb(0, 150, 0).getRgb());
    }
}
