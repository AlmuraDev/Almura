/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.util.bbcode.BBString;

public class UIExpandingLabel extends UILabel {

    public UIExpandingLabel(MalisisGui gui, String text, boolean multiLine) {
        super(gui, text, multiLine);
    }

    public UIExpandingLabel(MalisisGui gui, BBString text) {
        super(gui, text);
    }

    public UIExpandingLabel(MalisisGui gui, String text) {
        super(gui, text);
    }

    public UIExpandingLabel(MalisisGui gui, boolean multiLine) {
        super(gui, multiLine);
    }

    @Override
    public int getContentWidth() {
        return this.textWidth;
    }

    @Override
    public UILabel setText(String text) {
        if (text != null && text.equals(this.text)) {
            return this;
        }

        this.text = text;
        this.bbText = null;
        calculateSize();
        if (multiLine) {
            buildLines();
        }

        return this;
    }

    @Override
    protected void calculateSize() {
        this.textWidth = 0;
        for (String line : this.text.split("\\n")) {
            int lineWidth = (int) font.getStringWidth(line, fontOptions) + 5;
            if (this.textWidth < lineWidth) {
                textWidth = lineWidth;
            }
        }
        this.textHeight = this.getContentHeight();
        setSize(textWidth, textHeight);
    }
}
