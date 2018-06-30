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
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.collect.Lists;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UISwapContainer<T> extends UIDualListContainer<T> {

    private UIButton buttonToLeft, buttonToRight, buttonAllToLeft, buttonAllToRight;
    protected int leftItemLimit = -1, rightItemLimit = -1;

    public UISwapContainer(MalisisGui gui, int width, int height, Text leftTitle, Text rightTitle,
            BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory,
            BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> rightComponentFactory) {
        super(gui, width, height, leftTitle, rightTitle, leftComponentFactory, rightComponentFactory);
    }

    @Override
    protected UIContainer<?> createMiddleContainer(MalisisGui gui) {
        final UIContainer<?> middleContainer = new UIContainer(gui, 28, 92);
        middleContainer.setPadding(0, 3);
        middleContainer.setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER);
        middleContainer.setBorder(FontColors.WHITE, 1, 185);
        middleContainer.setBackgroundAlpha(0);

        this.buttonAllToRight = new UIButtonBuilder(gui)
                .size(20)
                .position(0, 0)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("->|")
                .tooltip("Move all to right container")
                .onClick(() -> this.transferAll(ContainerSide.RIGHT))
                .enabled(false)
                .build("button.all_to_right");

        this.buttonToRight = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonAllToRight, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("->")
                .tooltip("Move to right container")
                .onClick(() -> this.transferOne(ContainerSide.RIGHT))
                .enabled(false)
                .build("button.to_right");

        this.buttonToLeft = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonToRight, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("<-")
                .tooltip("Move to left container")
                .onClick(() -> this.transferOne(ContainerSide.LEFT))
                .enabled(false)
                .build("button.to_left");

        this.buttonAllToLeft = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonToLeft, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("|<-")
                .tooltip("Move all to left container")
                .onClick(() -> this.transferAll(ContainerSide.LEFT))
                .enabled(false)
                .build("button.all_to_left");

        middleContainer.add(this.buttonToRight, this.buttonAllToRight, this.buttonToLeft, this.buttonAllToLeft);

        return middleContainer;
    }

    @Override
    protected void updateControls(@Nullable T selectedValue, ContainerSide containerSide) {
        final boolean isSelected = selectedValue != null;
        final boolean isLeftFull = this.leftItemLimit > 0 && this.getListFromSide(ContainerSide.LEFT).getItems().size() == leftItemLimit;
        final boolean isRightFull = this.rightItemLimit > 0 && this.getListFromSide(ContainerSide.RIGHT).getItems().size() == rightItemLimit;

        this.buttonToLeft.setEnabled(containerSide == ContainerSide.RIGHT && isSelected && !isLeftFull);
        this.buttonToRight.setEnabled(containerSide == ContainerSide.LEFT && isSelected && !isRightFull);
        this.buttonAllToLeft.setEnabled(!this.getListFromSide(ContainerSide.RIGHT).getItems().isEmpty() && !isLeftFull);
        this.buttonAllToRight.setEnabled(!this.getListFromSide(ContainerSide.LEFT).getItems().isEmpty() && !isRightFull);

        super.updateControls(selectedValue, containerSide);
    }

    public UIDualListContainer<T> setItemLimit(int limit, ContainerSide target) {
        if (target == ContainerSide.LEFT) {
            this.leftItemLimit = limit;
        } else {
            this.rightItemLimit = limit;
        }

        return this;
    }

    protected void transferOne(ContainerSide target) {
        final UIDynamicList<T> targetList = this.getListFromSide(target);
        final UIDynamicList<T> sourceList = this.getOpposingListFromSide(target);
        final T item = sourceList.getSelectedItem();

        if (item == null) {
            return;
        }

        final int limit = this.getLimitFromSide(target);
        if (limit > -1 && limit == targetList.getItems().size()) {
            return;
        }

        if (targetList.addItem(item)) {
            sourceList.removeItem(item);
            this.fireEvent(new TransactionEvent<>(this, target));
        }
    }

    protected void transferAll(ContainerSide target) {
        final UIDynamicList<T> targetList = this.getListFromSide(target);
        final UIDynamicList<T> sourceList = this.getOpposingListFromSide(target);
        final int limit = this.getLimitFromSide(target);
        Stream<T> limitedItemStream = Lists.newArrayList(sourceList.getItems()).stream();
        if (limit > -1) {
            limitedItemStream = limitedItemStream.limit(Math.max(0, limit - targetList.getItems().size()));
        }

        final List<T> limitedItemList = limitedItemStream.collect(Collectors.toList());
        targetList.addItems(limitedItemList);
        sourceList.removeItems(limitedItemList);

        this.updateControls(null, target);

        this.fireEvent(new TransactionEvent<>(this, target));
    }

    private int getLimitFromSide(ContainerSide side) {
        if (side == ContainerSide.LEFT) {
            return this.leftItemLimit;
        }

        return this.rightItemLimit;
    }

    public static class TransactionEvent<T> extends ComponentEvent<UIDualListContainer<T>> {

        public final ContainerSide side;

        public TransactionEvent(UIDualListContainer<T> component, ContainerSide side) {
            super(component);
            this.side = side;
        }
    }
}
