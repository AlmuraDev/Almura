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

public abstract class DynamicBackgroundGui extends SimpleGui {

    protected final UIBackground background = new UIBackground(this);

    public DynamicBackgroundGui() {
    }

    public DynamicBackgroundGui(SimpleGui parent) {
        super(parent);
    }

    @Override
    public void construct() {
        background.register(this);
        addToScreen(background);
        animate(background.animation);
    }

    @Override
    public void setResolution() {
        int prevWidth = width;
        int prevHeight = height;
        super.setResolution();

        if (prevWidth != width || prevHeight != height) {
            background.animation =
                    new Animation(background,
                            new SizeTransform((int) (width * UIBackground.ZOOM_LEVEL), (int) (height * UIBackground.ZOOM_LEVEL), width, height)
                                    .forTicks(UIBackground.ANIMATION_SPEED));
            animate(background.animation);
        }
    }
}
