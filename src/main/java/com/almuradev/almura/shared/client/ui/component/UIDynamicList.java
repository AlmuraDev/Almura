/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.util.TriFunction;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.util.MouseButton;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIDynamicList<T> extends UIContainer<UIDynamicList<T>> {

    private final UIScrollBar scrollbar;
    private List<T> items = new ArrayList<>();
    private boolean canDeselect, canInternalClick, isDirty, readOnly;
    private int itemSpacing = 0;
    private TriFunction<MalisisGui, UIDynamicList<T>, T, ? extends ItemComponent<?>> itemComponentFactory = DefaultItemComponent::new;
    @Nullable private T selectedItem;
    @Nullable private Consumer<T> onSelectConsumer;

    public UIDynamicList(final MalisisGui gui, final int width, final int height) {
        super(gui, width, height);

        this.scrollbar = new UISlimScrollbar(gui, this, UIScrollBar.Type.VERTICAL);
        this.scrollbar.setAutoHide(true);

        this.setBackgroundAlpha(0);
    }

    public int getSize() {
        return this.getItems().size();
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
    @Nullable
    public T getItem(final int index) {
        return this.items.get(index);
    }

    /**
     * Adds the provided item to the list
     * @param item The item to add
     * @return True if the item was added, otherwise false
     */
    public boolean addItem(final T item) {
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
    public boolean addItems(final List<T> items) {
        final boolean result = this.items.addAll(items);

        if (result) {
            this.isDirty = true;
            this.fireEvent(new ItemsChangedEvent<>(this));
        }

        return result;
    }

    public void addItem(final int index, final T item) {
        this.items.add(index, item);

        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));
    }

    /**
     * Clears current items and adds provided items, restricted by max limit if previously set
     * @param items The items to set
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> setItems(final List<T> items) {
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
    public boolean removeItem(final T item) {
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
    public UIDynamicList<T> removeItem(final int index) {
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
    public boolean removeItems(final List<T> items) {
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
        this.setSelectedItem(null);
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
     * @param markDirty Mark the list as dirty
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> setSelectedItem(@Nullable final T item, final boolean markDirty) {
        if (!this.readOnly) {
            if (this.fireEvent(new SelectEvent<>(this, this.selectedItem, item))) {
                this.selectedItem = item;
                this.isDirty = markDirty;
                if (this.onSelectConsumer != null) {
                    this.onSelectConsumer.accept(item);
                }
            }
        }

        return this;
    }

    /**
     * Sets the selected item (if list is not read-only)
     * @param item The item to select
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> setSelectedItem(@Nullable final T item) {
        return this.setSelectedItem(item, true);
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
    public UIDynamicList<T> setItemComponentSpacing(final int spacing) {
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
    public UIDynamicList<T> setReadOnly(final boolean readOnly) {
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
    public UIDynamicList<T> setCanDeselect(final boolean canDeselect) {
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
    public UIDynamicList<T> setCanInternalClick(final boolean canInternalClick) {
        this.canInternalClick = canInternalClick;
        return this;
    }

    /**
     * Gets the item component factory
     * @return The item component factory
     */
    public TriFunction<MalisisGui, UIDynamicList<T>, T, ? extends ItemComponent<?>> getItemComponentFactory() {
        return this.itemComponentFactory;
    }

    /**
     * Sets the item component factory
     * @param factory The component factory
     * @return The {@link UIDynamicList<T>}
     */
    public UIDynamicList<T> setItemComponentFactory(final TriFunction<MalisisGui, UIDynamicList<T>, T, ? extends ItemComponent<?>> factory) {
        this.itemComponentFactory = factory;
        return this;
    }

    @Nullable
    public Consumer<T> getSelectConsumer() {
        return this.onSelectConsumer;
    }

    public UIDynamicList<T> setSelectConsumer(final Consumer<T> onSelectConsumer) {
        this.onSelectConsumer = onSelectConsumer;
        return this;
    }

    public UIScrollBar getScrollBar() {
        return this.scrollbar;
    }

    public Set<UIComponent<?>> getComponents() {
        return Collections.unmodifiableSet(this.components);
    }

    public void markDirty() {
        this.isDirty = true;
    }

    private void createItemComponents() {
        final float scrollPoint = this.getScrollBar().getOffset();
        final Integer focusedX = MalisisGui.getFocusedComponent() == null ? null : MalisisGui.getFocusedComponent().screenX();
        final Integer focusedY = MalisisGui.getFocusedComponent() == null ? null : MalisisGui.getFocusedComponent().screenY();

        final boolean wasItemFocused = focusedX != null && focusedY != null && this.getComponentAt(focusedX, focusedY) != null;

        this.removeAll();

        int startY = 0;
        for (final T item : this.items) {
            final ItemComponent<?> component = this.itemComponentFactory.apply(this.getGui(), this, item);
            component.attachData(item);
            component.setPosition(0, startY);

            if (wasItemFocused && component.screenX() == focusedX && component.screenY() == focusedY) {
                component.setFocused(true);
            }

            this.add(component);

            startY += component.getHeight() + this.itemSpacing;
        }

        this.getScrollBar().scrollTo(scrollPoint);

        this.isDirty = false;
    }

    @Override
    public ClipArea getClipArea() {
        return new ClipArea(this, this, true);
    }

    @Override
    public void setClipContent(final boolean clipContent) {}

    @Override
    public boolean shouldClipContent() {
        return true;
    }

    @Override
    public void draw(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
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

        @Nullable private Consumer<T> onDoubleClick;
        protected T item;

        public ItemComponent(final MalisisGui gui, final UIDynamicList<T> parent, final T item) {
            this(gui, parent, item, null);
        }

        public ItemComponent(final MalisisGui gui, final UIDynamicList<T> parent, final T item, @Nullable Consumer<T> onDoubleClick) {
            super(gui);

            // Set the parent
            this.setParent(parent);

            // Set the item
            this.item = item;

            // Set the consumer
            this.onDoubleClick = onDoubleClick;

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
        public boolean onClick(final int x, final int y) {
            final UIComponent component = getComponentAt(x, y);

            final UIDynamicList parent = (UIDynamicList) this.parent;

            if (this.equals(component) || !this.equals(component) && !parent.canInternalClick && hasParent(this, component)) {

                if (parent.isReadOnly()) {
                    return false;
                }

                if (parent.canDeselect()) {
                    parent.setSelectedItem(parent.getSelectedItem() == this.item ? null : this.item, false);
                } else {
                    parent.setSelectedItem(this.item, false);
                }
            }

            return true;
        }

        @Override
        public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
            if (this.parent instanceof UIDynamicList) {
                final UIDynamicList parent = (UIDynamicList) this.parent;

                final int width = parent.getRawWidth() - parent.getLeftPadding() - parent.getRightPadding()
                        - (parent.getScrollBar().isEnabled() ? parent.getScrollBar().getRawWidth() + 5 : 0);

                this.setSize(width, getHeight());

                final UIComponent<?> componentAt = this.getComponentAt(mouseX, mouseY);
                if (parent.getSelectedItem() == this.item) {
                    this.setColor(INNER_SELECTED_COLOR);
                } else if (componentAt != null && componentAt.getGui() == MalisisGui.currentGui()
                        && (this.equals(componentAt) || this.equals(componentAt.getParent()))) {
                    this.setColor(INNER_HOVER_COLOR);
                } else {
                    this.setColor(INNER_COLOR);
                }

                super.drawBackground(renderer, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public boolean onDoubleClick(final int x, final int y, final MouseButton button) {
            if (button != MouseButton.LEFT) {
                return super.onDoubleClick(x, y, button);
            }

            final UIComponent<?> componentAt = this.getComponentAt(x, y);
            final UIComponent<?> parentComponentAt = componentAt == null ? null : componentAt.getParent();
            if (!(componentAt instanceof UIDynamicList.ItemComponent) && !(parentComponentAt instanceof UIDynamicList.ItemComponent)) {
                return super.onDoubleClick(x, y, button);
            }

            if (this.onDoubleClick != null) {
                this.onDoubleClick.accept(item);
            }

            return true;
        }

        public void setOnDoubleClickConsumer(@Nullable final Consumer<T> onDoubleClick) {
            this.onDoubleClick = onDoubleClick;
        }

        private static boolean hasParent(final UIComponent parent, final UIComponent component) {
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

        DefaultItemComponent(final MalisisGui gui, final UIDynamicList<T> parent, final T item) {
            super(gui, parent, item);
        }

        @Override
        public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
            renderer.drawText(TextFormatting.WHITE + item.toString(), 2, 3, 0);
        }
    }

    public static class ItemsChangedEvent<T> extends ComponentEvent<UIDynamicList<T>> {
        public ItemsChangedEvent(final UIDynamicList<T> component) {
            super(component);
        }
    }

    public static class SelectEvent<T> extends ComponentEvent.ValueChange<UIDynamicList<T>, T> {
        public SelectEvent(final UIDynamicList<T> component, @Nullable final T oldValue, @Nullable final T newValue) {
            super(component, oldValue, newValue);
        }
    }
}
