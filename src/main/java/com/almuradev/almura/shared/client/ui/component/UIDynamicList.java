/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIDynamicList<T> extends UIContainer<UIDynamicList<T>> {

    private final UIScrollBar scrollbar;
    private List<T> items = new ArrayList<>();
    private boolean canDeselect, canInternalClick, isDirty, readOnly;
    private int itemSpacing = 0;
    private BiFunction<MalisisGui, T, ? extends ItemComponent<?>> itemComponentFactory = DefaultItemComponent::new;
    @Nullable private T selectedItem;
    @Nullable private Consumer<T> onSelectConsumer;

    public UIDynamicList(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        this.scrollbar = new UISlimScrollbar(gui, this, UIScrollBar.Type.VERTICAL);
        this.scrollbar.setAutoHide(true);

        this.setBackgroundAlpha(0);
    }

    /**
     * Gets an unmodifiable list of items
     * @return The unmodifiable list of items
     */
    public List<T> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    /**
     * Gets the item located at the index provided
     * @param index The index
     * @return The item located at the specified index
     */
    public T getItem(int index) {
        return this.items.get(index);
    }

    /**
     * Adds the provided item to the list
     * @param item The item to add
     * @return True if the item was added, otherwise false
     */
    public boolean addItem(T item) {
        final boolean result = this.items.add(item);

        if (result) {
            this.isDirty = true;
            this.fireEvent(new ItemsChangedEvent<>(this));
        }

        return result;
    }

    /**
     * Adds provided items to the list
     * @param items The items to add
     * @return True if all items were added, otherwise false
     */
    public boolean addItems(List<T> items) {
        final boolean result = this.items.addAll(items);

        if (result) {
            this.isDirty = true;
            this.fireEvent(new ItemsChangedEvent<>(this));
        }

        return result;
    }

    /**
     * Clears current items and adds provided items, restricted by max limit if previously set
     * @param items The items to set
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> setItems(List<T> items) {
        this.items.clear();
        this.items.addAll(items);
        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));

        return this;
    }

    /**
     * Removes specified item from the list
     * @param item The item to remove
     * @return True if the item was removed, otherwise false
     */
    public boolean removeItem(T item) {
        final boolean result = this.items.remove(item);
        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));

        return result;
    }

    /**
     * Removes the item located at the index provided
     * @param index The index
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> removeItem(int index) {
        this.items.remove(index);
        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));
        return this;
    }

    /**
     * REmoves specified items from the list
     * @param items The items to remove
     * @return True if all items were removed, otherwise false
     */
    public boolean removeItems(List<T> items) {
        final boolean result = this.items.removeAll(items);
        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));

        return result;
    }

    /**
     * Clears the list of items
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> clearItems() {
        this.items.clear();
        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));

        return this;
    }

    /**
     * Gets the selected item if present
     * @return The selected item if present, null otherwise
     */
    @Nullable
    public T getSelectedItem() {
        return this.selectedItem;
    }

    /**
     * Sets the selected item (if list is not read-only)
     * @param item The item to select
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> setSelectedItem(@Nullable T item) {
        if (!this.readOnly) {
            if (this.fireEvent(new SelectEvent<>(this, this.selectedItem, item))) {
                this.selectedItem = item;
                this.isDirty = true;
                if (this.onSelectConsumer != null) {
                    this.onSelectConsumer.accept(item);
                }
            }
        }

        return this;
    }

    /**
     * Gets the spacing between {@link ItemComponent}s
     * @return The spacing
     */
    public int getItemComponentSpacing() {
        return this.itemSpacing;
    }

    /**
     * Sets the spacing between {@link ItemComponent}s
     * @param spacing The space to use between components
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> setItemComponentSpacing(int spacing) {
        this.itemSpacing = spacing;
        return this;
    }

    /**
     * Gets read-only status
     * @return True if read-only, otherwise false
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Sets read-only status
     * @param readOnly The value to set the status as
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    /**
     * Gets deselect status
     * @return True if items can be deselected, otherwise false
     */
    public boolean canDeselect() {
        return this.canDeselect;
    }

    /**
     * Sets deselect status
     * @param canDeselect The value to set the status as
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> setCanDeselect(boolean canDeselect) {
        this.canDeselect = canDeselect;
        return this;
    }

    /**
     * Gets internal click status
     * @return The value to set the status as
     */
    public boolean canInternalClick() {
        return this.canInternalClick;
    }

    /**
     * Sets internal click status
     * @param canInternalClick canInternalClick The internal click enable status. If true the clicks on an item component will not count against
     * the item component.
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> setCanInternalClick(boolean canInternalClick) {
        this.canInternalClick = canInternalClick;
        return this;
    }

    /**
     * Gets the item component factory
     * @return The item component factory
     */
    public BiFunction<MalisisGui, T, ? extends ItemComponent<?>> getItemComponentFactory() {
        return this.itemComponentFactory;
    }

    /**
     * Sets the item component factory
     * @param factory The component factory
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> setItemComponentFactory(BiFunction<MalisisGui, T, ? extends ItemComponent<?>> factory) {
        this.itemComponentFactory = factory;
        return this;
    }

    @Nullable
    public Consumer<T> getSelectConsumer() {
        return this.onSelectConsumer;
    }

    public UIDynamicList<T> setSelectConsumer(Consumer<T> onSelectConsumer) {
        this.onSelectConsumer = onSelectConsumer;
        return this;
    }

    public UIScrollBar getScrollBar() {
        return this.scrollbar;
    }

    private void createItemComponents()
    {
        this.removeAll();

        int startY = 0;
        for (T item : this.items)
        {
            final ItemComponent<?> component = this.itemComponentFactory.apply(this.getGui(), item);
            component.attachData(item);
            component.setParent(this);
            component.setPosition(0, startY);

            this.add(component);

            startY += component.getHeight() + this.itemSpacing;
        }

        this.isDirty = false;
    }

    @Override
    public ClipArea getClipArea() {
        return new ClipArea(this);
    }

    @Override
    public void setClipContent(boolean clipContent) {}

    @Override
    public boolean shouldClipContent() {
        return true;
    }

    @Override
    public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (this.isDirty) {
            this.createItemComponents();
        }

        super.draw(renderer, mouseX, mouseY, partialTick);
    }

    public static class ItemComponent<T> extends UIContainer<ItemComponent<T>> {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private static final int INNER_COLOR = org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb();
        private static final int INNER_HOVER_COLOR = org.spongepowered.api.util.Color.ofRgb(40, 40, 40).getRgb();
        private static final int INNER_SELECTED_COLOR = org.spongepowered.api.util.Color.ofRgb(65, 65, 65).getRgb();

        protected T item;

        public ItemComponent(MalisisGui gui, T item) {
            super(gui);

            // Set the item
            this.item = item;

            // Set padding
            this.setPadding(3, 3);

            // Set colors
            this.setColor(INNER_COLOR);
            this.setBorder(BORDER_COLOR, 1, 255);

            // Set default size
            setSize(UIComponent.INHERITED, 15);

            this.construct(gui);
        }

        protected void construct(final MalisisGui gui) {}

        @SuppressWarnings("unchecked")
        @Override
        public boolean onClick(int x, int y) {
            final UIComponent component = getComponentAt(x, y);

            final UIDynamicList parent = (UIDynamicList) this.parent;

            if (this.equals(component) || !this.equals(component) && !parent.canInternalClick && hasParent(this, component)) {

                if (parent.isReadOnly()) {
                    return false;
                }

                if (parent.canDeselect()) {
                    parent.setSelectedItem(parent.getSelectedItem() == this.item ? null : this.item);
                } else {
                    parent.setSelectedItem(this.item);
                }
            }

            return true;
        }

        @Override
        public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            if (this.parent instanceof UIDynamicList) {
                final UIDynamicList parent = (UIDynamicList) this.parent;

                final int width = parent.getRawWidth() - parent.getLeftPadding() - parent.getRightPadding()
                        - (parent.getScrollBar().isEnabled() ? parent.getScrollBar().getRawWidth() + 5 : 0);

                this.setSize(width, getHeight());

                if (parent.getSelectedItem() == this.item) {
                    this.setColor(INNER_SELECTED_COLOR);
                } else if (this.isHovered()) {
                    this.setColor(INNER_HOVER_COLOR);
                } else {
                    this.setColor(INNER_COLOR);
                }

                super.drawBackground(renderer, mouseX, mouseY, partialTick);
            }
        }

        protected void setParent(UIDynamicList<T> parent) {
            this.parent = parent;
        }

        private static boolean hasParent(UIComponent parent, UIComponent component) {
            final UIComponent componentParent = component.getParent();
            if (componentParent == null) {
                return false;
            }

            if (componentParent == parent) {
                return true;
            }

            if (componentParent.getParent() != null) {
                return hasParent(parent, componentParent.getParent());
            }

            return false;
        }
    }

    public static class DefaultItemComponent<T> extends ItemComponent<T> {

        public DefaultItemComponent(MalisisGui gui, T item) {
            super(gui, item);
        }

        @Override
        public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            renderer.drawText(TextFormatting.WHITE + item.toString(), 2, 3, 0);
        }
    }

    public static class ItemsChangedEvent<T> extends ComponentEvent<UIDynamicList<T>> {
        public ItemsChangedEvent(UIDynamicList<T> component) {
            super(component);
        }
    }

    public static class SelectEvent<T> extends ComponentEvent.ValueChange<UIDynamicList<T>, T> {
        public SelectEvent(UIDynamicList<T> component, @Nullable T oldValue, @Nullable T newValue) {
            super(component, oldValue, newValue);
        }
    }
}
