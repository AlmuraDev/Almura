/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component;

import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.control.IControlComponent;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.util.MouseButton;

public class UIForm extends UIWindow {
    private UISimpleButton closeButton;
    private int topPadding, bottomPadding, leftPadding, rightPadding;
    private boolean closable, movable;

    public UIForm(MalisisGui gui, int width, int height) {
        super(gui, width, height);
        this.construct(gui);
    }

    public UIForm(MalisisGui gui, int width, int height, String title) {
        super(gui, title, width, height);
        this.construct(gui);
    }

    private void construct(MalisisGui gui) {
        this.setTopPadding(20);

        this.closeButton = new UISimpleButton(getGui(), "x");
        this.closeButton.setName("button.close");
        this.closeButton.setPosition(-rightPadding, -(topPadding + 3), Anchor.TOP | Anchor.RIGHT);
        this.closeButton.register(this);

        if (titleLabel != null) {
            titleLabel.setPosition(0, -topPadding, Anchor.TOP | Anchor.CENTER);
        }
    }

    public boolean isClosable() {
        return this.closable;
    }

    public UIForm setClosable(boolean closable) {
        this.closable = closable;

        if (closable) {
            add(this.closeButton);
        } else {
            this.remove(this.closeButton);
        }

        return this;
    }

    public boolean isMovable() {
        return this.movable;
    }

    public UIForm setMovable(boolean movable) {
        this.movable = movable;
        return this;
    }

    public int getTopPadding() {
        return this.topPadding;
    }

    public UIForm setTopPadding(int topPadding) {
        this.topPadding = topPadding;
        return this;
    }

    public int getBottomPadding() {
        return this.bottomPadding;
    }

    public UIForm setBottomPadding(int bottomPadding) {
        this.bottomPadding = bottomPadding;
        return this;
    }

    public int getLeftPadding() {
        return this.leftPadding;
    }

    public UIForm setLeftPadding(int leftPadding) {
        this.leftPadding = leftPadding;
        return this;
    }

    public int getRightPadding() {
        return this.rightPadding;
    }

    public UIForm setRightPadding(int rightPadding) {
        this.rightPadding = rightPadding;
        return this;
    }

    public UIForm setPadding(int topPadding, int bottomPadding, int leftPadding, int rightPadding) {
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
        this.leftPadding = leftPadding;
        this.rightPadding = rightPadding;
        return this;
    }

    @Override
    public boolean onDrag(int lastX, int lastY, int x, int y, MouseButton button) {
        if (!this.movable || button != MouseButton.LEFT || this.closeButton.isInsideBounds(x, y)) {
            return false;
        }

        final UIComponent<?> parentContainer = getParent();
        if (parentContainer == null) {
            return false;
        }

        final int xPos = getParent().relativeX(x) - relativeX(lastX);
        final int yPos = getParent().relativeY(y) - relativeY(lastY);

        final int targetX = Math.min(parentContainer.getWidth() - width, Math.max(xPos, 0));
        final int targetY = Math.min(parentContainer.getHeight() - height, Math.max(yPos, 0));
        setPosition(targetX, targetY, Anchor.NONE);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                if (this.closeButton.isInsideBounds(event.getX(), event.getY())) {
                    ((UIContainer) this.getParent()).remove(this);
                }
                break;
        }
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        super.drawBackground(renderer, mouseX, mouseY, partialTick);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        super.drawForeground(renderer, mouseX, mouseY, partialTick);
        this.closeButton.setFontOptions(this.closeButton.isInsideBounds(mouseX, mouseY) ?
                FontOptionsConstants.FRO_COLOR_WHITE : FontOptionsConstants.FRO_COLOR_GRAY);
    }

    @Override
    public int componentX(UIComponent<?> component) {
        int x = super.componentX(component);
        int a = Anchor.horizontal(component.getAnchor());
        if (a == Anchor.LEFT || a == Anchor.NONE) {
            x += leftPadding;
        } else if (a == Anchor.RIGHT) {
            x -= rightPadding;
        }

        if (!(component instanceof IControlComponent)) {
            x -= xOffset;
        }

        return x;
    }

    @Override
    public int componentY(UIComponent<?> component)
    {
        int y = super.componentY(component);
        int a = Anchor.vertical(component.getAnchor());
        if (a == Anchor.TOP || a == Anchor.NONE) {
            y += topPadding;
        } else if (a == Anchor.BOTTOM) {
            y -= bottomPadding;
        }

        if (!(component instanceof IControlComponent)) {
            y -= yOffset;
        }
        return y;
    }
}
