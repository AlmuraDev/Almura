/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component.container;

import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIDualListContainer<T> extends UIContainer<UIDualListContainer<T>> {

    private final BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory, rightComponentFactory;
    private UIContainer middleContainer;
    protected final UIDynamicList<T> leftDynamicList, rightDynamicList;

    @SuppressWarnings("deprecation")
    public UIDualListContainer(MalisisGui gui, int width, int height,
            Text leftTitle,
            Text rightTitle,
            BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory,
            BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> rightComponentFactory) {
        super(gui, width, height);

        // Assign factories
        this.leftComponentFactory = leftComponentFactory;
        this.rightComponentFactory = rightComponentFactory;

        this.setBorder(FontColors.WHITE, 1, 185);
        this.setBackgroundAlpha(0);

        this.middleContainer = this.createMiddleContainer(gui);

        final int middleContainerWidth = this.middleContainer == null ? 0 : this.middleContainer.getWidth();

        // Create left container
        final UIContainer<?> leftContainer = new UIContainer(gui, (this.width - middleContainerWidth - 5) / 2, UIComponent.INHERITED);
        leftContainer.setBackgroundAlpha(0);
        leftContainer.setPadding(4, 4);
        leftContainer.setTopPadding(20);

        final UILabel leftContainerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(leftTitle));
        leftContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

        this.leftDynamicList = new UIDynamicList<>(gui, UIComponent.INHERITED, UIComponent.INHERITED);
        this.leftDynamicList.setItemComponentFactory(this.leftComponentFactory);
        this.leftDynamicList.setItemComponentSpacing(1);
        this.leftDynamicList.setCanDeselect(true);
        this.leftDynamicList.setName("list.left");
        this.leftDynamicList.register(this);

        leftContainer.add(leftContainerLabel, this.leftDynamicList);

        // Create right container
        final UIContainer<?> rightContainer = new UIContainer(gui, (this.width - middleContainerWidth - 5) / 2, UIComponent.INHERITED);
        rightContainer.setBackgroundAlpha(0);
        rightContainer.setPadding(4, 4);
        rightContainer.setTopPadding(20);
        rightContainer.setAnchor(Anchor.TOP | Anchor.RIGHT);

        final UILabel rightContainerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(rightTitle));
        rightContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

        this.rightDynamicList = new UIDynamicList<>(gui, UIComponent.INHERITED, UIComponent.INHERITED);
        this.rightDynamicList.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);
        this.rightDynamicList.setItemComponentFactory(this.rightComponentFactory);
        this.rightDynamicList.setItemComponentSpacing(1);
        this.rightDynamicList.setCanDeselect(true);
        this.rightDynamicList.setName("list.right");
        this.rightDynamicList.register(this);

        rightContainer.add(rightContainerLabel, this.rightDynamicList);

        this.add(leftContainer, this.middleContainer, rightContainer);
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        super.drawBackground(renderer, mouseX, mouseY, partialTick);

        final int startX = this.width / 2;
        final int middleContainerHeight = this.middleContainer == null ? 0 : this.middleContainer.getHeight();
        final int halfHeight = (this.height - middleContainerHeight - (this.borderSize * 2)) / 2;
        int startY = 1;

        // Draw: top -> middle_section
        renderer.drawRectangle(startX, startY, 50, 1, halfHeight, FontColors.WHITE, 185);
        startY += halfHeight;

        // Skip: middle_section
        startY += middleContainerHeight;

        // Draw: middle_section -> bottom
        renderer.drawRectangle(startX, startY, 50, 1, halfHeight, FontColors.WHITE, 185);
    }

    @Subscribe
    private void onItemSelect(UIDynamicList.SelectEvent<T> event) {
        this.updateControls(event.getNewValue(), this.getTargetFromList(event.getComponent()));
    }

    @Subscribe
    private void onItemsChanged(UIDynamicList.ItemsChangedEvent<T> event) {
        this.updateControls(null, this.getTargetFromList(event.getComponent()));
    }

    public UIDualListContainer<T> setItems(List<T> list, ContainerSide target) {
        this.getListFromSide(target).setItems(list);
        this.fireEvent(new PopulateEvent<>(this, target));

        return this;
    }

    public Collection<T> getItems(ContainerSide target) {
        return this.getListFromSide(target).getItems();
    }

    @Nullable
    protected UIContainer<?> createMiddleContainer(MalisisGui gui) {
        return null;
    }

    protected UIDynamicList<T> getListFromSide(ContainerSide target) {
        if (target == ContainerSide.LEFT) {
            return this.leftDynamicList;
        }

        return this.rightDynamicList;
    }

    protected UIDynamicList<T> getOpposingListFromSide(ContainerSide target) {
        if (target == ContainerSide.LEFT) {
            return this.rightDynamicList;
        }

        return this.leftDynamicList;
    }

    protected ContainerSide getTargetFromList(UIDynamicList<T> list) {
        return list.getName().equalsIgnoreCase("list.left") ? ContainerSide.LEFT : ContainerSide.RIGHT;
    }

    protected void updateControls(@Nullable T selectedValue, ContainerSide containerSide) {
        // Unregister and re-register to avoid firing this event when deselecting from the other list
        this.leftDynamicList.unregister(this);
        this.leftDynamicList.setSelectedItem(null);
        this.leftDynamicList.register(this);
        this.rightDynamicList.unregister(this);
        this.rightDynamicList.setSelectedItem(null);
        this.rightDynamicList.register(this);

        this.fireEvent(new UpdateEvent<>(this));
    }

    public static class PopulateEvent<T> extends ComponentEvent<UIDualListContainer<T>> {

        public final ContainerSide side;

        public PopulateEvent(UIDualListContainer<T> component, ContainerSide side) {
            super(component);
            this.side = side;
        }
    }

    public static class UpdateEvent<T> extends ComponentEvent<UIDualListContainer<T>> {

        public UpdateEvent(UIDualListContainer<T> component) {
            super(component);
        }
    }

    public enum ContainerSide {
        LEFT,
        RIGHT
    }
}
