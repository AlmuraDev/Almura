/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * A column of text lines that a debug panel writes again every frame: the labels are kept and reused, the ones not written this frame hidden.
 */
final class DebugLines {

    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA;

    private final ComponentFactory factory;
    private final StackPanel column;
    private final List<Label> labels = new ArrayList<>();
    private int used;

    DebugLines(final ComponentFactory factory, final float spacing) {
        this.factory = factory;
        this.column = factory.stackPanel().setSpacing(spacing);
    }

    StackPanel component() {
        return this.column;
    }

    /**
     * Starts this frame's lines.
     */
    void begin() {
        this.used = 0;
    }

    Label line(final Text text) {
        final Label label;
        if (this.used < this.labels.size()) {
            label = this.labels.get(this.used);
        } else {
            label = this.factory.label("");
            this.labels.add(label);
            this.column.add(label.setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP));
        }
        this.used++;
        label.setText(text);
        label.setVisible(true);
        return label;
    }

    /**
     * A "key: value" line, the key white and the value grey, as 1.12's debug panels wrote them.
     */
    Label property(final String key, final String value) {
        return this.line(Text.concat(Text.of(key + ": ").color(WHITE), Text.of(value).color(GRAY)));
    }

    /**
     * Ends this frame's lines, hiding the ones not written.
     */
    void end() {
        for (int i = this.used; i < this.labels.size(); i++) {
            this.labels.get(i).setVisible(false);
        }
    }

    boolean isEmpty() {
        return this.used == 0;
    }
}
