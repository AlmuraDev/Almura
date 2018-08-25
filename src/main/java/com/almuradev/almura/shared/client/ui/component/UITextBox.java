/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import net.malisis.core.client.gui.component.interaction.UITextField;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class UITextBox extends UITextField {

    @Nullable private Consumer<UITextBox> onEnter;
    private boolean acceptsTab, acceptsReturn;
    private int tabIndex = 0;

    public UITextBox(SimpleScreen screen, String text) {
        this(screen, text, false);
    }

    public UITextBox(SimpleScreen screen, boolean multiLine) {
        this(screen, "", multiLine);
    }

    public UITextBox(SimpleScreen screen, String text, boolean multiLine) {
        super(screen, text, multiLine);
    }

    /**
     * Focuses on the {@link UITextBox}
     *
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
     *
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
     *
     * @return The textbox
     */
    public UITextBox deselectAll() {
        this.selectingText = false;
        this.selectionPosition.jumpTo(0);
        this.cursorPosition.jumpTo(this.text.length());

        return this;
    }

    /**
     * Get whether or not this {@link UITextBox} allows ENTER input, only relevant when multiline.
     *
     * @return True if ENTER is accepted, otherwise false.
     */
    public boolean getAcceptsReturn() {
        return this.acceptsReturn;
    }

    /**
     * Sets whether or not this {@link UITextBox} allows ENTER input, only relevant when multipline.
     *
     * @param acceptsReturn True if ENTER is accepted, otherwise false.
     * @return The textbox
     */
    public UITextBox setAcceptsReturn(boolean acceptsReturn) {
        this.acceptsReturn = acceptsReturn;
        return this;
    }

    /**
     * Get whether or not this {@link UITextBox} allows TAB input.
     *
     * @return True if TAB is accepted, otherwise false.
     */
    public boolean getAcceptsTab() {
        return this.acceptsTab;
    }

    @Override
    public boolean onKeyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_RETURN && (!this.multiLine || !this.acceptsReturn)) {
            if (this.onEnter != null) {
                this.onEnter.accept(this);
            }
            return false;
        }

        if (keyCode == Keyboard.KEY_TAB && !this.acceptsTab) {
            if (this.parent instanceof UIContainer) {
                if (SimpleScreen.isShiftKeyDown()) {
                    // Tab backwards
                    ((UIContainer) this.parent).tabToLastControl();
                } else {
                    // Tab forwards
                    ((UIContainer) this.parent).tabToNextControl();
                }
                return false;
            }
        }

        return super.onKeyTyped(keyChar, keyCode);
    }

    @Override
    public void setHovered(boolean hovered) {
        super.setHovered(hovered);
    }

    /**
     * Sets whether or not this {@link UITextBox} allows TAB input.
     *
     * @param acceptsTab True if TAB is accepted, otherwise false.
     * @return The textbox
     */
    public UITextBox setAcceptsTab(boolean acceptsTab) {
        this.acceptsTab = acceptsTab;
        return this;
    }

    /**
     * Gets the index used for tab order. If multiple controls have the same index then the first found will be used.
     *
     * @return The index
     */
    public int getTabIndex() {
        return this.tabIndex;
    }

    /**
     * Sets the index used for tab order. If multiple controls have the same index then the first found will be used.
     *
     * @param tabIndex The index
     * @return The textbox
     */
    public UITextBox setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
        return this;
    }

    /**
     * Sets the consumer used when using ENTER (only used if {@link UITextBox#getAcceptsReturn()} returns false)
     *
     * @param onEnter The consumer
     * @return The textbox
     */
    public UITextBox setOnEnter(Consumer<UITextBox> onEnter) {
        this.onEnter = onEnter;
        return this;
    }
}
