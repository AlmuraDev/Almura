/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component.container;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.element.Face;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UIContainer<T extends UIContainer<T>> extends net.malisis.core.client.gui.component.container.UIContainer<T>
        implements ITransformable.Color {

    /**
     * Top left corner color
     **/
    protected int topLeftColor = -1;
    /**
     * Top right corner color
     **/
    protected int topRightColor = -1;
    /**
     * Bottom left corner color
     */
    protected int bottomLeftColor = -1;
    /**
     * Bottom right corner color
     */
    protected int bottomRightColor = -1;
    /**
     * Top left corner alpha
     **/
    protected int topLeftAlpha = 255;
    /**
     * Top right corner alpha
     **/
    protected int topRightAlpha = 255;
    /**
     * Bottom left corner alpha
     */
    protected int bottomLeftAlpha = 255;
    /**
     * Bottom right corner alpha
     */
    protected int bottomRightAlpha = 255;
    /**
     * Border size
     **/
    protected int borderSize = 0;
    /**
     * Border color
     **/
    protected int borderColor = 0;
    /**
     * Border alpha
     **/
    protected int borderAlpha = 0;

    /**
     * Default constructor, creates the components list.
     *
     * @param gui the gui
     */
    public UIContainer(MalisisGui gui) {
        super(gui);
        shape = new SimpleGuiShape();
    }

    /**
     * Instantiates a new {@link UIContainer}.
     *
     * @param gui the gui
     * @param title the title
     */
    public UIContainer(MalisisGui gui, String title) {
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
    public UIContainer(MalisisGui gui, int width, int height) {
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
    public UIContainer(MalisisGui gui, String title, int width, int height) {
        this(gui);
        setTitle(title);
        setSize(width, height);
    }

    /**
     * Sets the border size and color for this {@link UIContainer}.
     *
     * @param color the color
     * @param size the size
     */
    public UIContainer<T> setBorder(int color, int size, int alpha) {
        borderColor = color;
        borderSize = size;
        borderAlpha = alpha;
        if (size >= 0) {
            shape = new XYResizableGuiShape(size);
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
    public UIContainer<T> setTopLeftColor(int topLeftColor) {
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
    public UIContainer<T> setTopRightColor(int topRightColor) {
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
    public UIContainer<T> setBottomLeftColor(int bottomLeftColor) {
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
    public UIContainer<T> setBottomRightColor(int bottomRightColor) {
        this.bottomRightColor = bottomRightColor;
        return this;
    }

    /**
     * Sets the top color.
     *
     * @param color the new top color
     */
    public UIContainer<T> setTopColor(int color) {
        setTopLeftColor(color);
        setTopRightColor(color);
        return this;
    }

    /**
     * Sets the bottom color.
     *
     * @param color the new bottom color
     */
    public UIContainer<T> setBottomColor(int color) {
        setBottomLeftColor(color);
        setBottomRightColor(color);
        return this;
    }

    /**
     * Sets the left color.
     *
     * @param color the new left color
     */
    public UIContainer<T> setLeftColor(int color) {
        setTopLeftColor(color);
        setBottomLeftColor(color);
        return this;
    }

    /**
     * Sets the right color.
     *
     * @param color the new right color
     */
    public UIContainer<T> setRightColor(int color) {
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
    public void setColor(int color) {
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
    public UIContainer<T> setTopLeftAlpha(int topLeftAlpha) {
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
    public UIContainer<T> setTopRightAlpha(int topRightAlpha) {
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
    public UIContainer<T> setBottomLeftAlpha(int bottomLeftAlpha) {
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
    public UIContainer<T> setBottomRightAlpha(int bottomRightAlpha) {
        this.bottomRightAlpha = bottomRightAlpha;
        return this;
    }

    /**
     * Sets the top alpha.
     *
     * @param alpha the new top alpha
     */
    public UIContainer<T> setTopAlpha(int alpha) {
        setTopLeftAlpha(alpha);
        setTopRightAlpha(alpha);
        return this;
    }

    /**
     * Sets the bottom alpha.
     *
     * @param alpha the new bottom alpha
     */
    public UIContainer<T> setBottomAlpha(int alpha) {
        setBottomLeftAlpha(alpha);
        setBottomRightAlpha(alpha);
        return this;
    }

    /**
     * Sets the left alpha.
     *
     * @param alpha the new left alpha
     */
    public UIContainer<T> setLeftAlpha(int alpha) {
        setTopLeftAlpha(alpha);
        setBottomLeftAlpha(alpha);
        return this;
    }

    /**
     * Sets the right alpha.
     *
     * @param alpha the new right alpha
     */
    public UIContainer<T> setRightAlpha(int alpha) {
        setTopRightAlpha(alpha);
        setBottomRightAlpha(alpha);
        return this;
    }

    /**
     * Sets the alpha background of this {@link UIContainer}.
     *
     * @param alpha the new alpha
     */
    public UIContainer<T> setBackgroundAlpha(int alpha) {
        setTopAlpha(alpha);
        setBottomAlpha(alpha);
        return this;
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (alpha == 0) {
            return;
        }

        renderer.enableBlending();

        Face f = shape.getFaces()[0];
        if (borderSize != 0) {
            f = shape.getFaces()[4];
            rp.colorMultiplier.set(borderColor);
            rp.alpha.set(borderAlpha);
        }

        RenderParameters frp = f.getParameters();
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
}
