/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UIXPOrbImage extends UIImage {

    private static final GuiTexture TEXTURE = new GuiTexture(new ResourceLocation("textures/entity/experience_orb.png"), 64, 64);
    private static final Icon ICON = TEXTURE.getIcon(4, 4, 8, 8);

    public UIXPOrbImage(MalisisGui gui) {
        super(gui, TEXTURE, ICON);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        renderer.bindTexture(TEXTURE);
        ((GuiIconProvider) this.iconProvider).setIcon(ICON);
        this.rp.setColor(org.spongepowered.api.util.Color.ofRgb(0, 150, 0).getRgb());
        renderer.drawShape(this.shape, this.rp);
    }
}
