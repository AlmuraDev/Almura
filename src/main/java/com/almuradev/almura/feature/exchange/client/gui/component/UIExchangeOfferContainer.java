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
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIExchangeOfferContainer extends UIDualListContainer<MockOffer> {

    protected UIButton buttonOne, buttonStack, buttonItem, buttonAll, buttonDirection;
    protected int leftItemLimit = -1, rightItemLimit = -1;
    private ContainerSide targetSide = ContainerSide.RIGHT;

    public UIExchangeOfferContainer(MalisisGui gui, int width, int height, Text leftTitle, Text rightTitle,
            BiFunction<MalisisGui, MockOffer, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory,
            BiFunction<MalisisGui, MockOffer, ? extends UIDynamicList.ItemComponent<?>> rightComponentFactory) {
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
    protected void updateControls(@Nullable MockOffer selectedValue, ContainerSide containerSide) {

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

    protected void transfer(ContainerSide target, @Nullable MockOffer offer, TransferType type) {
        final UIDynamicList<MockOffer> sourceList = this.getOpposingListFromSide(target);

        switch (type) {
            case SINGLE:
                this.transferQuantity(target, offer, 1);
                break;
            case STACK:
                if (offer == null) break;
                this.transferQuantity(target, offer, offer.item.getQuantity());
                break;
            case ITEM:
                if (offer == null) break;
                final List<MockOffer> toCopy = sourceList.getItems().stream()
                        .filter(o -> ItemStackComparators.IGNORE_SIZE.compare(offer.item, o.item) == 0)
                        .collect(Collectors.toList());
                toCopy.forEach(o -> this.transfer(target, o, TransferType.STACK));
                break;
            case ALL:
                this.transferAll(target);
                break;
        }

        this.fireEvent(new TransactionEvent<>(this, target, type));
    }

    private void transferQuantity(ContainerSide target, MockOffer offer, int quantity) {
        final UIDynamicList<MockOffer> targetList = this.getListFromSide(target);
        final UIDynamicList<MockOffer> sourceList = this.getOpposingListFromSide(target);
        final int targetSideLimit = this.getLimitFromSide(target);

        if (offer == null
                || targetList.getItems().stream().noneMatch(o -> ItemStackComparators.IGNORE_SIZE.compare(o.item, offer.item) == 0)
                && targetSideLimit > -1
                && targetList.getItems().size() >= targetSideLimit) {
            return;
        }

        final int targetStackMaxQuantity = target == ContainerSide.LEFT ? offer.item.getMaxStackQuantity() : Integer.MAX_VALUE;

        final MockOffer targetOffer = targetList.getItems().stream()
                .filter(o -> ItemStackComparators.IGNORE_SIZE.compare(o.item, offer.item) == 0 && o.item.getQuantity() < targetStackMaxQuantity)
                .findFirst()
                .orElseGet(() -> {
                    final MockOffer newOffer = new MockOffer(offer.item.copy(), Minecraft.getMinecraft().player);
                    newOffer.item.setQuantity(0);
                    targetList.addItem(newOffer);
                    return newOffer;
                });

        final int remainder;
        remainder = this.addQuantity(targetList, targetOffer, quantity, targetStackMaxQuantity);
        this.removeQuantity(sourceList, offer, quantity - remainder);
        if (remainder > 0) {
            this.transferQuantity(target, offer, remainder);
        }
    }

    private void transferAll(ContainerSide target) {
        final List<MockOffer> toCopy = new ArrayList<>(this.getOpposingListFromSide(target).getItems());
        toCopy.forEach(o -> this.transferQuantity(target, o, o.item.getQuantity()));
    }

    private int getLimitFromSide(ContainerSide side) {
        if (side == ContainerSide.LEFT) {
            return this.leftItemLimit;
        }

        return this.rightItemLimit;
    }

    private int addQuantity(UIDynamicList<MockOffer> list, MockOffer offer, int quantity, int max) {
        final int initialQuantity = offer.item.getQuantity();
        final int rawTotalQuantity = initialQuantity + quantity;
        final int newQuantity = Math.min(rawTotalQuantity, max);
        final int remainder = rawTotalQuantity - newQuantity;

        offer.item.setQuantity(newQuantity);

        this.fireEvent(new UIDynamicList.ItemsChangedEvent<>(list));

        return remainder;
    }

    private int removeQuantity(UIDynamicList<MockOffer> list, MockOffer offer, int quantity) {
        final int initialQuantity = offer.item.getQuantity();
        final int newQuantity = initialQuantity - quantity;
        offer.item.setQuantity(Math.max(0, newQuantity));

        if (newQuantity <= 0) {
            list.removeItem(offer);
        }

        this.fireEvent(new UIDynamicList.ItemsChangedEvent<>(list));

        return newQuantity < 0 ? Math.abs(newQuantity) : 0;
    }

    private void changeDirections() {
        // Invert the target side
        this.targetSide = this.targetSide == ContainerSide.LEFT ? ContainerSide.RIGHT : ContainerSide.LEFT;
        this.updateControls(null, this.targetSide);
    }

    @Subscribe
    private void onItemSelect(UIDynamicList.SelectEvent<MockOffer> event) {
        final boolean isValidTarget = this.getTargetFromList(event.getComponent()) != this.targetSide;

        this.buttonOne.setEnabled(isValidTarget);
        this.buttonStack.setEnabled(isValidTarget);
        this.buttonItem.setEnabled(isValidTarget);
        this.buttonAll.setEnabled(isValidTarget);
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
