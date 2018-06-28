/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIDynamicList<T> extends UIContainer<UIDynamicList<T>> {

    private final List<T> items = new ArrayList<>();
    private final UIScrollBar scrollbar;
    private boolean canDeselect, isDirty, readOnly;
    private int itemSpacing = 0;
    private BiFunction<MalisisGui, T, ? extends ItemComponent<?>> itemComponentFactory = DefaultItemComponent::new;
    @Nullable private T selectedItem;

    public UIDynamicList(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        this.scrollbar = new UISlimScrollbar(gui, this, UIScrollBar.Type.VERTICAL);
        this.scrollbar.setAutoHide(true);
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public UIDynamicList<T> addItem(T item) {
        this.items.add(item);
        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));

        return this;
    }

    public UIDynamicList<T> addItems(List<T> items) {
        this.items.addAll(items);
        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));

        return this;
    }

    public UIDynamicList<T> setItems(List<T> items) {
        this.items.clear();
        this.items.addAll(items);
        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));

        return this;
    }

    public UIDynamicList<T> removeItem(T item) {
        this.items.remove(item);
        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));

        return this;
    }

    public UIDynamicList<T> removeItems(List<T> items) {
        this.items.removeAll(items);
        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));

        return this;
    }

    public UIDynamicList<T> clearItems() {
        this.items.clear();
        this.isDirty = true;
        this.fireEvent(new ItemsChangedEvent<>(this));

        return this;
    }

    @Nullable
    public T getSelectedItem() {
        return this.selectedItem;
    }

    public UIDynamicList<T> setSelectedItem(@Nullable T item) {
        if (!this.readOnly) {
            if (this.fireEvent(new SelectEvent<>(this, this.selectedItem, item))) {
                this.selectedItem = item;
                this.isDirty = true;
            }
        }

        return this;
    }

    public int getItemSpacing() {
        return this.itemSpacing;
    }

    public UIDynamicList<T> setItemSpacing(int spacing) {
        this.itemSpacing = spacing;
        return this;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public UIDynamicList<T> setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public boolean canDeselect() {
        return this.canDeselect;
    }

    public UIDynamicList<T> setCanDeselect(boolean canDeselect) {
        this.canDeselect = canDeselect;
        return this;
    }

    public BiFunction<MalisisGui, T, ? extends ItemComponent<?>> getItemComponentFactory() {
        return this.itemComponentFactory;
    }

    public UIDynamicList<T> setItemComponentFactory(BiFunction<MalisisGui, T, ? extends ItemComponent<?>> factory) {
        this.itemComponentFactory = factory;
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

    public static class ItemComponent<T> extends UIBackgroundContainer {
        protected T item;

        public ItemComponent(MalisisGui gui, T item) {
            super(gui);
            this.item = item;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean onClick(int x, int y) {
            final UIComponent component = getComponentAt(x, y);
            if (this.equals(component) && this.parent instanceof UIDynamicList) {
                final UIDynamicList parent = (UIDynamicList) this.parent;

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

        protected void setParent(UIDynamicList<T> parent) {
            this.parent = parent;
        }
    }

    public static class DefaultItemComponent<T> extends ItemComponent<T> {

        public DefaultItemComponent(MalisisGui gui, T item) {
            super(gui, item);
        }

        @Override
        public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            renderer.drawText(item.toString());
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
