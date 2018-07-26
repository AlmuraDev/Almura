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

    @Nullable private UIButton buttonOne, buttonStack, buttonItem, buttonAll, buttonDirection;
    private long leftItemLimit = -1, rightItemLimit = -1;
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
    protected void updateControls(@Nullable final MockOffer selectedValue, final ContainerSide containerSide) {

        this.buttonDirection.setText(this.targetSide == ContainerSide.LEFT ? "<-" : "->");
        this.buttonDirection.setTooltip(String.format("Send items to the %s", this.targetSide == ContainerSide.LEFT ? "left" : "right"));

        this.buttonOne.setEnabled(false);
        this.buttonStack.setEnabled(false);
        this.buttonItem.setEnabled(false);

        super.updateControls(selectedValue, containerSide);
    }

    public UIExchangeOfferContainer setItemLimit(long limit, ContainerSide target) {
        if (target == ContainerSide.LEFT) {
            this.leftItemLimit = limit;
        } else {
            this.rightItemLimit = limit;
        }

        return this;
    }

    protected void transfer(final ContainerSide target, @Nullable final MockOffer offer, final TransferType type) {
        final UIDynamicList<MockOffer> sourceList = this.getOpposingListFromSide(target);

        switch (type) {
            case SINGLE:
                this.transferQuantity(target, offer, 1);
                break;
            case STACK:
                if (offer == null) {
                    break;
                }
                this.transferQuantity(target, offer, offer.quantity);
                break;
            case ITEM:
                if (offer == null) {
                    break;
                }
                final List<MockOffer> toCopy = sourceList.getItems().stream()
                    .filter(o -> ItemStackComparators.IGNORE_SIZE.compare(offer.item, o.item) == 0)
                    .collect(Collectors.toList());
                toCopy.forEach(o -> this.transfer(target, o, TransferType.STACK));
                break;
            case ALL:
                this.transferAll(target);
                break;
        }
    }

    private void transferQuantity(final ContainerSide target, @Nullable final MockOffer offer, final long quantity) {
        final UIDynamicList<MockOffer> targetList = this.getListFromSide(target);
        final UIDynamicList<MockOffer> sourceList = this.getOpposingListFromSide(target);
        final long targetSideLimit = this.getLimitFromSide(target);

        if (offer == null
            || targetList.getItems().stream().noneMatch(o -> ItemStackComparators.IGNORE_SIZE.compare(o.item, offer.item) == 0)
            && targetSideLimit > -1
            && targetList.getItems().size() >= targetSideLimit) {
            return;
        }

        final long targetStackMaxQuantity = target == ContainerSide.LEFT ? offer.item.getMaxStackQuantity() : Long.MAX_VALUE;

        final MockOffer targetOffer = targetList.getItems().stream()
            .filter(o -> ItemStackComparators.IGNORE_SIZE.compare(o.item, offer.item) == 0 && o.quantity < targetStackMaxQuantity)
            .findFirst()
            .orElseGet(() -> {
                final MockOffer newOffer = new MockOffer(offer.slotId, offer.item.copy(), Minecraft.getMinecraft().player);
                newOffer.quantity = 0;
                targetList.addItem(newOffer);
                return newOffer;
            });

        final long remainder;
        remainder = this.addQuantity(targetList, offer.slotId, targetOffer, quantity, targetStackMaxQuantity);
        this.removeQuantity(sourceList, offer.slotId, offer, quantity - remainder);
        if (remainder > 0) {
            this.transferQuantity(target, offer, remainder);
        }
    }

    private void transferAll(final ContainerSide target) {
        final List<MockOffer> toCopy = new ArrayList<>(this.getOpposingListFromSide(target).getItems());
        toCopy.forEach(o -> this.transferQuantity(target, o, o.quantity));
    }

    private long getLimitFromSide(final ContainerSide side) {
        if (side == ContainerSide.LEFT) {
            return this.leftItemLimit;
        }

        return this.rightItemLimit;
    }

    private long addQuantity(final UIDynamicList<MockOffer> list, final int originatingSlotId, final MockOffer offer, final long quantity,
        final long max) {
        final long initialQuantity = offer.quantity;
        final long rawTotalQuantity = initialQuantity + quantity;
        final long newQuantity = Math.min(rawTotalQuantity, max);
        final long remainder = rawTotalQuantity - newQuantity;

        offer.quantity = newQuantity;

        this.fireEvent(new TransactionEvent<>(this, getTargetFromList(list), originatingSlotId, offer, quantity - remainder));
        this.fireEvent(new UIDynamicList.ItemsChangedEvent<>(list));

        return remainder;
    }

    private long removeQuantity(final UIDynamicList<MockOffer> list, final int originatingSlotId, final MockOffer offer, final long quantity) {
        final long newQuantity = offer.quantity - quantity;
        offer.quantity = newQuantity;

        if (newQuantity <= 0) {
            list.removeItem(offer);
        }

        this.fireEvent(new TransactionEvent<>(this, getTargetFromList(list), originatingSlotId, offer, -quantity));
        this.fireEvent(new UIDynamicList.ItemsChangedEvent<>(list));

        return newQuantity < 0 ? Math.abs(newQuantity) : 0;
    }

    private void changeDirections() {
        // Invert the target side
        this.targetSide = this.targetSide == ContainerSide.LEFT ? ContainerSide.RIGHT : ContainerSide.LEFT;
        this.updateControls(null, this.targetSide);
    }

    @Subscribe
    private void onItemSelect(final UIDynamicList.SelectEvent<MockOffer> event) {
        final boolean isValidTarget = this.getTargetFromList(event.getComponent()) != this.targetSide;

        this.buttonOne.setEnabled(isValidTarget);
        this.buttonStack.setEnabled(isValidTarget);
        this.buttonItem.setEnabled(isValidTarget);
        this.buttonAll.setEnabled(isValidTarget);
    }

    public enum TransferType {
        SINGLE,
        STACK,
        ITEM,
        ALL
    }

    public static class TransactionEvent<T> extends ComponentEvent<UIDualListContainer<T>> {

        public final ContainerSide side;
        public final MockOffer offer;
        public final int originatingSlotId;
        public final long quantity;

        TransactionEvent(final UIDualListContainer<T> component, final ContainerSide side, final int originatingSlotId, final MockOffer offer,
            final long quantity) {
            super(component);
            this.side = side;
            this.originatingSlotId = originatingSlotId;
            this.offer = offer;
            this.quantity = quantity;
        }
    }
}
