/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import com.almuradev.almura.shared.client.ui.Fonts;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

/**
 * A right click menu for Malisis screens.
 *
 * <p>Attach a menu to a component with {@link #attach(UIComponent, Provider)}: right clicking the component (or anything inside it, e.g.
 * a row of a list) asks the provider for a menu, built fresh each time so its entries can depend on the current state, and opens it at
 * the mouse. Example:</p>
 * <pre>{@code
 * UIContextMenu.attach(row, component -> new UIContextMenu(gui)
 *     .add("Buy 1", () -> buy(1))
 *     .add("Buy All", this::buyAll, () -> canBuyAll())
 *     .separator()
 *     .add("Search this seller", () -> searchSeller(seller)));
 * }</pre>
 *
 * <p>The menu draws over everything, stays on screen, highlights the entry under the mouse, greys out disabled entries and closes when an
 * entry is clicked, when clicking anywhere else or on escape. Opened by {@code MixinUIComponent} (UIComponent#onRightClick).</p>
 */
@SideOnly(Side.CLIENT)
public final class UIContextMenu extends UIComponent {

    /**
     * Builds the menu for a right click on the component it is attached to, or returns null for no menu.
     */
    @FunctionalInterface
    public interface Provider {

        @Nullable
        UIContextMenu create(UIComponent component);
    }

    /**
     * Every UIComponent, through {@code MixinUIComponent}: the component keeps its own provider, so it goes away with the component.
     */
    public interface Holder {

        @Nullable
        Provider almura$getContextMenu();

        void almura$setContextMenu(@Nullable Provider provider);
    }

    private static final int ENTRY_HEIGHT = 12;
    private static final int SEPARATOR_HEIGHT = 5;
    private static final int PADDING_X = 6;
    private static final int MIN_WIDTH = 60;
    private static final int BACKGROUND = 0x101010;
    private static final int BORDER = 0x9A9A9A;
    private static final int HOVER = 0x3C5A8C;
    private static final FontOptions ENABLED_FONT = FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build();
    private static final FontOptions DISABLED_FONT = FontOptions.builder().from(FontColors.DARK_GRAY_FO).shadow(false).build();

    // Weak so a menu left open when its screen closes does not keep the screen
    private static WeakReference<UIContextMenu> open = new WeakReference<>(null);

    private final List<Entry> entries = new ArrayList<>();

    public UIContextMenu(final MalisisGui gui) {
        // Opened on its gui before it is added to it
        this.gui = gui;
        this.setZIndex(500);
        this.setVisible(false);
        this.setBackground(this::drawBackground);
        this.setForeground(this::drawForeground);
    }

    /**
     * Gives a component (and everything inside it) a right click menu.
     */
    public static void attach(final UIComponent component, final Provider provider) {
        ((Holder) component).almura$setContextMenu(provider);
    }

    public static void detach(final UIComponent component) {
        ((Holder) component).almura$setContextMenu(null);
    }

    /**
     * Opens the menu of a right clicked component, if one is attached to it.
     *
     * @return True if a menu opened (the click is handled)
     */
    public static boolean openFor(final UIComponent component, final int mouseX, final int mouseY) {
        final Provider provider = ((Holder) component).almura$getContextMenu();
        if (provider == null) {
            return false;
        }
        final UIContextMenu menu = provider.create(component);
        if (menu == null || menu.entries.isEmpty()) {
            return false;
        }
        menu.open(mouseX, mouseY);
        return true;
    }

    /**
     * @return True if the mouse is over an open menu (what is under it shouldn't react to the mouse)
     */
    public static boolean isMouseOver() {
        final UIContextMenu menu = open.get();
        if (menu == null || !menu.isVisible()) {
            return false;
        }
        for (UIComponent hovered = MalisisGui.getHoveredComponent(); hovered != null; hovered = hovered.getParent()) {
            if (hovered == menu) {
                return true;
            }
        }
        return false;
    }

    public UIContextMenu add(final String label, final Runnable action) {
        return this.add(label, action, () -> true);
    }

    /**
     * Adds an entry that is greyed out and can't be clicked while {@code enabled} is false.
     */
    public UIContextMenu add(final String label, final Runnable action, final BooleanSupplier enabled) {
        this.entries.add(new Entry(label, action, enabled));
        return this;
    }

    public UIContextMenu separator() {
        this.entries.add(null);
        return this;
    }

