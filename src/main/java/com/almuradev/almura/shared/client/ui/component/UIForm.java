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
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.MouseButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;

@SideOnly(Side.CLIENT)
public class UIForm extends UIContainer<UIForm> {
    private UIContainer<?> titleContainer;
    private UILine line;
    private UISimpleButton closeButton;
    private boolean closable, movable, dragging;

    public UIForm(MalisisGui gui, int width, int height) {
        this(gui, width, height, "");
    }

    public UIForm(MalisisGui gui, int width, int height, String title) {
        super(gui, title, width, height);

        // Defaults
        this.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.setMovable(true);
        this.setBorder(FontColors.WHITE, 1, 185);
        this.setBackgroundAlpha(215);
        this.setColor(Integer.MIN_VALUE);
        this.setPadding(3, 20, 3, 3);

        this.construct(gui);

        // Needs to happen after construct
        this.setClosable(true);
    }

    private void construct(MalisisGui gui) {
        this.titleContainer = new UIContainer<>(gui);
        this.titleContainer.setPosition(-this.getLeftBorderedPadding(), -19, Anchor.TOP | Anchor.LEFT);
        this.titleContainer.setSize(this.getWidth() - this.getRightBorderedPadding(), 15);
        this.titleContainer.setColor(0x363636);
        this.titleContainer.setBackgroundAlpha(215);

        this.add(this.titleContainer);

        this.line = new UILine(gui, this.getWidth() + this.getLeftBorderedPadding() + this.getRightBorderedPadding());
        this.line.setPosition(-(this.getLeftBorderedPadding()), -4);

        this.add(this.line);

        this.closeButton = new UISimpleButton(getGui(), "x");
        this.closeButton.setName("button.form.close");
        this.closeButton.setPosition(0, 0, Anchor.MIDDLE | Anchor.RIGHT);
        this.closeButton.register(this);

        if (this.titleLabel != null) {
            this.remove(this.titleLabel);
            this.titleContainer.add(this.titleLabel);
            this.titleLabel.setFontOptions(FontColors.WHITE_FO.toBuilder().shadow(false).build());
            this.titleLabel.setPosition(0, 1, Anchor.MIDDLE | Anchor.CENTER);
        }
    }

    public boolean isClosable() {
        return this.closable;
    }

    public UIForm setClosable(boolean closable) {
        this.closable = closable;

        if (closable) {
            this.titleContainer.add(this.closeButton);
        } else {
            this.titleContainer.remove(this.closeButton);
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

    public void setTitle(String text, FontOptions options) {
        super.setTitle(text);
        this.titleLabel.setFontOptions(options);
    }

    @SuppressWarnings("deprecation")
    public void setTitle(Text text) {
        super.setTitle(TextSerializers.LEGACY_FORMATTING_CODE.serialize(text));
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

    private int getLeftBorderedPadding() {
        return this.getLeftPadding() - this.borderSize;
    }

    private int getRightBorderedPadding() {
        return this.getRightPadding() - this.borderSize;
    }

    @Override
    public boolean onButtonPress(int x, int y, MouseButton button) {
        this.dragging = !this.closeButton.isInsideBounds(x, y) && this.titleContainer.isInsideBounds(x, y);
        return super.onButtonPress(x, y, button);
    }

    @Override
    public boolean onButtonRelease(int x, int y, MouseButton button) {
        if (button == MouseButton.LEFT && this.dragging) {
            this.dragging = false;
        }
        return super.onButtonRelease(x, y, button);
    }

    @Override
    public boolean onDrag(int lastX, int lastY, int x, int y, MouseButton button) {
        if (!this.movable || !this.dragging) {
            return super.onDrag(lastX, lastY, x, y, button);
        }

        final UIComponent<?> parentContainer = getParent();
        if (parentContainer == null) {
            return super.onDrag(lastX, lastY, x, y, button);
        }

        final int xPos = getParent().relativeX(x) - relativeX(lastX);
        final int yPos = getParent().relativeY(y) - relativeY(lastY);

        final int targetX = Math.min(parentContainer.getWidth() - this.width, Math.max(xPos, 0));
        final int targetY = Math.min(parentContainer.getHeight() - this.height, Math.max(yPos, 0));
        setPosition(targetX, targetY, Anchor.NONE);
        return true;
    }

    @Override
    public UIForm setSize(final int width, final int height) {
        super.setSize(width, height);

        if (this.line != null) {
            this.line.setWidth(this.getWidth() + this.getLeftBorderedPadding() + this.getRightBorderedPadding());
        }

        if (this.titleContainer != null) {
            this.titleContainer.setSize(this.getWidth() - this.getRightBorderedPadding(), 15);
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.form.close":
                if (this.closeButton.isInsideBounds(event.getX(), event.getY())) {
                    this.onClose();
                    if (getParent() instanceof net.malisis.core.client.gui.component.container.UIContainer) {
                        ((net.malisis.core.client.gui.component.container.UIContainer) this.getParent()).remove(this);
                    }
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
