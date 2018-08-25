/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.screen;

import com.almuradev.almura.shared.client.GuiConfig;
import net.malisis.core.MalisisCore;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public abstract class SimpleScreen extends MalisisGui {

    protected final Optional<GuiScreen> parent;
    private final boolean drawParentInBackground;

    /**
     * Creates a gui with an absent parent
     */
    public SimpleScreen() {
        this(null);
    }

    /**
     * Creates a gui with a parent
     *
     * @param parent the {@link SimpleScreen} that we came from
     */
    public SimpleScreen(@Nullable GuiScreen parent) {
        this(parent, false);
    }

    public SimpleScreen(@Nullable GuiScreen parent, boolean drawParentInBackground) {
        this.parent = Optional.ofNullable(parent);
        this.renderer.setDefaultTexture(GuiConfig.SpriteSheet.ALMURA);
        this.drawParentInBackground = drawParentInBackground;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        if (this.drawParentInBackground) {
            this.parent.ifPresent(screen -> screen.drawScreen(mouseX, mouseY, partialTick));
        }
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    public void updateScreen() {
        if (this.drawParentInBackground) {
            this.parent.ifPresent(GuiScreen::updateScreen);
        }
        super.updateScreen();
    }

    @Override
    public void onResize(Minecraft minecraft, int width, int height) {
        if (this.drawParentInBackground) {
            this.parent.ifPresent(screen -> screen.onResize(minecraft, width, height));
        }
        super.onResize(minecraft, width, height);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        if (this.drawParentInBackground) {
            this.parent.filter(screen -> screen instanceof MalisisGui).ifPresent(screen -> ((MalisisGui) screen).update(mouseX, mouseY, partialTick));
        }
        super.update(mouseX, mouseY, partialTick);
    }

    @Override
    public void updateGui() {
        if (this.drawParentInBackground) {
            this.parent.filter(screen -> screen instanceof MalisisGui).ifPresent(screen -> ((MalisisGui) screen).updateGui());
        }
        super.updateGui();
    }

    /**
     * Closes this {@link SimpleScreen} and displays the parent, if present.
     */
    @Override
    public void close() {
        setFocusedComponent(null, true);
        setHoveredComponent(null, true);
        Keyboard.enableRepeatEvents(false);

        if (this.mc.player != null && this.mc.player.openContainer != this.mc.player.inventoryContainer) {
            this.mc.player.closeScreen();
        }

        this.onClose();

        this.mc.displayGuiScreen(this.parent.orElse(null));
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        try {
            long time = System.currentTimeMillis();

            UIComponent<?> component = getComponentAt(x, y);
            if (component != null && component.isEnabled()) {
                component.setFocused(true);

                boolean regularClick = true;
                boolean doubleClick = button == this.lastClickButton && time - this.lastClickTime < 250 && component == this.focusedComponent;

                if (doubleClick) {
                    regularClick = !component.onDoubleClick(x, y, MouseButton.getButton(button));
                    this.lastClickTime = 0;
                }

                if (regularClick) {
                    component.onButtonPress(x, y, MouseButton.getButton(button));
                    if (this.draggedComponent == null)
                        this.draggedComponent = component;
                }
            } else {
                setFocusedComponent(null, true);
                if (this.inventoryContainer != null && !this.inventoryContainer.getPickedItemStack().isEmpty()) {
                    MalisisInventoryContainer.ActionType
                            action = button == 1 ? MalisisInventoryContainer.ActionType.DROP_ONE : MalisisInventoryContainer.ActionType.DROP_STACK;
                    MalisisGui.sendAction(action, null, button);
                }
            }

            this.lastClickTime = time;
            this.lastClickButton = button;
        } catch (Exception e) {
            MalisisCore.message("A problem occurred : " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        }
    }

    public void setFocusedComponent(UIComponent<?> component) {
        this.focusedComponent = component;
    }

    public void addToScreen(UIComponent... components) {
        Arrays.stream(components).forEach(this::addToScreen);
    }

    protected void onClose() {}

    /**
     * Gets the X position after applying padding against another component, uses {@link Anchor#LEFT} by default
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @return The padded X position
     */
    public static int getPaddedX(UIComponent<?> component, int padding) {
        return getPaddedX(component, padding, Anchor.LEFT);
    }

    /**
     * Gets the X position after applying padding against another component
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @param anchor The direction we're going
     * @return The padded X position
     */
    public static int getPaddedX(UIComponent<?> component, int padding, int anchor) {
        if (anchor == Anchor.LEFT) {
            return component.getX() + component.getWidth() + padding;
        } else if (anchor == Anchor.RIGHT) {
            return component.getX() - component.getWidth() - padding;
        } else {
            throw new IllegalArgumentException("Invalid anchor used [" + anchor + "], anchor must be LEFT or RIGHT.");
        }
    }

    /**
     * Gets the Y position after applying padding against another component, uses {@link Anchor#TOP} by default
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @return The padded Y position
     */
    public static int getPaddedY(UIComponent<?> component, int padding) {
        return getPaddedY(component, padding, Anchor.TOP);
    }

    /**
     * Gets the Y position after applying padding against another component
     * @param component The component to apply padding against
     * @param padding The padding to use
     * @param anchor The direction we're going
     * @return The padded Y position
     */
    public static int getPaddedY(UIComponent<?> component, int padding, int anchor) {
        if (anchor == Anchor.BOTTOM) {
            return component.getY() - component.getHeight() - padding;
        } else if (anchor == Anchor.TOP) {
            return component.getY() + component.getHeight() + padding;
        } else {
            throw new IllegalArgumentException("Invalid anchor used [" + anchor + "], anchor must be BOTTOM or TOP.");
        }
    }

    /**
     * Gets the width of a component with padding removed
     * @param component The component to get width from
     * @return The width of a component with padding removed
     */
    public static int getPaddedWidth(UIComponent<? extends IScrollable> component) {
        return component.getWidth() - component.self().getLeftPadding() - component.self().getRightPadding();
    }

    /**
     * Gets the height of a component with padding removed
     * @param component The component to get height from
     * @return The height of a component with padding removed
     */
    public static int getPaddedHeight(UIComponent<? extends IScrollable> component) {
        return component.getHeight() - component.self().getTopPadding() - component.self().getBottomPadding();
    }
}
