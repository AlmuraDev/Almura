/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel.debug;

import com.almuradev.almura.feature.hud.screen.origin.component.panel.AbstractAutoSizingPanel;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;

public abstract class AbstractDebugPanel extends AbstractAutoSizingPanel {
    private static final int INITIAL_TEXT_Y_OFFSET = 5;

    AbstractDebugPanel(final MalisisGui gui, final int width, final int height) {
        super(gui, width, height, INITIAL_TEXT_Y_OFFSET);
    }

    @Nullable
    protected final Entity getView() {
        final Entity view = this.client.getRenderViewEntity();
        if (view == null || this.client.player == null || this.client.player.world == null || !this.client.gameSettings.showDebugInfo) {
            return null;
        }
        return view;
    }
}
