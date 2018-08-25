/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import net.malisis.core.client.gui.component.interaction.UITextField;

public class UITextBox extends UITextField {

    public UITextBox(SimpleScreen screen, String text, boolean multiLine) {
        super(screen, text, multiLine);
    }

    public UITextBox(SimpleScreen screen, String text) {
        super(screen, text);
    }

    public UITextBox(SimpleScreen screen, boolean multiLine) {
        super(screen, multiLine);
    }

    /**
     * Focuses on the {@link UITextBox}
     * @return The textbox
     */
    public UITextBox focus() {
        if (!isEnabled()) {
            return this;
        }
        this.setFocused(true);
        ((SimpleScreen) getGui()).setFocusedComponent(this);

        return this;
    }

    /**
     * Selects all the text in the component
     * @return The textbox
     */
    public UITextBox selectAll() {
        this.selectingText = true;
        this.selectionPosition.jumpToBeginning();
        this.cursorPosition.jumpToEnd();

        return this;
    }

    /**
     * Deselect all the text in the component
     * @return The textbox
     */
    public UITextBox deselectAll() {
        this.selectingText = false;
        this.selectionPosition.jumpTo(0);
        this.cursorPosition.jumpTo(this.text.length());

        return this;
    }
}
