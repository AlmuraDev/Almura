package com.almuradev.almura.client.gui;

import com.almuradev.almura.client.gui.menu.UIBackground;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.SizeTransform;
import net.minecraft.client.Minecraft;

public abstract class AlmuraBackgroundGui extends AlmuraGui {

    protected final UIBackground background = new UIBackground(this);

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}

     * @param parent the {@link AlmuraGui} that we came from
     */
    public AlmuraBackgroundGui(AlmuraGui parent) {
        super(parent);

        background.register(this);
        addToScreen(background);
        animate(background.animation);

        setup();
    }


    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height) {
        if (background.getTexture() != UIBackground.DEFAULT_TEXTURE && (this.width != width || this.height != height)) {
            background.animation =
                    new Animation(background,
                                  new SizeTransform((int) (width * UIBackground.ZOOM_LEVEL), (int) (height * UIBackground.ZOOM_LEVEL), width, height)
                                          .forTicks(UIBackground.ANIMATION_SPEED));
            animate(background.animation);
        }
        super.setWorldAndResolution(minecraft, width, height);
    }
}
