/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import com.almuradev.almura.shared.client.ui.UIConstants;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class UITextBox extends UITextField {

    @Nullable private Consumer<UITextBox> onEnter;
    private boolean acceptsTab = true;
    private boolean acceptsReturn = true;
    private int tabIndex = 0;

    public UITextBox(SimpleScreen screen, String text) {
        this(screen, text, false);
    }

    public UITextBox(SimpleScreen screen, boolean multiLine) {
        this(screen, "", multiLine);
    }

    public UITextBox(SimpleScreen screen, String text, boolean multiLine) {
        super(screen, text, multiLine);
        this.setFontOptions(UIConstants.DEFAULT_TEXTBOX_FO);
    }

    @Override
    public boolean onKeyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_RETURN && (!this.multiLine || !this.acceptsReturn)) {
            if (this.onEnter != null) {
                this.onEnter.accept(this);
            }
            return false;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.closeDeep(this);
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
    public UITextField setEditable(final boolean editable) {
        this.setFontOptions(editable ? UIConstants.DEFAULT_TEXTBOX_FO : UIConstants.READ_ONLY_TEXTBOX_FO);
        return super.setEditable(editable);
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
     * Wrap the selected text in the provided values
     * @param prefix The prefix
     * @param suffix The suffix
     */
    public void wrap(String prefix, String suffix) {
        if (this.getSelectedText().isEmpty()) {
            return;
        }

        final StringBuilder oldText = this.text;
        final String oldValue = this.text.toString();
        final boolean cursorIsStart = this.cursorPosition.getPosition() < this.selectionPosition.getPosition();
        final CursorPosition start = cursorIsStart ? this.cursorPosition : this.selectionPosition;
        final CursorPosition end = start == this.cursorPosition ? this.selectionPosition : this.cursorPosition;
        String newValue = oldText.insert(start.getPosition(), prefix).insert(end.getPosition() + prefix.length(), suffix).toString();

        if (this.filterFunction != null) {
            newValue = this.filterFunction.apply(newValue);
        }

        if (!fireEvent(new ComponentEvent.ValueChange<>(this, oldValue, newValue))) {
            return;
        }

        this.text = new StringBuilder(newValue);
        buildLines();
        this.cursorPosition.jumpBy(cursorIsStart ? suffix.length() : prefix.length());
        this.selectionPosition.jumpBy(cursorIsStart ? suffix.length() : prefix.length());
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

    private void closeDeep(final UIComponent component) {
        if (component.getParent() instanceof UIForm) {
            ((UIForm) component.getParent()).onClose();
            return;
        }

        if (component.getParent() != null) {
            this.closeDeep(component.getParent());
        }
    }
}
