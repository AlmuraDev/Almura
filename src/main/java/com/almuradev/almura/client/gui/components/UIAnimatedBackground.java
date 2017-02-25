/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.components;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.util.FileSystem;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent.SizeChangeEvent;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.SizeTransform;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

public class UIAnimatedBackground extends UIComponent<UIAnimatedBackground> {

    public static final float ZOOM_LEVEL = 1.5f;
    public static final int ANIMATION_SPEED = 160;
    private static final Multimap<TimeState, GuiTexture> STATE_TEXTURES = ArrayListMultimap.create();
    private static final Random RANDOM = new Random();
    public Animation animation;
    private GuiTexture currentTexture;
    private int currentAnchor;

    public UIAnimatedBackground(MalisisGui gui) {
        super(gui);

        shape = new SimpleGuiShape();
    }

    @SuppressWarnings("rawtypes")
	@Override
    public void setParent(UIComponent parent) {
        if (parent != null) {
            parent.register(this);
        } else {
            getParent().unregister(this);
        }

        super.setParent(parent);
    }

    private GuiTexture[] listBackgrounds(TimeState state) {
        if (!STATE_TEXTURES.containsKey(state)) {
            readStateFiles(state);
        }

        return STATE_TEXTURES.get(state).toArray(new GuiTexture[0]);
    }

    private void readStateFiles(TimeState state) {
        final File stateDir = new File(FileSystem.CONFIG_BACKGROUNDS_PATH.toString(), state.toString());

        for (File file : stateDir.listFiles()) {
            if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                try {
                    STATE_TEXTURES.put(state, new GuiTexture(file));
                } catch (IOException e) {
                    Almura.LOGGER.warn("Failed to load background image {} for TimeState [{}].", file.getName(), state, Configuration.DEBUG_ALL ? e
                            : null);
                }
            }
        }
    }

    /**
     * Gets a random anchor value.
     *
     * @return a random anchor value.
     */
    private int getRandomAnchor() {
        int anchor = currentAnchor;
        while (anchor == currentAnchor) {
            anchor = (RANDOM.nextBoolean() ? Anchor.LEFT : Anchor.RIGHT) | (RANDOM.nextBoolean() ? Anchor.TOP : Anchor.BOTTOM);
        }
        return anchor;
    }

    /**
     * Gets a random background as a {@link GuiTexture}.
     *
     * @return a random background.
     */
    private GuiTexture getRandomTexture() {
        GuiTexture[] backgrounds = listBackgrounds(TimeState.currentState());
        if (backgrounds.length == 0) {
            return null;
        }

        if (backgrounds.length == 1) {
            return backgrounds[0];
        }

        GuiTexture texture = currentTexture;
        while (texture == currentTexture) {
            texture = backgrounds[RANDOM.nextInt(backgrounds.length)];
        }

        return texture;
    }

    private Animation getAnimation() {
        int w = getParent().getWidth();
        int h = getParent().getHeight();

        SizeTransform<UIAnimatedBackground> size = new SizeTransform<>(w, h, (int) (w * ZOOM_LEVEL), (int) (h * ZOOM_LEVEL));
        size.forTicks(ANIMATION_SPEED);

        animation = new Animation(this, size);
        return animation;
    }

    private void resetAnimation() {
        currentTexture = getRandomTexture();
        currentAnchor = getRandomAnchor();
        setPosition(0, 0, currentAnchor);
        getGui().animate(getAnimation());
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        // Check if the animation is finished
        if (animation == null || animation.isFinished()) {
            resetAnimation();
        }

        rp.icon.set(icon);
        renderer.bindTexture(currentTexture);
        renderer.drawShape(shape, rp);
    }

    @SuppressWarnings("rawtypes")
	@Subscribe
    public void onComponentSizeChange(SizeChangeEvent<UIComponent> event) {
        if (event.getComponent() != this) {
            resetAnimation();
        }
    }

    private enum TimeState {
        MORNING,
        DAY,
        EVENING,
        NIGHT;

        public static TimeState currentState() {
            final int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            if (hours <= 8) {
                return TimeState.MORNING;
            } else if (hours <= 15) {
                return TimeState.DAY;
            } else if (hours <= 20) {
                return TimeState.EVENING;
            } else {
                return TimeState.NIGHT;
            }
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
