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

    public void focus() {
        if (!isEnabled()) {
            return;
        }
        this.setFocused(true);
        ((SimpleScreen) getGui()).setFocusedComponent(this);
    }

    /**
     * Selects all the text in the component
     */
    public void selectAll() {
        this.selectingText = true;
        this.selectionPosition.jumpToBeginning();
        this.cursorPosition.jumpToEnd();
    }

    /**
     * Deselect all the text in the component
     */
    public void deselectAll() {
        selectingText = false;
        selectionPosition.jumpTo(0);
        cursorPosition.jumpTo(text.length());
    }
}
