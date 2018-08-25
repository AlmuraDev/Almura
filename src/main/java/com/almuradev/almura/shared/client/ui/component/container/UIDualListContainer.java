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
import com.almuradev.almura.shared.client.ui.component.UILine;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
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
    private final Text leftTitle, rightTitle;
    protected UIContainer<?> leftContainer, middleContainer, rightContainer;
    protected UIDynamicList<T> leftDynamicList, rightDynamicList;

    public UIDualListContainer(final MalisisGui gui, final int width, final int height,
            final Text leftTitle, final Text rightTitle,
            final BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory,
            final BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> rightComponentFactory) {
        super(gui, width, height);

        this.leftTitle = leftTitle;
        this.rightTitle = rightTitle;

        this.leftDynamicList = new UIDynamicList<>(gui, UIComponent.INHERITED, UIComponent.INHERITED);
        this.rightDynamicList = new UIDynamicList<>(gui, UIComponent.INHERITED, UIComponent.INHERITED);

        this.leftComponentFactory = leftComponentFactory;
        this.rightComponentFactory = rightComponentFactory;
    }

    @SuppressWarnings("deprecation")
    protected void construct(final MalisisGui gui) {
        this.setBorder(FontColors.WHITE, 1, 185);
        this.setBackgroundAlpha(0);

        this.middleContainer = this.createMiddleContainer(gui);

        final int middleContainerWidth = this.middleContainer == null ? 0 : this.middleContainer.getWidth();

        // Create left container
        this.leftContainer = new UIContainer(gui, (this.width - middleContainerWidth - 5) / 2, UIComponent.INHERITED);
        this.leftContainer.setBackgroundAlpha(0);
        this.leftContainer.setPadding(4, 4);
        this.leftContainer.setTopPadding(20);

        final UILabel leftContainerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(leftTitle));
        leftContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

        this.leftDynamicList.setItemComponentFactory(this.leftComponentFactory);
        this.leftDynamicList.setItemComponentSpacing(1);
        this.leftDynamicList.setCanDeselect(false);
        this.leftDynamicList.setName("list.left");
        this.leftDynamicList.register(this);

        this.leftContainer.add(leftContainerLabel, this.leftDynamicList);

        // Create right container
        this.rightContainer = new UIContainer(gui, (this.width - middleContainerWidth - 5) / 2, UIComponent.INHERITED);
        this.rightContainer.setBackgroundAlpha(0);
        this.rightContainer.setPadding(4, 4);
        this.rightContainer.setTopPadding(20);
        this.rightContainer.setAnchor(Anchor.TOP | Anchor.RIGHT);

        final UILabel rightContainerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(rightTitle));
        rightContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

        this.rightDynamicList.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);
        this.rightDynamicList.setItemComponentFactory(this.rightComponentFactory);
        this.rightDynamicList.setItemComponentSpacing(1);
        this.rightDynamicList.setCanDeselect(false);
        this.rightDynamicList.setName("list.right");
        this.rightDynamicList.register(this);

        this.rightContainer.add(rightContainerLabel, this.rightDynamicList);

        final UILine titleLine = new UILine(gui, this.getRawWidth() - (this.borderSize * 2));
        titleLine.setPosition(0, SimpleScreen.getPaddedY(leftContainerLabel, 2));

        this.add(this.leftContainer, this.middleContainer, this.rightContainer, titleLine);
    }

    @Override
    public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        super.drawBackground(renderer, mouseX, mouseY, partialTick);

        final int startX = this.width / 2;
        final int middleContainerHeight = this.middleContainer == null ? 0 : this.middleContainer.getHeight();
        final int halfHeight = (this.height - middleContainerHeight - (this.borderSize * 2)) / 2;
        int startY = 1;

        // Draw: top-left -> top-right (title line)
        renderer.drawRectangle(this.borderSize, this.leftContainer.getTopPadding() - 4, 1, this.getRawWidth() - (this.borderSize * 2), 1,
                FontColors.WHITE, 185);

        // Draw: top -> middle_section
        renderer.drawRectangle(startX, startY, 50, 1, halfHeight, FontColors.WHITE, 185);
        startY += halfHeight;

        // Skip: middle_section
        startY += middleContainerHeight;

        // Draw: middle_section -> bottom
        renderer.drawRectangle(startX, startY, 50, 1, halfHeight, FontColors.WHITE, 185);
    }

    @Subscribe
    private void onItemSelect(final UIDynamicList.SelectEvent<T> event) {
        this.updateControls(event.getNewValue(), this.getSideFromList(event.getComponent()));
    }

    public UIDualListContainer<T> setItems(final List<T> list, final SideType target) {
        this.getListFromSide(target).setItems(list);
        this.fireEvent(new PopulateEvent<>(this, target));

        return this;
    }

    public Collection<T> getItems(SideType target) {
        return this.getListFromSide(target).getItems();
    }

    @Nullable
    protected UIContainer<?> createMiddleContainer(final MalisisGui gui) {
        return null;
    }

    protected UIDynamicList<T> getListFromSide(final SideType target) {
        if (target == SideType.LEFT) {
            return this.leftDynamicList;
        }

        return this.rightDynamicList;
    }

    protected UIDynamicList<T> getOpposingListFromSide(final SideType target) {
        if (target == SideType.LEFT) {
            return this.rightDynamicList;
        }

        return this.leftDynamicList;
    }

    protected SideType getSideFromList(final UIDynamicList<T> list) {
        return list.getName().equalsIgnoreCase("list.left") ? SideType.LEFT : SideType.RIGHT;
    }

    protected SideType getOpposingSideFromList(final UIDynamicList<T> list) {
        return list.getName().equalsIgnoreCase("list.left") ? SideType.RIGHT : SideType.LEFT;
    }

    protected SideType getOppositeSide(final SideType side) {
        return side == SideType.LEFT ? SideType.RIGHT : SideType.LEFT;
    }

    protected void updateControls(@Nullable final T selectedValue, final SideType targetSide) {
        // Deselect on the list that wasn't targeted
        final UIDynamicList<T> targetList = this.getListFromSide(targetSide);

        // Unregister and re-register to avoid recursion
        targetList.unregister(this);
        targetList.setSelectedItem(null);
        targetList.register(this);

        this.fireEvent(new UpdateEvent<>(this));
    }

    public static class PopulateEvent<T> extends ComponentEvent<UIDualListContainer<T>> {

        public final SideType side;

        public PopulateEvent(final UIDualListContainer<T> component, final SideType side) {
            super(component);
            this.side = side;
        }
    }

    public static class UpdateEvent<T> extends ComponentEvent<UIDualListContainer<T>> {

        public UpdateEvent(final UIDualListContainer<T> component) {
            super(component);
        }
    }

    public enum SideType {
        LEFT,
        RIGHT
    }
}
