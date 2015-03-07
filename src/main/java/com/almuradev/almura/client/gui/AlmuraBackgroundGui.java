/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui;

import com.almuradev.almura.client.gui.components.UIBackground;
import com.almuradev.almurasdk.client.gui.SimpleGui;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.SizeTransform;
import net.minecraft.client.Minecraft;

public abstract class AlmuraBackgroundGui extends SimpleGui {

    protected final UIBackground background = new UIBackground(this);

    public AlmuraBackgroundGui(SimpleGui parent) {
        super(parent);

        background.register(this);
        addToScreen(background);
        animate(background.animation);
    }


    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height) {
        if (this.width != width || this.height != height) {
            background.animation =
                    new Animation(background,
                            new SizeTransform((int) (width * UIBackground.ZOOM_LEVEL), (int) (height * UIBackground.ZOOM_LEVEL), width, height)
                                    .forTicks(UIBackground.ANIMATION_SPEED));
            animate(background.animation);
        }
        super.setWorldAndResolution(minecraft, width, height);
    }
}
