/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.components;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.almuradev.almurasdk.FileSystem;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.SizeTransform;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UIBackground extends UIImage {

    public static final float ZOOM_LEVEL = 1.5f;
    public static final int ANIMATION_SPEED = 160;
    private static final Map<TimeState, List<GuiTexture>> STATE_TEXTURES = Maps.newEnumMap(TimeState.class);
    private static final Random RANDOM = new Random();
    private final MalisisGui gui;
    public Animation animation;

    static {
        for (TimeState state : TimeState.values()) {
            final Path statePath = Paths.get(Filesystem.CONFIG_BACKGROUNDS_PATH.toString(), state.toString());
            List<GuiTexture> backgrounds = STATE_TEXTURES.get(state);
            if (backgrounds == null) {
                backgrounds = Lists.newArrayList();
                STATE_TEXTURES.put(state, backgrounds);
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(statePath, FileSystem.FILTER_IMAGE_FILES_ONLY)) {
                for (Path path : stream) {
                    if (Configuration.DEBUG_ALL) {
                        Almura.LOGGER.info("Filtered [" + path + "] for TimeState [" + state + "].");
                    }
                    backgrounds.add(new GuiTexture(FileSystem.registerTexture(Almura.MOD_ID, path.toString(), path)));
                }
            } catch (IOException e) {
                if (Configuration.DEBUG_ALL) {
                    Almura.LOGGER.warn("Failed to filter background images (jpg/png) for TimeState [" + state
                            + "]. Using default texture for that TimeState.", e);
                } else {
                    Almura.LOGGER.warn("Failed to filter background images (jpg/png) for TimeState [" + state
                            + "]. Using default texture for that TimeState.");
                }
            }
        }
    }

    private int currentAnchor;

    public UIBackground(MalisisGui gui) {
        super(gui, getRandomBackgroundTexture(), null);
        this.gui = gui;
        this.setSize(UIComponent.INHERITED, UIComponent.INHERITED);
        animation =
                new Animation(this, new SizeTransform((int) (gui.width * ZOOM_LEVEL), (int) (gui.height * ZOOM_LEVEL), gui.width, gui.height)
                        .forTicks(ANIMATION_SPEED));
    }

    /**
     * Gets a random anchor value.
     *
     * @return a random anchor value.
     */
    private static int getRandomAnchor() {
        final int anchor;
        switch (getRandomValue(1, 4)) {
            case 1:
                anchor = Anchor.BOTTOM | Anchor.RIGHT;
                break;
            case 2:
                anchor = Anchor.TOP | Anchor.RIGHT;
                break;
            case 3:
                anchor = Anchor.BOTTOM | Anchor.LEFT;
                break;
            default:
                anchor = Anchor.TOP | Anchor.LEFT;
                break;
        }
        return anchor;
    }

    /**
     * Gets the {@link TimeState} based on the time of day from the local calendar instance.
     *
     * @return the time state.
     */
    private static TimeState getTimeState() {
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

    /**
     * Gets a random background as a {@link GuiTexture}.
     *
     * @return a random background.
     */
    private static GuiTexture getRandomBackgroundTexture() {
        final TimeState state = getTimeState();
        final List<GuiTexture> backgrounds = STATE_TEXTURES.get(state);
        if (backgrounds == null) {
            return null;
        }
        if (backgrounds.isEmpty()) {
            return null;
        }
        final int random = getRandomValue(1, STATE_TEXTURES.get(state).size()) - 1;
        return STATE_TEXTURES.get(state).get(random);
    }

    /**
     * Gets a random value from the passed in range.
     *
     * @param min the minimum value to return.
     * @param max the maximum value to return.
     * @return a random value between the min and max.
     */
    private static int getRandomValue(int min, int max) {
        return RANDOM.nextInt((max - min) + 1) + 1;
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        // Check if the animation is finished
        if (animation.isFinished()) {
            // Get a random background and ensure it isn't the same as the current one so long as there is more than one background image
            GuiTexture texture = getRandomBackgroundTexture();
            while (texture == getTexture() && STATE_TEXTURES.get(getTimeState()).size() > 1) {
                texture = getRandomBackgroundTexture();
            }
            setIcon(texture, null);

            // Start the animation again
            animate();
        }
        super.drawForeground(renderer, mouseX, mouseY, partialTick);
    }

    public void animate() {
        // Get a random anchor
        int anchor = getRandomAnchor();

        // Ensure it isn't the same anchor as the current one
        while (anchor == currentAnchor) {
            anchor = getRandomAnchor();
        }
        currentAnchor = anchor;

        // Set the position based on the new anchor
        setPosition(0, 0, currentAnchor);

        // Create a new animation based on the current gui width and height
        animation =
                new Animation(this, new SizeTransform((int) (gui.width * ZOOM_LEVEL), (int) (gui.height * ZOOM_LEVEL), gui.width, gui.height)
                        .forTicks(ANIMATION_SPEED));

        // Start the animation
        getGui().animate(animation);
    }

    private enum TimeState {
        MORNING,
        DAY,
        EVENING,
        NIGHT;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