    /**
     * Opens the menu with its top left corner at the mouse, moved back in if it would leave the screen.
     */
    public void open(final int mouseX, final int mouseY) {
        final UIContextMenu previous = open.get();
        if (previous != null && previous != this) {
            previous.close();
        }

        int width = MIN_WIDTH;
        int height = 2;
        for (final Entry entry : this.entries) {
            if (entry == null) {
                height += SEPARATOR_HEIGHT;
            } else {
                width = Math.max(width, (int) Fonts.MINECRAFT.getStringWidth(entry.label, ENABLED_FONT) + PADDING_X * 2);
                height += ENTRY_HEIGHT;
            }
        }
        this.setSize(width, height);

        final MalisisGui gui = this.getGui();
        final int x = Math.max(1, Math.min(mouseX, gui.width - width - 1));
        final int y = Math.max(1, Math.min(mouseY, gui.height - height - 1));
        gui.addToScreen(this);
        this.setPosition(x, y);
        this.setVisible(true);
        this.setFocused(true);
        open = new WeakReference<>(this);
    }

    public void close() {
        if (open.get() == this) {
            open.clear();
        }
        this.setVisible(false);
        if (this.isFocused()) {
            this.setFocused(false);
        }
        if (this.getGui() != null) {
            this.getGui().removeFromScreen(this);
        }
    }

    @Nullable
    private Entry entryAt(final int mouseX, final int mouseY) {
        final int x = mouseX - this.screenX();
        final int y = mouseY - this.screenY();
        if (x < 0 || x >= this.getWidth()) {
            return null;
        }
        int top = 1;
        for (final Entry entry : this.entries) {
            final int height = entry == null ? SEPARATOR_HEIGHT : ENTRY_HEIGHT;
            if (y >= top && y < top + height) {
                return entry;
            }
            top += height;
        }
        return null;
    }

    private void drawBackground(final GuiRenderer renderer) {
        // Clicking anywhere else takes the focus: close, once the screen is done drawing its components
        if (!this.isFocused()) {
            this.setVisible(false);
            Minecraft.getMinecraft().addScheduledTask(this::close);
            return;
        }

        final int width = this.getWidth();
        final int height = this.getHeight();
        renderer.drawRectangle(0, 0, 0, width, height, BORDER, 255);
        renderer.drawRectangle(1, 1, 0, width - 2, height - 2, BACKGROUND, 240);

        final Entry hovered = this.entryAt(MalisisGui.MOUSE_POSITION.x(), MalisisGui.MOUSE_POSITION.y());
        int top = 1;
        for (final Entry entry : this.entries) {
            if (entry == null) {
                renderer.drawRectangle(3, top + SEPARATOR_HEIGHT / 2, 0, width - 6, 1, BORDER, 160);
                top += SEPARATOR_HEIGHT;
                continue;
            }
            if (entry == hovered && entry.enabled.getAsBoolean()) {
                renderer.drawRectangle(1, top, 0, width - 2, ENTRY_HEIGHT, HOVER, 220);
            }
            top += ENTRY_HEIGHT;
        }
    }

    private void drawForeground(final GuiRenderer renderer) {
        int top = 1;
        for (final Entry entry : this.entries) {
            if (entry == null) {
                top += SEPARATOR_HEIGHT;
                continue;
            }
            renderer.drawText(Fonts.MINECRAFT, entry.label, PADDING_X, top + 2, 0, entry.enabled.getAsBoolean() ? ENABLED_FONT : DISABLED_FONT);
            top += ENTRY_HEIGHT;
        }
    }

    @Override
    public boolean onClick() {
        final Entry entry = this.entryAt(MalisisGui.MOUSE_POSITION.x(), MalisisGui.MOUSE_POSITION.y());
        if (entry == null || !entry.enabled.getAsBoolean()) {
            return true; // Separator, disabled entry or the border: keep the menu open
        }
        this.close();
        entry.action.run();
        return true;
    }

    @Override
    public boolean onRightClick() {
        return true;
    }

    @Override
    public boolean onKeyTyped(final char keyChar, final int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.onKeyTyped(keyChar, keyCode);
    }

    private static final class Entry {

        final String label;
        final Runnable action;
        final BooleanSupplier enabled;

        Entry(final String label, final Runnable action, final BooleanSupplier enabled) {
            this.label = label;
            this.action = action;
            this.enabled = enabled;
        }
    }
}
