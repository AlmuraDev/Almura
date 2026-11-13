/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.Almura;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;

/**
 * One of the HUD's bars, as 1.12's UIPropertyBar drew it: an icon (health's heart, hunger's drumstick...), a dark track filled in the bar's
 * colour, and the value over it ("17.5/20").
 */
final class HudBar {

    private static final SpriteId WHITE = SpriteId.of(Almura.ID, "theme/white");
    private static final float ICON = 9f, HEIGHT = 9f, ICON_GAP = 2f, TRACK_HEIGHT = 7f;
    private static final int TRACK = 0xB4000000;
    private static final DecimalFormat NUMBER = new DecimalFormat("0.##");

    private final AnchorPanel panel;
    private final Image fill;
    private final Label value;
    private final float trackWidth;

    /**
     * @param back the icon behind (an empty heart), or null for none
     * @param front the icon over it (a full heart), or null for none
     */
    HudBar(final ComponentFactory factory, final float width, final int color, final @Nullable SpriteId back, final @Nullable SpriteId front) {
        final boolean icons = back != null || front != null;
        final float trackX = icons ? ICON + ICON_GAP : 0f;
        this.trackWidth = width - trackX;

        final Image track = factory.image(WHITE).setTint(TRACK).setScaling(Image.Scaling.STRETCH);
        track.setSize(this.trackWidth, TRACK_HEIGHT);
        this.fill = factory.image(WHITE).setTint(color).setScaling(Image.Scaling.STRETCH);
        this.fill.setSize(this.trackWidth - 2f, TRACK_HEIGHT - 2f);
        this.value = factory.label("").setTextScale(0.8f);

        this.panel = factory.anchorPanel();
        this.panel.setSize(width, HEIGHT);
        if (back != null) {
            this.panel.add(icon(factory, back));
        }
        if (front != null) {
            this.panel.add(icon(factory, front));
        }
        this.panel.add(track.setAnchor(Anchor.MIDDLE_LEFT).setMargin(trackX, 0f, 0f, 0f));
        this.panel.add(this.fill.setAnchor(Anchor.MIDDLE_LEFT).setMargin(trackX + 1f, 0f, 0f, 0f));
        // Centred across the whole bar, icon and all, as on 1.12
        this.panel.add(this.value.setAnchor(Anchor.CENTER));
    }

    private static Image icon(final ComponentFactory factory, final SpriteId sprite) {
        final Image icon = factory.image(sprite).setImageSize(ICON, ICON).setScaling(Image.Scaling.FIT);
        icon.setSize(ICON, ICON);
        icon.setAnchor(Anchor.MIDDLE_LEFT);
        return icon;
    }

    AnchorPanel component() {
        return this.panel;
    }

    /**
     * Shows a value out of its most: the track filled that far, and the numbers over it.
     */
    void set(final float value, final float most) {
        final float amount = most <= 0f ? 0f : Math.max(0f, Math.min(1f, value / most));
        this.fill.setWidth(Math.max(0f, (this.trackWidth - 2f) * amount));
        this.fill.setVisible(amount > 0f);
        this.value.setText(Text.of(NUMBER.format(value) + "/" + NUMBER.format(most)).color(0xFFFFFFFF));
    }

    void setVisible(final boolean visible) {
        this.panel.setVisible(visible);
    }
}
