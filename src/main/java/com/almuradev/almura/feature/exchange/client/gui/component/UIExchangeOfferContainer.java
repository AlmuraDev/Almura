/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui.component;

import com.almuradev.almura.feature.exchange.MockOffer;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.component.container.UIDualListContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.collect.Lists;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIExchangeOfferContainer extends UIDualListContainer<MockOffer> {

    protected UIButton buttonOneToLeft, buttonStackToLeft, buttonItemToLeft, buttonAllToLeft,
                       buttonOneToRight, buttonStackToRight, buttonItemToRight, buttonAllToRight;
    protected int leftItemLimit = -1, rightItemLimit = -1;

    public UIExchangeOfferContainer(MalisisGui gui, int width, int height, Text leftTitle, Text rightTitle,
            BiFunction<MalisisGui, MockOffer, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory,
            BiFunction<MalisisGui, MockOffer, ? extends UIDynamicList.ItemComponent<?>> rightComponentFactory) {
        super(gui, width, height, leftTitle, rightTitle, leftComponentFactory, rightComponentFactory);
    }

    @Override
    protected UIContainer<?> createMiddleContainer(MalisisGui gui) {
        final UIContainer<?> middleContainer = new UIContainer(gui, 28, 241);
        middleContainer.setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER);
        middleContainer.setBackgroundAlpha(0);

        final UIContainer<?> toRightContainer = new UIContainer<>(gui, 28, 92);
        toRightContainer.setPadding(0, 3);
        toRightContainer.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);
        toRightContainer.setBorder(FontColors.WHITE, 1, 185);
        toRightContainer.setBackgroundAlpha(0);

        this.buttonAllToRight = new UIButtonBuilder(gui)
                .size(20)
                .position(0, 0)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("->|")
                .tooltip("Move all items to right container")
                .onClick(() -> this.transfer(ContainerSide.RIGHT, TransferType.ALL))
//                .enabled(false)
                .build("button.all_to_right");

        this.buttonItemToRight = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonAllToRight, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("I>")
                .tooltip("Move all of this item type right container")
                .onClick(() -> this.transfer(ContainerSide.RIGHT, TransferType.ITEM))
//                .enabled(false)
                .build("button.to_right");

        this.buttonStackToRight = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonItemToRight, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("S>")
                .tooltip("Move entire stack to right container")
                .onClick(() -> this.transfer(ContainerSide.RIGHT, TransferType.STACK))
//                .enabled(false)
                .build("button.to_right");

        this.buttonOneToRight = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonStackToRight, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("1>")
                .tooltip("Move single item to right container")
                .onClick(() -> this.transfer(ContainerSide.RIGHT, TransferType.SINGLE))
//                .enabled(false)
                .build("button.to_right");

        toRightContainer.add(this.buttonOneToRight, this.buttonStackToRight, this.buttonItemToRight, this.buttonAllToRight);


        final UIContainer<?> toLeftContainer = new UIContainer<>(gui, 28, 92);
        toLeftContainer.setPadding(0, 3);
        toLeftContainer.setPosition(0, 0, Anchor.BOTTOM | Anchor.CENTER);
        toLeftContainer.setBorder(FontColors.WHITE, 1, 185);
        toLeftContainer.setBackgroundAlpha(0);

        this.buttonOneToLeft = new UIButtonBuilder(gui)
                .size(20)
                .position(0, 0)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("<1")
                .tooltip("Move single item to left container")
                .onClick(() -> this.transfer(ContainerSide.LEFT, TransferType.SINGLE))
//                .enabled(false)
                .build("button.to_left");

        this.buttonStackToLeft = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonOneToLeft, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("<S")
                .tooltip("Move entire stack to left container")
                .onClick(() -> this.transfer(ContainerSide.LEFT, TransferType.STACK))
//                .enabled(false)
                .build("button.to_left");

        this.buttonItemToLeft = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonStackToLeft, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("<I")
                .tooltip("Move all of this item type left container")
                .onClick(() -> this.transfer(ContainerSide.LEFT, TransferType.ITEM))
//                .enabled(false)
                .build("button.to_left");

        this.buttonAllToLeft = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonItemToLeft, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("|<-")
                .tooltip("Move all items to left container")
                .onClick(() -> this.transfer(ContainerSide.LEFT, TransferType.ALL))
