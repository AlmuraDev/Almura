/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.components;

import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class UIForm extends UIBackgroundContainer {

    private static final int TITLE_BAR_HEIGHT = 13;
    private final UIBackgroundContainer contentContainer;
    private final DraggableBackgroundContainer titleContainer;
    private int dragX, dragY;
    private boolean dragging = false;

    /**
     * Creates a form with no title that has a close button
     *
     * @param parent The parent {@link AlmuraGui}
     * @param width The width of the form
     * @param height The height of the form
     */
    public UIForm(AlmuraGui parent, int width, int height) {
        this(parent, width, height, "", true);
    }

    /**
     * Creates a form with a title that has a close buton
     *
     * @param parent The parent {@link AlmuraGui}
     * @param width The width of the form
     * @param height The height of the form
     * @param title The title of the form
     */
    public UIForm(AlmuraGui parent, int width, int height, String title) {
        this(parent, width, height, title, true);
    }

    /**
     * Creates a form with a title that may or may not show a close button
     *
     * @param parent The parent {@link AlmuraGui}
     * @param width The width of the form
     * @param height The height of the form
     * @param title The title of the form
     * @param showCloseButton Specifies if this form has a close button
     */
    public UIForm(AlmuraGui parent, int width, int height, String title, boolean showCloseButton) {
        super(parent);

        // Setup controls
        titleContainer = new DraggableBackgroundContainer(parent, title, showCloseButton);
        contentContainer = new UIBackgroundContainer(parent);

        setSize(width, height);

        titleContainer.setSize(INHERITED, TITLE_BAR_HEIGHT);
        titleContainer.setColor(3428032);
        titleContainer.register(this);

        contentContainer.setSize(INHERITED, getHeight() - TITLE_BAR_HEIGHT);
        contentContainer.setPosition(0, TITLE_BAR_HEIGHT);
        contentContainer.setBackgroundAlpha(0);

        // Add controls
        this.add(titleContainer, contentContainer);

        // Set form properties
        setColor(Integer.MIN_VALUE);
        setBackgroundAlpha(175);
    }

    /**
     * The {@link UIBackgroundContainer} that holds all the content, all controls are added to this.
     *
     * @return The content container
     */
    public final UIBackgroundContainer getContentContainer() {
        return contentContainer;
    }

    /**
     * Gets the title of the form
     * @return The title of the form
     */
    @Override
    public String getTitle() {
        return titleLabel.getText();
    }

    /**
     * Sets the title of the form
     *
     * @param title The title to use for the form
     * @return The UIForm
     */
    @Override
    public UIForm setTitle(String title) {
        titleLabel.setText(title);
        return this;
    }

    /**
     * Sets the width of the form
     *
     * @param width The width to set the form to
     */
    public void setWidth(int width) {
        this.setSize(width, height);
    }

    /**
     * Sets the height of the form
     *
     * @param height The height to set the form to
     */
    public void setHeight(int height) {
        this.setSize(width, height);
    }

    protected void close() {
        Keyboard.enableRepeatEvents(false);
        if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.closeScreen();
        }
        Minecraft.getMinecraft().displayGuiScreen(getGui());
        Minecraft.getMinecraft().setIngameFocus();
    }

    @Override
    public UIForm setSize(int width, int height) {
        super.setSize(width, height);
        contentContainer.setSize(INHERITED, getHeight() - TITLE_BAR_HEIGHT);
        return this;
    }

    private class DraggableBackgroundContainer extends UIBackgroundContainer {

        private CloseButton titleCloseButton;

        public DraggableBackgroundContainer(AlmuraGui parent, String title, boolean showCloseButton) {
            super(parent);

            titleLabel = new UILabel(parent, ChatColor.BLACK + title);
            titleLabel.setPosition(4, 1, Anchor.LEFT | Anchor.MIDDLE);

            if (showCloseButton) {
                titleCloseButton = new CloseButton(parent);
                titleCloseButton.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
                titleCloseButton.setName("uiform.title.close");
                titleCloseButton.register(this);
                add(titleCloseButton);
            }

            add(titleLabel);
        }

        @Subscribe
        public void onPress(MouseEvent.Press event) {
            if (titleCloseButton != null && titleCloseButton.isInsideBounds(event.getX(), event.getY())) {
                dragging = false;
                return;
            }
            dragging = true;
            dragX = relativeX(event.getX());
            dragY = relativeY(event.getY());
        }

        @Subscribe
        public void onDrag(MouseEvent.Drag event) {
            // Do not drag if not the left mouse button
            if (!dragging || event.getButton() != MouseButton.LEFT) {
                return;
            }

            // Do not drag if inside the close button
            if (titleCloseButton != null && titleCloseButton.isInsideBounds(event.getX(), event.getY())) {
                return;
            }

            final UIComponent parentContainer = getParent().getParent();
            if (parentContainer == null) {
                return;
            }

            final int xPos = parentContainer.relativeX(event.getX()) - dragX;
            final int yPos = parentContainer.relativeY(event.getY()) - dragY;

            getParent().setPosition(xPos < 0 ? 0 : xPos, yPos < 0 ? 0 : yPos, Anchor.NONE);
        }

        @Subscribe
        public void onClick(UIButton.ClickEvent event) {
            switch (event.getComponent().getName().toLowerCase()) {
                case "uiform.title.close":
                    ((UIForm) this.getParent()).close();
            }
        }
    }

    private class CloseButton extends UIButton {

        public CloseButton(AlmuraGui gui) {
            super(gui, "");
            width = 23;
            height = 10;
            shape = new SimpleGuiShape();
            icon = AlmuraGui.ICON_CLOSE_NORMAL;
            iconHovered = AlmuraGui.ICON_CLOSE_HOVER;
        }
    }
}
