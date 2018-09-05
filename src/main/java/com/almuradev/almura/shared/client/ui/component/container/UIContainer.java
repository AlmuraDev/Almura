/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component.container;

import com.almuradev.almura.shared.client.ui.component.UITextBox;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.element.Face;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class UIContainer<T extends UIContainer<T>> extends net.malisis.core.client.gui.component.container.UIContainer<T>
        implements ITransformable.Color {

    protected int topLeftColor = -1;
    protected int topRightColor = -1;
    protected int bottomLeftColor = -1;
    protected int bottomRightColor = -1;
    protected int topLeftAlpha = 255;
    protected int topRightAlpha = 255;
    protected int bottomLeftAlpha = 255;
    protected int bottomRightAlpha = 255;
    protected int leftBorderSize = 0;
    protected int rightBorderSize = 0;
    protected int topBorderSize = 0;
    protected int bottomBorderSize = 0;
    protected int borderColor = 0;
    protected int borderAlpha = 0;

    /**
     * Default constructor, creates the components list.
     *
     * @param gui the gui
     */
    public UIContainer(final MalisisGui gui) {
        super(gui);
        shape = new SimpleGuiShape();
    }

    /**
     * Instantiates a new {@link UIContainer}.
     *
     * @param gui the gui
     * @param title the title
     */
    public UIContainer(final MalisisGui gui, final String title) {
        this(gui);
        setTitle(title);
    }

    /**
     * Instantiates a new {@link UIContainer}.
     *
     * @param gui the gui
     * @param width the width
     * @param height the height
     */
    public UIContainer(final MalisisGui gui, final int width, final int height) {
        this(gui);
        setSize(width, height);
    }

    /**
     * Instantiates a new {@link UIContainer}.
     *
     * @param gui the gui
     * @param title the title
     * @param width the width
     * @param height the height
     */
    public UIContainer(final MalisisGui gui, final String title, final int width, final int height) {
        this(gui);
        setTitle(title);
        setSize(width, height);
    }

    /**
     * Sets the border size, color, and alpha for this {@link UIContainer} for all sides.
     *
     * @param color the color
     * @param size the size
     * @param alpha the alpha
     */
    public UIContainer<T> setBorder(final int color, final int size, final int alpha) {
        return this.setBorders(color, alpha, size, size, size, size);
    }

    /**
     * Sets the border color, alpha, and all side sizes.
     *
     * @param color The color
     * @param alpha The alpha
     * @param leftBorderSize The left size
     * @param topBorderSize The top size
     * @param rightBorderSize The right size
     * @param bottomBorderSize The bottom size
     * @return The {@link UIContainer}
     */
    public UIContainer<T> setBorders(final int color, final int alpha, final int leftBorderSize, final int topBorderSize, final int rightBorderSize,
            final int bottomBorderSize) {
        this.borderColor = color;
        this.borderAlpha = alpha;
        this.leftBorderSize = leftBorderSize;
        this.topBorderSize = topBorderSize;
        this.rightBorderSize = rightBorderSize;
        this.bottomBorderSize = bottomBorderSize;

        if (leftBorderSize >= 0 || rightBorderSize >= 0 || topBorderSize >= 0 || bottomBorderSize >= 0) {
            shape = new SidedShape(leftBorderSize, topBorderSize, rightBorderSize, bottomBorderSize);
        } else {
            shape = new SimpleGuiShape();
        }

        return this;
    }

    /**
     * Gets the top left color.
     *
     * @return the top left color
     */
    public int getTopLeftColor() {
        return topLeftColor;
    }

    /**
     * Sets the top left color.
     *
     * @param topLeftColor the new top left color
     */
    public UIContainer<T> setTopLeftColor(final int topLeftColor) {
        this.topLeftColor = topLeftColor;
        return this;
    }

    /**
     * Gets the top right color.
     *
     * @return the top right color
     */
    public int getTopRightColor() {
        return topRightColor;
    }

    /**
     * Sets the top right color.
     *
     * @param topRightColor the new top right color
     */
    public UIContainer<T> setTopRightColor(final int topRightColor) {
        this.topRightColor = topRightColor;
        return this;
    }

    /**
     * Gets the bottom left color.
     *
     * @return the bottom left color
     */
    public int getBottomLeftColor() {
        return bottomLeftColor;
    }

    /**
     * Sets the bottom left color.
     *
     * @param bottomLeftColor the new bottom left color
     */
    public UIContainer<T> setBottomLeftColor(final int bottomLeftColor) {
        this.bottomLeftColor = bottomLeftColor;
        return this;
    }

    /**
     * Gets the bottom right color.
     *
     * @return the bottom right color
     */
    public int getBottomRightColor() {
        return bottomRightColor;
    }

    /**
     * Sets the bottom right color.
     *
     * @param bottomRightColor the new bottom right color
     */
    public UIContainer<T> setBottomRightColor(final int bottomRightColor) {
        this.bottomRightColor = bottomRightColor;
        return this;
    }

    /**
     * Sets the top color.
     *
     * @param color the new top color
     */
    public UIContainer<T> setTopColor(final int color) {
        setTopLeftColor(color);
        setTopRightColor(color);
        return this;
    }

    /**
     * Sets the bottom color.
     *
     * @param color the new bottom color
     */
    public UIContainer<T> setBottomColor(final int color) {
        setBottomLeftColor(color);
        setBottomRightColor(color);
        return this;
    }

    /**
     * Sets the left color.
     *
     * @param color the new left color
     */
    public UIContainer<T> setLeftColor(final int color) {
        setTopLeftColor(color);
        setBottomLeftColor(color);
        return this;
    }

    /**
     * Sets the right color.
     *
     * @param color the new right color
     */
    public UIContainer<T> setRightColor(final int color) {
        setTopRightColor(color);
        setBottomRightColor(color);
        return this;
    }

    /**
     * Sets the color of this {@link UIContainer}.
     *
     * @param color the new color
     */
    @Override
    public void setColor(final int color) {
        setTopColor(color);
        setBottomColor(color);
    }

    /**
     * Gets the top left alpha.
     *
     * @return the top left alpha
     */
    public int getTopLeftAlpha() {
        return topLeftAlpha;
    }

    /**
     * Sets the top left alpha.
     *
     * @param topLeftAlpha the new top left alpha
     */
    public UIContainer<T> setTopLeftAlpha(final int topLeftAlpha) {
        this.topLeftAlpha = topLeftAlpha;
        return this;
    }

    /**
     * Gets the top right alpha.
     *
     * @return the top right alpha
     */
    public int getTopRightAlpha() {
        return topRightAlpha;
    }

    /**
     * Sets the top right alpha.
     *
     * @param topRightAlpha the new top right alpha
     */
    public UIContainer<T> setTopRightAlpha(final int topRightAlpha) {
        this.topRightAlpha = topRightAlpha;
        return this;
    }

    /**
     * Gets the bottom left alpha.
     *
     * @return the bottom left alpha
     */
    public int getBottomLeftAlpha() {
        return bottomLeftAlpha;
    }

    /**
     * Sets the bottom left alpha.
     *
     * @param bottomLeftAlpha the new bottom left alpha
     */
    public UIContainer<T> setBottomLeftAlpha(final int bottomLeftAlpha) {
        this.bottomLeftAlpha = bottomLeftAlpha;
        return this;
    }

    /**
     * Gets the bottom right alpha.
     *
     * @return the bottom right alpha
     */
    public int getBottomRightAlpha() {
        return bottomRightAlpha;
    }

    /**
     * Sets the bottom right alpha.
     *
     * @param bottomRightAlpha the new bottom right alpha
     */
    public UIContainer<T> setBottomRightAlpha(final int bottomRightAlpha) {
        this.bottomRightAlpha = bottomRightAlpha;
        return this;
    }

    /**
     * Sets the top alpha.
     *
     * @param alpha the new top alpha
     */
    public UIContainer<T> setTopAlpha(final int alpha) {
        setTopLeftAlpha(alpha);
        setTopRightAlpha(alpha);
        return this;
    }

    /**
     * Sets the bottom alpha.
     *
     * @param alpha the new bottom alpha
     */
    public UIContainer<T> setBottomAlpha(final int alpha) {
        setBottomLeftAlpha(alpha);
        setBottomRightAlpha(alpha);
        return this;
    }

    /**
     * Sets the left alpha.
     *
     * @param alpha the new left alpha
     */
    public UIContainer<T> setLeftAlpha(final int alpha) {
        setTopLeftAlpha(alpha);
        setBottomLeftAlpha(alpha);
        return this;
    }

    /**
     * Sets the right alpha.
     *
     * @param alpha the new right alpha
     */
    public UIContainer<T> setRightAlpha(final int alpha) {
        setTopRightAlpha(alpha);
        setBottomRightAlpha(alpha);
        return this;
    }

    /**
     * Sets the alpha background of this {@link UIContainer}.
     *
     * @param alpha the new alpha
     */
    public UIContainer<T> setBackgroundAlpha(final int alpha) {
        setTopAlpha(alpha);
        setBottomAlpha(alpha);
        return this;
    }

    /**
     * Sets the padding for all sides of the container
     *
     * @param padding The padding
     * @return The container
     */
    public UIContainer<T> setPadding(final int padding) {
        this.setLeftPadding(padding);
        this.setTopPadding(padding);
        this.setRightPadding(padding);
        this.setBottomPadding(padding);
        return this;
    }

    /**
     * Sets the padding for all sides of the container
     *
     * @param left The left padding
     * @param top The top padding
     * @param right The right padding
     * @param bottom The bottom padding
     * @return The container
     */
    public UIContainer<T> setPadding(final int left, final int top, final int right, final int bottom) {
        this.setLeftPadding(left);
        this.setTopPadding(top);
        this.setRightPadding(right);
        this.setBottomPadding(bottom);
        return this;
    }

    /**
     * Sets the width of the container
     *
     * @param width The width
     * @return The container
     */
    public UIContainer<T> setWidth(final int width) {
        this.setSize(width, this.height);
        return this;
    }

    /**
     * Sets the height of the container
     *
     * @param height The height
     * @return The container
     */
    public UIContainer<T> setHeight(final int height) {
        this.setSize(this.width, height);
        return this;
    }

    /**
     * Gets the size of the left border
     *
     * @return The border size
     */
    public int getLeftBorderSize() {
        return this.leftBorderSize;
    }

    /**
     * Gets the size of the right border
     *
     * @return The border size
     */
    public int getRightBorderSize() {
        return this.rightBorderSize;
    }

    /**
     * Gets the size of the top border
     *
     * @return The border size
     */
    public int getTopBorderSize() {
        return this.topBorderSize;
    }

    /**
     * Gets the size of the bottom border
     *
     * @return The border size
     */
    public int getBottomBorderSize() {
        return this.bottomBorderSize;
    }

    /**
     * Gets the color of the border
     *
     * @return The border color
     */
    public int getBorderColor() {
        return this.borderColor;
    }

    /**
     * Gets the alpha of the border
     *
     * @return The border alpha
     */
    public int getBorderAlpha() {
        return this.borderAlpha;
    }

    /**
     * Gets an unmodifiable set of components
     *
     * @return The components
     */
    public Set<UIComponent<?>> getComponents() {
        return Collections.unmodifiableSet(this.components);
    }

    public void tabToLastControl() {
        final UITextBox currentTextbox = SimpleScreen.getFocusedComponent() instanceof UITextBox
                ? ((UITextBox) SimpleScreen.getFocusedComponent())
                : null;
        UITextBox toFocus = null;
        final int lastTabIndex = currentTextbox == null ? 0 : currentTextbox.getTabIndex();

        // Attempt to find the next control in line
        final List<UITextBox> filteredComponents = this.components
                .stream()
                .filter(c -> c.isEnabled() && c.isVisible() && c instanceof UITextBox)
                .map(c -> (UITextBox) c)
                .filter(UITextField::isEditable)
                .sorted(Comparator.comparingInt(UITextBox::getTabIndex).reversed())
                .collect(Collectors.toList());

        // Attempt 1: See if we can find an index that is higher than our current
        for (final UITextBox tb : filteredComponents) {
            if (tb == currentTextbox) {
                continue;
            }

            if (tb.getTabIndex() < lastTabIndex) {
                toFocus = tb;
                break;
            }
        }

        // Attempt 2: See if we can find an index with the same value
        if (toFocus == null) {
            for (final UITextBox tb : filteredComponents) {
                if (tb == currentTextbox) {
                    continue;
                }

                if (tb.getTabIndex() == lastTabIndex) {
                    toFocus = tb;
                    break;
                }
            }
        }

        // Attempt 3: We attempt to find the rollover point and start there
        if (toFocus == null) {
            UITextBox highestIndexTextBox = currentTextbox;
            for (final UITextBox tb : filteredComponents) {

                if (tb == currentTextbox) {
                    continue;
                }

                if (highestIndexTextBox == null || tb.getTabIndex() > highestIndexTextBox.getTabIndex()) {
                    highestIndexTextBox = tb;
                }
            }

            if (highestIndexTextBox != null) {
                toFocus = highestIndexTextBox;
            }
        }

        if (toFocus != null && currentTextbox != null) {
            currentTextbox.deselectAll();
            toFocus.focus();
            toFocus.selectAll();
        }
    }

    public void tabToNextControl() {
        final UITextBox currentTextbox = SimpleScreen.getFocusedComponent() instanceof UITextBox
                ? ((UITextBox) SimpleScreen.getFocusedComponent())
                : null;
        UITextBox toFocus = null;
        final int lastTabIndex = currentTextbox == null ? 0 : currentTextbox.getTabIndex();

        // Attempt to find the next control in line
        final List<UITextBox> filteredComponents = this.components
                .stream()
                .filter(c -> c.isEnabled() && c.isVisible() && c instanceof UITextBox)
                .map(c -> (UITextBox) c)
                .filter(UITextField::isEditable)
                .sorted(Comparator.comparingInt(UITextBox::getTabIndex))
                .collect(Collectors.toList());

        // Attempt 1: See if we can find an index that is higher than our current
        for (final UITextBox tb : filteredComponents) {
            if (tb == currentTextbox) {
                continue;
            }

            if (tb.getTabIndex() > lastTabIndex) {
                toFocus = tb;
                break;
            }
        }

        // Attempt 2: See if we can find an index with the same value
        if (toFocus == null) {
            for (final UITextBox tb : filteredComponents) {
                if (tb == currentTextbox) {
                    continue;
                }

                if (tb.getTabIndex() == lastTabIndex) {
                    toFocus = tb;
                    break;
                }
            }
        }

        // Attempt 3: We attempt to find the rollover point and start there
        if (toFocus == null) {
            UITextBox lowestIndexTextBox = currentTextbox;
            for (final UITextBox tb : filteredComponents) {

                if (tb == currentTextbox) {
                    continue;
                }

                if (lowestIndexTextBox == null || tb.getTabIndex() < lowestIndexTextBox.getTabIndex()) {
                    lowestIndexTextBox = tb;
                }
            }

            if (lowestIndexTextBox != null) {
                toFocus = lowestIndexTextBox;
            }
        }

        if (toFocus != null && currentTextbox != null) {
            currentTextbox.deselectAll();
            toFocus.focus();
            toFocus.selectAll();
        }
    }

    @Override
    public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        if (alpha == 0) {
            return;
        }

        renderer.enableBlending();

        Face f = shape.getFaces()[0];
        if (leftBorderSize > 0 || rightBorderSize > 0 || topBorderSize > 0 || bottomBorderSize > 0) {
            f = shape.getFaces()[4];
            rp.colorMultiplier.set(borderColor);
            rp.alpha.set(borderAlpha);
        }

        final RenderParameters frp = f.getParameters();
        frp.usePerVertexColor.set(true);
        frp.usePerVertexAlpha.set(true);
        f.getVertexes("TopLeft").get(0).setColor(topLeftColor).setAlpha(topLeftAlpha);
        f.getVertexes("TopRight").get(0).setColor(topRightColor).setAlpha(topRightAlpha);
        f.getVertexes("BottomLeft").get(0).setColor(bottomLeftColor).setAlpha(bottomLeftAlpha);
        f.getVertexes("BottomRight").get(0).setColor(bottomRightColor).setAlpha(bottomRightAlpha);

        renderer.disableTextures();

        renderer.drawShape(shape, rp);
        renderer.next();

        renderer.enableTextures();
    }

    private static final class SidedShape extends GuiShape {
        protected int left, top, right, bottom;

        SidedShape(final int left, final int top, final int right, final int bottom) {
            super(9);
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;

            this.storeState();
        }

        @Override
        public void setSize(int w, int h) {
            // Establish the correct internal width and height
            w = Math.max(w - this.left - this.right, 0);
            h = Math.max(h - this.top - this.bottom, 0);

            faces[0].scale(left, top, 0);     // TOP_LEFT......Corner (scale width and height by left and top values)
            faces[1].scale(w, top, 0);        // TOP...........Line   (scale height by top value)
            faces[2].scale(right, top, 0);    // TOP_RIGHT.....Corner (scale width and height by right and top values)
            faces[3].scale(left, h, 0);       // LEFT..........Line   (scale width by left value)
            faces[4].scale(w, h, 0);          // CONTAINER.....Center (scale width and height by w and h values)
            faces[5].scale(right, h, 0);      // RIGHT.........Line   (scale width by right value)
            faces[6].scale(left, bottom, 0);  // BOTTOM_LEFT...Corner (scale width and height by left and bottom values)
            faces[7].scale(w, bottom, 0);     // BOTTOM........Line   (scale height by bottom value)
            faces[8].scale(right, bottom, 0); // BOTTOM_RIGHT..Corner (scale width and height by right and bottom values)

            faces[1].translate(left, 0, 0);           // TOP...........Line   (offset x by left value)
            faces[2].translate(left + w, 0, 0);       // TOP_RIGHT.....Corner (offset x by left value)
            faces[3].translate(0, top, 0);            // LEFT..........Line   (offset y by top value)
            faces[4].translate(left, top, 0);         // CONTAINER.....Center (offset x and y by left and top values)
            faces[5].translate(left + w, top, 0);     // RIGHT.........Line   (offset x and y by left and top values)
            faces[6].translate(0, top + h, 0);        // BOTTOM_LEFT...Corner (offset y by top value)
            faces[7].translate(left, top + h, 0);     // BOTTOM........Line   (offset x and y by left and top values)
            faces[8].translate(left + w, top + h, 0); // BOTTOM_RIGHT..Corner (offset x and y by left and top values)
        }

        @Override
        public void scale(final float x, final float y) {}
    }
}