//                .enabled(false)
                .build("button.all_to_left");

        toLeftContainer.add(this.buttonOneToLeft, this.buttonStackToLeft, this.buttonItemToLeft, this.buttonAllToLeft);

        final int startY = SimpleScreen.getPaddedY(toRightContainer, 0);
        final int endY = toLeftContainer.getY() - toLeftContainer.getHeight();
        final UIContainer<?> lineContainer = new UIContainer<>(gui);
        lineContainer.setSize(1, endY - startY);
        lineContainer.setPosition(1, startY, Anchor.TOP | Anchor.CENTER);
        lineContainer.setColor(FontColors.WHITE);
        lineContainer.setBackgroundAlpha(185);

        middleContainer.add(toRightContainer, toLeftContainer, lineContainer);

        return middleContainer;
    }

    @Override
    protected void updateControls(@Nullable MockOffer selectedValue, ContainerSide containerSide) {
        final boolean isSelected = selectedValue != null;
        final boolean isLeftFull = this.leftItemLimit > 0 && this.getListFromSide(ContainerSide.LEFT).getItems().size() == leftItemLimit;
        final boolean isRightFull = this.rightItemLimit > 0 && this.getListFromSide(ContainerSide.RIGHT).getItems().size() == rightItemLimit;

//        this.buttonStackToLeft.setEnabled(containerSide == ContainerSide.RIGHT && isSelected && !isLeftFull);
//        this.buttonStackToRight.setEnabled(containerSide == ContainerSide.LEFT && isSelected && !isRightFull);
//        this.buttonAllToLeft.setEnabled(!this.getListFromSide(ContainerSide.RIGHT).getItems().isEmpty() && !isLeftFull);
//        this.buttonAllToRight.setEnabled(!this.getListFromSide(ContainerSide.LEFT).getItems().isEmpty() && !isRightFull);

        super.updateControls(selectedValue, containerSide);
    }

    public UIExchangeOfferContainer setItemLimit(int limit, ContainerSide target) {
        if (target == ContainerSide.LEFT) {
            this.leftItemLimit = limit;
        } else {
            this.rightItemLimit = limit;
        }

        return this;
    }

    protected void transfer(ContainerSide target, TransferType type) {
        final UIDynamicList<MockOffer> sourceList = this.getOpposingListFromSide(target);
        final MockOffer sourceItem = sourceList.getSelectedItem();

        switch (type) {
            case SINGLE:
                this.transferQuantity(target, 1);
                break;
            case STACK:
                if (sourceItem == null) break;
                this.transferQuantity(target, sourceItem.item.getQuantity());
                break;
            case ITEM:
                break;
            case ALL:

                break;
        }

        this.fireEvent(new TransactionEvent<>(this, target, type));
    }

    private void transferQuantity(ContainerSide target, int quantity) {
        final UIDynamicList<MockOffer> targetList = this.getListFromSide(target);
        final UIDynamicList<MockOffer> sourceList = this.getOpposingListFromSide(target);
        final MockOffer sourceOffer = sourceList.getSelectedItem();

        if (sourceOffer == null) {
            return;
        }

        final Optional<MockOffer> optTargetItem = targetList.getItems().stream()
                .filter(o -> {
                    final net.minecraft.item.ItemStack itemStack = (net.minecraft.item.ItemStack) (Object) o.item;
                    return itemStack.isItemEqual((net.minecraft.item.ItemStack) (Object) sourceOffer.item)
                            && o.item.getQuantity() < (target == ContainerSide.LEFT ? o.item.getMaxStackQuantity() : Integer.MAX_VALUE);})
                .findFirst();

        if (optTargetItem.isPresent()) {
            final int remainder = addQuantity(optTargetItem.get().item, quantity);
            removeQuantity(sourceOffer.item, quantity - remainder);
            if (remainder > 0) {
                this.transferQuantity(target, remainder);
            }
        } else {
            final MockOffer newOffer = new MockOffer(sourceOffer.item.copy(), Minecraft.getMinecraft().player);
            removeQuantity(sourceOffer.item, quantity);
            newOffer.item.setQuantity(quantity);
            targetList.addItem(newOffer);
        }

        if (sourceOffer.item.getQuantity() == 0) {
            sourceList.removeItem(sourceOffer);
        }
    }

    private void transferAll(ContainerSide target) {
        final UIDynamicList<MockOffer> targetList = this.getListFromSide(target);
        final UIDynamicList<MockOffer> sourceList = this.getOpposingListFromSide(target);
        final int itemLimit = this.getLimitFromSide(target);

        Stream<MockOffer> limitedItemStream = Lists.newArrayList(sourceList.getItems()).stream();
        if (itemLimit > -1) {
            limitedItemStream = limitedItemStream.limit(Math.max(0, itemLimit - targetList.getItems().size()));
        }

        final List<MockOffer> limitedItemList = limitedItemStream.collect(Collectors.toList());
        targetList.addItems(limitedItemList);
        sourceList.removeItems(limitedItemList);

        this.updateControls(null, target);
    }

    private int getLimitFromSide(ContainerSide side) {
        if (side == ContainerSide.LEFT) {
            return this.leftItemLimit;
        }

        return this.rightItemLimit;
    }

    private static int removeQuantity(ItemStack stack, int quantity) {
        final int initialQuantity = stack.getQuantity();
        final int newQuantity = initialQuantity - quantity;
        stack.setQuantity(Math.max(0, newQuantity));

        return newQuantity < 0 ? Math.abs(newQuantity) : 0;
    }

    private static int addQuantity(ItemStack stack, int quantity) {
        final int initialQuantity = stack.getQuantity();
        final int maxQuantity = stack.getMaxStackQuantity();
        final int rawTotalQuantity = initialQuantity + quantity;
        final int newQuantity = Math.min(rawTotalQuantity, maxQuantity);
        final int remainder = rawTotalQuantity - newQuantity;

        stack.setQuantity(newQuantity);

        return remainder;
    }

    public static class TransactionEvent<T> extends ComponentEvent<UIDualListContainer<T>> {

        public final ContainerSide side;
        public final TransferType type;

        public TransactionEvent(UIDualListContainer<T> component, ContainerSide side, TransferType type) {
            super(component);
            this.side = side;
            this.type = type;
        }
    }

    public enum TransferType {
        SINGLE,
        STACK,
        ITEM,
        ALL
    }
}
