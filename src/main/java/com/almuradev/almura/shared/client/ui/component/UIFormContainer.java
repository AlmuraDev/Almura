/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.button.UISimpleButton;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.util.MouseButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UIFormContainer extends UIBackgroundContainer {
    private UISimpleButton closeButton;
    private boolean closable, movable;

    public UIFormContainer(MalisisGui gui, int width, int height) {
        this(gui, width, height, "");
    }

    public UIFormContainer(MalisisGui gui, int width, int height, String title) {
        super(gui, title, width, height);
        setColor(Integer.MIN_VALUE);
        setBackgroundAlpha(175);
        this.construct();
    }

    private void construct() {
        setTopPadding(20);

        this.closeButton = new UISimpleButton(getGui(), "x");
        this.closeButton.setName("button.form.close");
        this.closeButton.setPosition(2, -(getTopPadding() - 1), Anchor.TOP | Anchor.RIGHT);
        this.closeButton.register(this);

        if (this.titleLabel != null) {
            this.titleLabel.setFontOptions(FontColors.WHITE_FO);
            this.titleLabel.setPosition(0, -getTopPadding() + 5, Anchor.TOP | Anchor.CENTER);
        }
    }

    public boolean isClosable() {
        return this.closable;
    }

    public UIFormContainer setClosable(boolean closable) {
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

    public UIFormContainer setMovable(boolean movable) {
        this.movable = movable;
        return this;
    }

    public void onClose() {
        final MalisisGui currentGui = getGui();
        if (currentGui != null) {
            if (currentGui.isOverlay()) {
                currentGui.closeOverlay();
            } else {
                currentGui.close();
            }
        }
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

        final int targetX = Math.min(parentContainer.getWidth() - this.width, Math.max(xPos, 0));
        final int targetY = Math.min(parentContainer.getHeight() - this.height, Math.max(yPos, 0));
        setPosition(targetX, targetY, Anchor.NONE);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.form.close":
                if (this.closeButton.isInsideBounds(event.getX(), event.getY())) {
                    this.onClose();
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
        this.closeButton.setFontOptions(this.closeButton.isInsideBounds(mouseX, mouseY) ? FontColors.WHITE_FO : FontColors.GRAY_FO);
    }
}
