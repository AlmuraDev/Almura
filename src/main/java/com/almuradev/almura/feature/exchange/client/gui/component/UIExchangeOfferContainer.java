/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui.component;

import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.component.container.UIDualListContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.item.BasicVanillaStack;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIExchangeOfferContainer extends UIDualListContainer<BasicVanillaStack> {

    @Nullable private UIButton buttonOne, buttonStack, buttonItem, buttonAll, buttonDirection;
    private int leftItemLimit = -1, rightItemLimit = -1;
    private ContainerSide targetSide = ContainerSide.RIGHT;

    public UIExchangeOfferContainer(MalisisGui gui, int width, int height, Text leftTitle, Text rightTitle,
        BiFunction<MalisisGui, BasicVanillaStack, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory,
        BiFunction<MalisisGui, BasicVanillaStack, ? extends UIDynamicList.ItemComponent<?>> rightComponentFactory) {
        super(gui, width, height, leftTitle, rightTitle, leftComponentFactory, rightComponentFactory);

        this.leftDynamicList.register(this);
        this.rightDynamicList.register(this);
    }

    @Override
    protected UIContainer<?> createMiddleContainer(MalisisGui gui) {
        final UIContainer<?> middleContainer = new UIContainer(gui, 38, 129);
        middleContainer.setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER);
        middleContainer.setBorder(FontColors.WHITE, 1, 185);
        middleContainer.setBackgroundAlpha(0);

        this.buttonOne = new UIButtonBuilder(gui)
            .size(20)
            .position(0, 3)
            .anchor(Anchor.TOP | Anchor.CENTER)
            .text("1")
            .tooltip("Move one of the selected item to the target container")
            .onClick(() -> this.transfer(this.targetSide, this.getOpposingListFromSide(this.targetSide).getSelectedItem(), TransferType.SINGLE))
            .enabled(false)
            .build("button.one");

        this.buttonStack = new UIButtonBuilder(gui)
            .size(20)
            .position(0, SimpleScreen.getPaddedY(this.buttonOne, 2))
            .anchor(Anchor.TOP | Anchor.CENTER)
            .text("S")
            .tooltip("Move the selected stack of items to the target container")
            .onClick(() -> this.transfer(this.targetSide, this.getOpposingListFromSide(this.targetSide).getSelectedItem(), TransferType.STACK))
            .enabled(false)
            .build("button.stack");

        this.buttonDirection = new UIButtonBuilder(gui)
            .size(30)
            .position(0, SimpleScreen.getPaddedY(this.buttonStack, 4))
            .anchor(Anchor.TOP | Anchor.CENTER)
            .text("->")
            .tooltip("Send items to the right")
            .onClick(this::changeDirections)
            .build("button.direction");

        this.buttonItem = new UIButtonBuilder(gui)
            .size(20)
            .position(0, SimpleScreen.getPaddedY(this.buttonDirection, 4))
            .anchor(Anchor.TOP | Anchor.CENTER)
            .text("I")
            .tooltip("Moves all items of the selected type to the target container")
            .onClick(() -> this.transfer(this.targetSide, this.getOpposingListFromSide(this.targetSide).getSelectedItem(), TransferType.ITEM))
            .enabled(false)
            .build("button.item");

        this.buttonAll = new UIButtonBuilder(gui)
            .size(20)
            .position(0, SimpleScreen.getPaddedY(this.buttonItem, 2))
            .anchor(Anchor.TOP | Anchor.CENTER)
            .text("A")
            .tooltip("Moves all items of the selected type to the target container")
            .onClick(() -> this.transfer(this.targetSide, null, TransferType.ALL))
            .build("button.all");

        middleContainer.add(this.buttonOne, this.buttonStack, this.buttonDirection, this.buttonItem, this.buttonAll);

        return middleContainer;
    }

    @Override
    protected void updateControls(@Nullable final BasicVanillaStack selectedValue, final ContainerSide containerSide) {
        this.buttonDirection.setText(this.targetSide == ContainerSide.LEFT ? "<-" : "->");
        this.buttonDirection.setTooltip(String.format("Send items to the %s", this.targetSide == ContainerSide.LEFT ? "left" : "right"));

        this.buttonOne.setEnabled(false);
        this.buttonStack.setEnabled(false);
        this.buttonItem.setEnabled(false);

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

    protected void transfer(final ContainerSide target, @Nullable final BasicVanillaStack stack, final TransferType type) {
        final UIDynamicList<BasicVanillaStack> sourceList = this.getOpposingListFromSide(target);

        switch (type) {
            case SINGLE:
                this.transferQuantity(target, stack, 1);
                break;
            case STACK:
                if (stack == null) {
                    break;
                }
                this.transferQuantity(target, stack, stack.getQuantity());
                break;
            case ITEM:
                if (stack == null) {
                    break;
                }
                final List<BasicVanillaStack> matchingItems = sourceList.getItems().stream()
                    .filter(i -> i != null && ItemStackComparators.IGNORE_SIZE.compare((ItemStack) (Object) stack.asRealStack(),
                            (ItemStack) (Object) i.asRealStack()) == 0)
                    .collect(Collectors.toList());

                matchingItems.forEach(i -> this.transfer(target, i, TransferType.STACK));
                break;
            case ALL:
                this.transferAll(target);
                break;
        }
    }

    private void transferQuantity(final ContainerSide target, @Nullable final BasicVanillaStack stack, final int quantity) {
        final UIDynamicList<BasicVanillaStack> targetList = this.getListFromSide(target);
        final UIDynamicList<BasicVanillaStack> sourceList = this.getOpposingListFromSide(target);
        final int targetSideLimit = this.getLimitFromSide(target);

        if (stack == null
            || targetList.getItems().stream()
                .noneMatch(s ->
                        ItemStackComparators.IGNORE_SIZE.compare((ItemStack) (Object) s.asRealStack(), (ItemStack) (Object) stack.asRealStack()) == 0)
            && targetSideLimit > -1
            && targetList.getItems().size() >= targetSideLimit) {
            return;
        }

        final int targetStackMaxQuantity = target == ContainerSide.LEFT ? ((ItemStack) (Object) stack.asRealStack()).getMaxStackQuantity() : Integer.MAX_VALUE;

        final BasicVanillaStack targetStack = targetList.getItems().stream()
            .filter(s -> ItemStackComparators.IGNORE_SIZE.compare(
                    (ItemStack) (Object) s.asRealStack(),
                    (ItemStack) (Object) stack.asRealStack()) == 0 && s.getQuantity() < targetStackMaxQuantity)
            .findFirst()
            .orElseGet(() -> {
                final BasicVanillaStack newStack = (BasicVanillaStack) stack.copy();
                newStack.setQuantity(0);
                targetList.addItem(newStack);
                return newStack;
            });

        final int remainder;
        remainder = this.addQuantity(targetList, targetStack, quantity, targetStackMaxQuantity);
        this.removeQuantity(sourceList, stack, quantity - remainder);
        if (remainder > 0) {
            this.transferQuantity(target, stack, remainder);
        }
    }

    private void transferAll(final ContainerSide target) {
        final List<BasicVanillaStack> toCopy = new ArrayList<>(this.getOpposingListFromSide(target).getItems());
        toCopy.forEach(s -> this.transferQuantity(target, s, s.getQuantity()));
    }

    private int getLimitFromSide(final ContainerSide side) {
        if (side == ContainerSide.LEFT) {
            return this.leftItemLimit;
        }

        return this.rightItemLimit;
    }

    private int addQuantity(final UIDynamicList<BasicVanillaStack> list, final BasicVanillaStack stack, final int quantity, final int max) {
        final int initialQuantity = stack.getQuantity();
        final int rawTotalQuantity = initialQuantity + quantity;
        final int newQuantity = Math.min(rawTotalQuantity, max);
        final int remainder = rawTotalQuantity - newQuantity;

        stack.setQuantity(newQuantity);

        this.fireEvent(new TransactionEvent<>(this, getTargetFromList(list), stack, quantity - remainder));
        this.fireEvent(new UIDynamicList.ItemsChangedEvent<>(list));

        return remainder;
    }

    private int removeQuantity(final UIDynamicList<BasicVanillaStack> list, final BasicVanillaStack stack, final int quantity) {
        final int newQuantity = stack.getQuantity() - quantity;
        stack.setQuantity(newQuantity);

        if (newQuantity <= 0) {
            list.removeItem(stack);
        }

        this.fireEvent(new TransactionEvent<>(this, getTargetFromList(list), stack, -quantity));
        this.fireEvent(new UIDynamicList.ItemsChangedEvent<>(list));

        return newQuantity < 0 ? Math.abs(newQuantity) : 0;
    }

    private void changeDirections() {
        // Invert the target targetSide
        this.targetSide = this.targetSide == ContainerSide.LEFT ? ContainerSide.RIGHT : ContainerSide.LEFT;
        this.updateControls(null, this.targetSide);
    }

    @Subscribe
    private void onItemSelect(final UIDynamicList.SelectEvent<BasicVanillaStack> event) {
        final ContainerSide targetSide = this.getTargetFromList(event.getComponent());
        final boolean isValidTarget = targetSide != this.targetSide;
        final boolean isValidSource = event.getNewValue() != null;

        this.updateControls(event.getNewValue(), targetSide);

        this.buttonOne.setEnabled(isValidTarget && isValidSource);
        this.buttonStack.setEnabled(isValidTarget && isValidSource);
        this.buttonItem.setEnabled(isValidTarget && isValidSource);
        this.buttonAll.setEnabled(isValidTarget);
    }

    public enum TransferType {
        SINGLE,
        STACK,
        ITEM,
        ALL
    }

    public static class TransactionEvent<T> extends ComponentEvent<UIDualListContainer<T>> {

        public final ContainerSide targetSide;
        public final BasicVanillaStack stack;
        public final int quantity;

        TransactionEvent(final UIDualListContainer<T> component, final ContainerSide targetSide, final BasicVanillaStack stack, final int quantity) {
            super(component);
            this.targetSide = targetSide;
            this.stack = stack;
            this.quantity = quantity;
        }
    }
}
